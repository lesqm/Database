package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.sessions.Session;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import java.util.List;
import java.util.Map;
import ru.lesqm.db.LesqmDatabaseApp;
import ru.lesqm.db.Utils;
import ru.lesqm.db.logic.Database;
import ru.lesqm.db.logic.User;

public class AuthController extends Controller {

    public Response login() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        if (session.get("user-logined") != null && session.getBoolean("user-logined") == true) {
            return redirect(urls.that());
        }
        return ok(view("login.html", new AuthData(forms.generateFormId("auth"))));
    }

    public Response loginProcess() throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        Map<String, List<String>> post = ctx.getRequest().getParameters();
        Session session = ctx.getSession();

        if (session.get("user-logined") != null && session.getBoolean("user-logined") == true) {
            return temporaryRedirect(urls.that());
        }
        if (post.get("email") == null
                || post.get("password") == null
                || post.get("fid") == null) {
            return ok("Bad post parameters " + post.size());
        }

        for (Map.Entry<String, List<String>> e : post.entrySet()) {
            if (e.getValue().isEmpty()) {
                return ok("Bad post parameter: " + e.getKey());
            }
        }

        if (!forms.testFormId("auth", post.get("fid").get(0))) {
            return ok("Bad form id");
        }

        User u = db.getUserByEmail(post.get("email").get(0));
        if (u == null || !u.getPassword().equals(Utils.toSHA1(post.get("password").get(0)))) {
            return ok(view("login.html", new AuthData(forms.generateFormId("auth"),
                    "Электронная почта пользователя и пароль не совпадают или учетная запись отсутствует.")));
        }

        session.put("user-logined", true);
        session.put("user-id", u.getId());
        session.put("user-name", u.getName());

        List<String> get = ctx.getRequest().getQuery().get("r");
        if (get == null || get.isEmpty()) {
            return redirect(urls.that());
        } else {
            return redirect(urls.that(get.get(0)));
        }
    }

    public Response logoutProcess() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        session.remove("user-logined");
        session.remove("user-id");
        session.remove("user-name");

        return redirect(urls.that());
    }

    public Response check() {
        Session session = ctx.getSession();
        if (session.get("user-logined") == null || session.getBoolean("user-logined") == false) {
            return temporaryRedirect(urls.that("restricted"));
        }

        return proceed();
    }

    public static class AuthData {

        public String fid;
        public String error = "";

        public AuthData(String fid) {
            this.fid = fid;
        }

        public AuthData(String fid, String error) {
            this.fid = fid;
            this.error = error;
        }
    }
}
