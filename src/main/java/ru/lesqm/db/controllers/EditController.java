package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.sessions.Session;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.lesqm.db.LesqmDatabaseApp;
import ru.lesqm.db.logic.Database;
import ru.lesqm.db.logic.Keyword;
import ru.lesqm.db.logic.MClass;
import ru.lesqm.db.logic.Molecule;


public class EditController extends Controller {

    public Response add() throws TemplateNotFoundException, TemplateRenderException {
        return ok(view("add.html", new EditorData(forms.generateFormId("add"), null)));
    }

    public Response addProcess() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();
        
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        Map<String, List<String>> post = ctx.getRequest().getParameters();

        if (post.get("formula") == null
                || post.get("picture") == null
                || post.get("baseName") == null
                || post.get("nomeName") == null
                || post.get("nomeNameEng") == null
                || post.get("codeName") == null
                || post.get("content") == null
                || post.get("literature") == null
                || post.get("keywords") == null
                || post.get("classes") == null
                || post.get("fid") == null) {
            return ok("Bad post parameters " + post.size());
        }

        for (Map.Entry<String, List<String>> e : post.entrySet()) {
            if (e.getValue().isEmpty()) {
                return ok("Bad post parameter: " + e.getKey());
            }
        }

        if (!forms.testFormId("add", post.get("fid").get(0))) {
            return ok("Bad form id");
        }

        Molecule m = new Molecule();

        m.setFormula(post.get("formula").get(0));
        m.setPicture(post.get("picture").get(0));
        m.setBaseName(post.get("baseName").get(0));
        m.setNomeName(post.get("nomeName").get(0));
        m.setNomeNameEng(post.get("nomeNameEng").get(0));
        m.setCodeName(post.get("codeName").get(0));
        m.setContent(post.get("content").get(0));
        m.setLiterature(post.get("literature").get(0));
        m.setCtype(1);
        m.setUid((long) session.get("user-id"));

        long id = db.putMolecule(m);

        String[] keywords = post.get("keywords").get(0).split(",");
        List<Long> kids = new ArrayList<>();
        for (String keyword : keywords) {
            kids.add(db.putKeyword(keyword.trim()));
        }
        db.putKeywordsToMoleculeById(id, kids);

        String[] classes = post.get("classes").get(0).split(",");
        List<Long> clids = new ArrayList<>();
        for (String mclass : classes) {
            clids.add(db.putClass(mclass.trim()));
        }
        db.putClassesToMoleculeById(id, clids);

        return redirect(urls.that("change/" + Molecule.hid.encode(id)));
    }

    public Response edit(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeByMid(mid[0])) == null) {
            return notFoundDefault(ctx);
        }

        m.setKeywords(db.getMoleculeKeywords(m.getId()));

        return ok(view("edit.html", new EditorData(forms.generateFormId("edit"), m)));
    }

    public Response editProcess(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();


        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeByMid(mid[0])) == null) {
            return notFoundDefault(ctx);
        }

        Map<String, List<String>> post = ctx.getRequest().getParameters();

        if (post.get("formula") == null
                || post.get("picture") == null
                || post.get("baseName") == null
                || post.get("nomeName") == null
                || post.get("nomeNameEng") == null
                || post.get("codeName") == null
                || post.get("content") == null
                || post.get("literature") == null
                || post.get("keywords") == null
                || post.get("classes") == null
                || post.get("fid") == null) {
            return ok("Bad post parameters " + post.size());
        }

        for (Map.Entry<String, List<String>> e : post.entrySet()) {
            if (e.getValue().isEmpty()) {
                return ok("Bad post parameter: " + e.getKey());
            }
        }

        if (!forms.testFormId("edit", post.get("fid").get(0))) {
            return ok("Bad form id");
        }

        m.setFormula(post.get("formula").get(0));
        m.setPicture(post.get("picture").get(0));
        m.setBaseName(post.get("baseName").get(0));
        m.setNomeName(post.get("nomeName").get(0));
        m.setNomeNameEng(post.get("nomeNameEng").get(0));
        m.setCodeName(post.get("codeName").get(0));
        m.setContent(post.get("content").get(0));
        m.setLiterature(post.get("literature").get(0));
        m.setCtype(1);
        m.setUid((long) session.get("user-id"));

        long id = db.putMoleculeChange(m);

        String[] keywords = post.get("keywords").get(0).split(",");
        List<Long> kids = new ArrayList<>();
        for (String keyword : keywords) {
            kids.add(db.putKeyword(keyword.trim()));
        }

        String[] classes = post.get("classes").get(0).split(",");
        List<Long> clids = new ArrayList<>();
        for (String mclass : classes) {
            clids.add(db.putClass(mclass.trim()));
        }

        db.putKeywordsToMoleculeById(id, kids);
        db.putClassesToMoleculeById(id, clids);

        return redirect(urls.that("view/" + Molecule.hmid.encode(m.getMid())
                + "/" + Molecule.hid.encode(id)));
    }

    public Response deleteProcess(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeByMid(mid[0])) == null) {
            return notFoundDefault(ctx);
        }

        m.setUid((long) session.get("user-id"));
        db.putMoleculeDelete(m);
        return redirect(urls.that());
    }

    public Response revertProcess(String hid) throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] id = Molecule.hid.decode(hid.toLowerCase());
        Molecule m = null;

        if (id.length == 0 || (m = db.getMoleculeById(id[0])) == null) {
            return notFoundDefault(ctx);
        }

        List<Keyword> keywords = db.getMoleculeKeywords(m.getId());
        List<Long> kids = new ArrayList<>();
        keywords.stream().forEach((k) -> {
            kids.add(k.getId());
        });

        List<MClass> classess = db.getMoleculeClasses(m.getId());
        List<Long> clids = new ArrayList<>();
        classess.stream().forEach((c) -> {
            clids.add(c.getId());
        });

        m.setUid((long) session.get("user-id"));
        long newid = db.putMoleculeRevert(m);

        db.putKeywordsToMoleculeById(newid, kids);
        db.putKeywordsToMoleculeById(newid, clids);

        return redirect(urls.that("view/" + Molecule.hmid.encode(m.getMid())
                + "/" + Molecule.hid.encode(newid)));
    }

    public static class EditorData {

        public String fid;
        public Molecule m;

        protected EditorData(String fid, Molecule m) {
            this.fid = fid;
            this.m = m;
        }
    }
}
