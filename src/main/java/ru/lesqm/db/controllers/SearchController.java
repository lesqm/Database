package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.dependency.Inject;
import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import java.util.List;
import ru.lesqm.db.services.DatabaseService;
import ru.lesqm.db.logic.Molecule;

public class SearchController extends Controller {

    private final DatabaseService db;

    @Inject
    public SearchController(DatabaseService db) {
        this.db = db;
    }

    public Response search() throws TemplateNotFoundException, TemplateRenderException {
        List<String> q = ctx.getRequest().getQuery().get("q");

        if (q == null || q.isEmpty()) {
            return temporaryRedirect(urls.that());
        }

        String query = q.get(0);

        List<Molecule> lm = db.searchMilecule(query);

        lm.forEach((m) -> {
            m.setMClasses(db.getMoleculeClasses(m.getId()));
            m.setKeywords(db.getMoleculeKeywords(m.getId()));
        });

        ctx.put("lm", lm);

        return ok(view("search.html"));
    }
}
