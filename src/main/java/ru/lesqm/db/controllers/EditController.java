package ru.lesqm.db.controllers;

import com.bunjlabs.fugaframework.dependency.Inject;
import com.bunjlabs.fugaframework.foundation.Controller;
import com.bunjlabs.fugaframework.foundation.Response;
import com.bunjlabs.fugaframework.sessions.Session;
import com.bunjlabs.fugaframework.templates.TemplateNotFoundException;
import com.bunjlabs.fugaframework.templates.TemplateRenderException;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import ru.lesqm.db.services.DatabaseService;
import ru.lesqm.db.logic.Keyword;
import ru.lesqm.db.logic.MClass;
import ru.lesqm.db.logic.Molecule;
import ru.lesqm.db.logic.MoleculeJson;
import ru.lesqm.db.logic.User;
import ru.lesqm.db.utils.JsonOkUrl;

public class EditController extends Controller {

    private static final Gson gson = new Gson();

    private final DatabaseService db;

    @Inject
    public EditController(DatabaseService db) {
        this.db = db;
    }

    public Response add() throws TemplateNotFoundException, TemplateRenderException {
        return ok(view("add.html"));
    }

    public Response addProcess() throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        MoleculeJson inputMol = gson.fromJson(ctx.getRequest().getContent().asString(), MoleculeJson.class);

        Molecule m = new Molecule();

        m.setFormula(inputMol.formula);
        m.setPicture(inputMol.pictureUrl);
        m.setBaseName(inputMol.baseName);
        m.setNomeName(inputMol.nomeName);
        m.setNomeNameEng(inputMol.nomeNameEng);
        m.setCodeName(inputMol.codeName);
        m.setContent(inputMol.content);
        m.setLiterature(inputMol.literature);

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

        db.putChange(((User) session.get("user")).getId(), id, 0);

        return ok(gson.toJson(new JsonOkUrl(urls.that("view/" + Molecule.hmid.encode(id))))).asJson();
    }

    public Response edit(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeById(mid[0])) == null) {
            return notFound();
        }

        List<Keyword> keywords = db.getMoleculeKeywords(m.getId());
        List<MClass> mClasses = db.getMoleculeClasses(m.getId());

        m.setKeywords(keywords == null ? new ArrayList<>() : keywords);
        m.setMClasses(mClasses == null ? new ArrayList<>() : mClasses);

        ctx.put("m", m);

        return ok(view("edit.html"));
    }

    public Response editProcess(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeById(mid[0])) == null) {
            return notFound();
        }

        MoleculeJson inputMol = gson.fromJson(ctx.getRequest().getContent().asString(), MoleculeJson.class);

        m.setFormula(inputMol.formula);
        m.setPicture(inputMol.pictureUrl);
        m.setBaseName(inputMol.baseName);
        m.setNomeName(inputMol.nomeName);
        m.setNomeNameEng(inputMol.nomeNameEng);
        m.setCodeName(inputMol.codeName);
        m.setContent(inputMol.content);
        m.setLiterature(inputMol.literature);

        db.updateMolecule(m);

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

        db.deleteKeywordBindings(m.getId());
        db.putKeywordsToMoleculeById(m.getId(), kids);

        db.deleteClassBindings(m.getId());
        db.putClassesToMoleculeById(m.getId(), clids);

        db.putChange(((User) session.get("user")).getId(), m.getId(), 1);

        return ok(gson.toJson(new JsonOkUrl(urls.that("view", Molecule.hmid.encode(m.getId()))))).asJson();
    }

    public Response deleteProcess(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();

        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;

        if (mid.length == 0 || (m = db.getMoleculeById(mid[0])) == null) {
            return notFound();
        }

        db.deleteMolecule(m.getId());
        db.deleteKeywordBindings(m.getId());
        db.deleteClassBindings(m.getId());

        db.putChange(((User) session.get("user")).getId(), 0, 2);

        return redirect(urls.that());
    }
}
