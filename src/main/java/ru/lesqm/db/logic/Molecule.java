package ru.lesqm.db.logic;

import java.sql.Timestamp;
import java.util.List;
import org.hashids.Hashids;

public class Molecule {

    public static final Hashids hid = new Hashids("hgydkrjt", 8, "abcdefghijklmnopqrstuvwxyz0123456789");
    public static final Hashids hmid = new Hashids("nvheoptf", 8, "abcdefghijklmnopqrstuvwxyz0123456789");

    private Long id;
    private Long mid;
    private Long uid;
    private String userName;
    private Timestamp date;
    private Integer ctype;
    private String formula;
    private String picture;       // New
    private String baseName;
    private String nomeName;
    private String nomeNameEng;   // New
    private String codeName;      // New
    private List<MClass> mclass;  // New
    private String content;
    private String literature;
    private List<Keyword> keywords;

    public Long getId() {
        return id;
    }

    public Long getMid() {
        return mid;
    }

    public Long getUid() {
        return uid;
    }

    public Timestamp getDate() {
        return date;
    }

    public Integer getCtype() {
        return ctype;
    }

    public String getFormula() {
        return formula;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getNomeName() {
        return nomeName;
    }

    public String getContent() {
        return content;
    }

    public String getLiterature() {
        return literature;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMid(Long mid) {
        this.mid = mid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public void setCtype(Integer ctype) {
        this.ctype = ctype;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public void setNomeName(String nomeName) {
        this.nomeName = nomeName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLiterature(String literature) {
        this.literature = literature;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNomeNameEng() {
        return nomeNameEng;
    }

    public void setNomeNameEng(String nomeNameEng) {
        this.nomeNameEng = nomeNameEng;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public List<MClass> getMClasses() {
        return mclass;
    }

    public void setMClasses(List<MClass> mclass) {
        this.mclass = mclass;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

}
