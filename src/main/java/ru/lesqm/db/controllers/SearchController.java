package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import java.util.List;
import ru.lesqm.db.LesqmDatabaseApp;
import ru.lesqm.db.logic.Database;
import ru.lesqm.db.logic.DatabaseSearch;
import ru.lesqm.db.logic.Molecule;

public class SearchController extends Controller {

    public Response search() throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();
        final DatabaseSearch search = ((LesqmDatabaseApp) ctx.getApp()).getDatabaseSearch();

        List<String> q = ctx.getRequest().getQuery().get("q");

        if (q == null || q.isEmpty()) {
            return temporaryRedirect(urls.that());
        }
        String query = q.get(0);

        List<Molecule> lm = db.getMoleculeByIds(search.search(query));
        return ok(view("search.html", lm));
    }
}
