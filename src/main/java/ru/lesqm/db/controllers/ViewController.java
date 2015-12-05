package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import java.util.List;
import ru.lesqm.db.LesqmDatabaseApp;
import ru.lesqm.db.logic.*;

public class ViewController extends Controller {

    public Response viewChangesAll() throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        List<Log> logList;

        if ((logList = db.getChangesAll()) == null) {
            return notFoundDefault(ctx);
        }

        return ok(view("viewChanges.html", logList));
    }

    public Response viewByHmid(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeById(mid[0])) == null) {
            return notFoundDefault(ctx);
        }

        m.setKeywords(db.getMoleculeKeywords(m.getId()));
        m.setMClasses(db.getMoleculeClasses(m.getId()));

        return ok(view("view.html", m));
    }
}
