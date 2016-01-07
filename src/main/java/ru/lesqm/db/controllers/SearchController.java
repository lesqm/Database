package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import java.util.List;
import ru.lesqm.db.LesqmDatabaseApp;
import ru.lesqm.db.logic.Database;
import ru.lesqm.db.logic.Molecule;

public class SearchController extends Controller {

    public Response search() throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        List<String> q = ctx.getRequest().getQuery().get("q");

        if (q == null || q.isEmpty()) {
            return temporaryRedirect(urls.that());
        }
        
        String query = q.get(0);

        List<Molecule> lm = db.searchMilecule(query);
        
        for(Molecule m : lm) {
            m.setMClasses(db.getMoleculeClasses(m.getId()));
            m.setKeywords(db.getMoleculeKeywords(m.getId()));
        }
        
        return ok(view("search.html", lm));
    }
}
