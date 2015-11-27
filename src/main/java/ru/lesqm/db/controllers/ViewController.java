package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import java.util.List;
import ru.lesqm.db.LesqmDatabaseApp;
import ru.lesqm.db.logic.*;

public class ViewController extends Controller {

    public Response viewChangesByHmid(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        List<Molecule> lm = null;

        if (mid.length == 0 || (lm = db.getMoleculeByMidAll(mid[0])) == null) {
            return notFoundDefault(ctx);
        }

        return ok(view("viewChanges.html", lm));
    }

    public Response viewChangesAll() throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        List<Molecule> lm;

        if ((lm = db.getMoleculeChangesAll()) == null) {
            return notFoundDefault(ctx);
        }

        return ok(view("viewChanges.html", lm));
    }

    public Response viewByHmid(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeByMid(mid[0])) == null) {
            return notFoundDefault(ctx);
        }

        m.setKeywords(db.getMoleculeKeywords(m.getId()));
        m.setMClasses(db.getMoleculeClasses(m.getId()));

        return ok(view("view.html", m));
    }

    public Response viewByHid(String hid) throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] id = Molecule.hid.decode(hid.toLowerCase());
        Molecule m = null;

        if (id.length == 0 || (m = db.getMoleculeById(id[0])) == null) {
            return notFoundDefault(ctx);
        }

        return temporaryRedirect(urls.that("view/" + Molecule.hmid.encode(m.getMid())
                + "/" + Molecule.hid.encode(m.getId())));
    }

    public Response viewByHmidAndHid(String hmid, String hid) throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        long[] id = Molecule.hid.decode(hid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || id.length == 0 || (m = db.getMoleculeByMidAndId(mid[0], id[0])) == null) {
            return notFoundDefault(ctx);
        }

        m.setKeywords(db.getMoleculeKeywords(m.getId()));
        m.setMClasses(db.getMoleculeClasses(m.getId()));

        return ok(view("view.html", m));
    }
}
