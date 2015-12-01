package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.sessions.Session;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import com.google.gson.Gson;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.lesqm.db.LesqmDatabaseApp;
import ru.lesqm.db.logic.Database;
import ru.lesqm.db.logic.Keyword;
import ru.lesqm.db.logic.MClass;
import ru.lesqm.db.logic.Molecule;
import ru.lesqm.db.logic.MoleculeJson;

public class EditController extends Controller {

    private static final Gson gson = new Gson();

    public static class JsonOk {

        public JsonOk(String url) {
            this.url = url;
        }

        public String status = "ok";
        public String url;
    }

    public Response add() throws TemplateNotFoundException, TemplateRenderException {
        return ok(view("add.html", new EditorData(forms.generateFormId("add"), null)));
    }

    public Response addProcess() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        MoleculeJson inputMol = gson.fromJson(ctx.getRequest().getContent().toString(Charset.forName("UTF-8")), MoleculeJson.class);

        Molecule m = new Molecule();

        m.setFormula(inputMol.formula);
        m.setPicture(inputMol.picture);
        m.setBaseName(inputMol.baseName);
        m.setNomeName(inputMol.nomeName);
        m.setNomeNameEng(inputMol.nomeNameEng);
        m.setCodeName(inputMol.codeName);
        m.setContent(inputMol.content);
        m.setLiterature(inputMol.literature);
        m.setCtype(1);
        m.setUid((long) session.get("user-id"));

        long id = db.putMolecule(m);

        String[] keywords = inputMol.keywords.split(",");
        List<Long> kids = new ArrayList<>();
        for (String keyword : keywords) {
            kids.add(db.putKeyword(keyword.trim()));
        }
        db.putKeywordsToMoleculeById(id, kids);

        String[] classes = inputMol.classes.split(",");
        List<Long> clids = new ArrayList<>();
        for (String mclass : classes) {
            clids.add(db.putClass(mclass.trim()));
        }
        db.putClassesToMoleculeById(id, clids);

        return ok(gson.toJson(new JsonOk(urls.that("change/" + Molecule.hid.encode(id))))).asJson();
    }

    public Response edit(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeByMid(mid[0])) == null) {
            return notFoundDefault(ctx);
        }

        List<Keyword> keywords = db.getMoleculeKeywords(m.getId());
        List<MClass> mClasses = db.getMoleculeClasses(m.getId());

        m.setKeywords(keywords == null ? new ArrayList<>() : keywords);
        m.setMClasses(mClasses == null ? new ArrayList<>() : mClasses);

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

        MoleculeJson inputMol = gson.fromJson(ctx.getRequest().getContent().toString(Charset.forName("UTF-8")), MoleculeJson.class);

        m.setFormula(inputMol.formula);
        m.setPicture(inputMol.picture);
        m.setBaseName(inputMol.baseName);
        m.setNomeName(inputMol.nomeName);
        m.setNomeNameEng(inputMol.nomeNameEng);
        m.setCodeName(inputMol.codeName);
        m.setContent(inputMol.content);
        m.setLiterature(inputMol.literature);
        m.setCtype(1);
        m.setUid((long) session.get("user-id"));

        long id = db.putMoleculeChange(m);

        String[] keywords = inputMol.keywords.split(",");
        List<Long> kids = new ArrayList<>();
        for (String keyword : keywords) {
            kids.add(db.putKeyword(keyword.trim()));
        }

        String[] classes = inputMol.classes.split(",");
        List<Long> clids = new ArrayList<>();
        for (String mclass : classes) {
            clids.add(db.putClass(mclass.trim()));
        }

        db.putKeywordsToMoleculeById(id, kids);
        db.putClassesToMoleculeById(id, clids);

        return ok(gson.toJson(new JsonOk(urls.that("view/" + Molecule.hmid.encode(m.getMid())
                + "/" + Molecule.hid.encode(id))))).asJson();
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
