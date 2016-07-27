package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.dependency.Inject;
import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import ru.lesqm.db.services.DatabaseService;

public class BaseController extends Controller {

    private final DatabaseService db;

    @Inject
    public BaseController(DatabaseService db) {
        this.db = db;
    }

    public Response selectAllUsers() {
        ctx.put("users", db.getUserAll());

        return proceed();
    }
}
