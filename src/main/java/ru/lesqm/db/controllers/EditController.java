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
        return ok(view("add.html"));
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
        
        db.putChange((long) session.get("user-id"), id, 0);
        
        return ok(gson.toJson(new JsonOk(urls.that("view/" + Molecule.hmid.encode(id))))).asJson();
    }
    
    public Response edit(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();
        
        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;
        
        if (mid.length == 0 || (m = db.getMoleculeById(mid[0])) == null) {
            return notFoundDefault(ctx);
        }
        
        List<Keyword> keywords = db.getMoleculeKeywords(m.getId());
        List<MClass> mClasses = db.getMoleculeClasses(m.getId());
        
        m.setKeywords(keywords == null ? new ArrayList<>() : keywords);
        m.setMClasses(mClasses == null ? new ArrayList<>() : mClasses);
        
        return ok(view("edit.html", m));
    }
    
    public Response editProcess(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();
        
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();
        
        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;
        
        if (mid.length == 0 || (m = db.getMoleculeById(mid[0])) == null) {
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
        
        db.putChange((long) session.get("user-id"), m.getId(), 1);
        
        return ok(gson.toJson(new JsonOk(urls.that("view/" + Molecule.hmid.encode(m.getId()))))).asJson();
    }
    
    public Response deleteProcess(String hmid) throws TemplateNotFoundException, TemplateRenderException {
        Session session = ctx.getSession();
        
        final Database db = ((LesqmDatabaseApp) ctx.getApp()).getDatabase();
        
        long[] mid = Molecule.hmid.decode(hmid.toLowerCase());
        Molecule m = null;
        
        if (mid.length == 0 || (m = db.getMoleculeById(mid[0])) == null) {
            return notFoundDefault(ctx);
        }
        
        db.deleteMolecule(m.getId());
        db.deleteKeywordBindings(m.getId());
        db.deleteClassBindings(m.getId());
        
        db.putChange((long) session.get("user-id"), 0, 2);
        
        return redirect(urls.that());
    }
}
