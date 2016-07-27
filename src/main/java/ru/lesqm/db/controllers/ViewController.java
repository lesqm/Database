package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.dependency.Inject;
import ru.lesqm.db.services.DatabaseService;
import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import java.util.List;
import ru.lesqm.db.logic.*;

public class ViewController extends Controller {

    private final DatabaseService db;

    @Inject
    public ViewController(DatabaseService db) {
        this.db = db;
    }

    public Response index() {
        return seeOther(urls.that("changes", "add"));
    }

    public Response viewChangesAll() throws TemplateNotFoundException, TemplateRenderException {
        List<Log> logList;

        if ((logList = db.getChangesAll()) == null) {
            return notFound();
        }

        logList.forEach((m) -> {
            m.setMClasses(db.getMoleculeClasses(m.getMid()));
            m.setKeywords(db.getMoleculeKeywords(m.getMid()));
        });

        ctx.put("logList", logList);

        return ok(view("changes.html"));
    }

    public Response viewChangesAdd() throws TemplateNotFoundException, TemplateRenderException {
        List<Log> logList;

        if ((logList = db.getChangesAdd()) == null) {
            return notFound();
        }

        logList.forEach((m) -> {
            m.setMClasses(db.getMoleculeClasses(m.getMid()));
            m.setKeywords(db.getMoleculeKeywords(m.getMid()));
        });

        ctx.put("logList", logList);

        return ok(view("adding.html"));
    }

    public Response viewByHmid(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeById(mid[0])) == null) {
            return notFound();
        }

        m.setKeywords(db.getMoleculeKeywords(m.getId()));
        m.setMClasses(db.getMoleculeClasses(m.getId()));

        ctx.put("m", m);

        return ok(view("view.html"));
    }
}
