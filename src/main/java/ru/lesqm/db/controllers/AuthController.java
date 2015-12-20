package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.sessions.Session;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import com.google.gson.Gson;
import java.nio.charset.Charset;
import ru.lesqm.db.LesqmDatabaseApp;
import ru.lesqm.db.Utils;
import ru.lesqm.db.logic.Database;
import ru.lesqm.db.logic.User;
import ru.lesqm.db.utils.JsonError;
import ru.lesqm.db.utils.JsonOkUrl;

public class AuthController extends Controller {

    private static final Gson gson = new Gson();

    public Response login() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        if (session.get("user") == null) {
            return ok(view("login.html"));
        }

        return redirect(urls.that());
    }

    public Response loginProcess() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        if (session.get("user-logined") != null && session.getBoolean("user-logined") == true) {
            return temporaryRedirect(urls.that());
        }

        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        AuthData adata = gson.fromJson(ctx.getRequest().getContent().toString(Charset.forName("UTF-8")), AuthData.class);

        User u = db.getUserByEmail(adata.email);

        if (u == null || !u.getPassword().equals(Utils.toSHA1(adata.password))) {
            return ok(gson.toJson(new JsonError("Неверный логин или пароль"))).asJson();
        }

        session.put("user-logined", true);
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

        String uri = ctx.getRequest().getUri();
        uri = uri.length() > 1 ? "?r=" + urls.that(uri.substring(1)) : "";
        if (session.get("user") == null) {
            return temporaryRedirect(urls.that("signin" + uri));
        }

        return proceed();
    }

    public static class AuthData {

        public String email;
        public String password;
    }
}
