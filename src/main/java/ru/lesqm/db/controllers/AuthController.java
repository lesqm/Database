package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.dependency.Inject;
import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.sessions.Session;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import com.google.gson.Gson;
import ru.lesqm.db.utils.Utils;
import ru.lesqm.db.services.DatabaseService;
import ru.lesqm.db.logic.User;
import ru.lesqm.db.utils.JsonError;
import ru.lesqm.db.utils.JsonOkUrl;

public class AuthController extends Controller {

    private static final Gson gson = new Gson();

    private final DatabaseService db;

    @Inject
    public AuthController(DatabaseService db) {
        this.db = db;
    }

    public Response login() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        if (session.get("user") == null) {
            return ok(view("login.html"));
        }

        return redirect(urls.that());
    }

    public Response loginProcess() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        if (session.get("user") != null) {
            return temporaryRedirect(urls.that());
        }

        AuthData adata = gson.fromJson(ctx.getRequest().getContent().asString(), AuthData.class);

        User u = db.getUserByEmail(adata.email);

        if (u == null || !u.getPassword().equals(Utils.toSHA1(adata.password))) {
            return ok(gson.toJson(new JsonError("Неверный логин или пароль"))).asJson();
        }

        session.put("user-logined", true);
        session.put("user-db", adata.db);
        session.put("user", u);

        return ok(gson.toJson(new JsonOkUrl(urls.that()))).asJson();
    }

    public Response logoutProcess() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        session.remove("user-logined");
        session.remove("user");

        return redirect(urls.that());
    }

    public Response check() {
        Session session = ctx.getSession();

        if (session.get("user") == null) {
            String uri = ctx.getRequest().getUri();
            uri = uri.length() > 1 ? "?r=" + urls.that(uri.substring(1)) : "";

            return temporaryRedirect(urls.that("signin" + uri));
        }

        return proceed();
    }

    public static class AuthData {

        public String db;
        public String email;
        public String password;
    }
}
