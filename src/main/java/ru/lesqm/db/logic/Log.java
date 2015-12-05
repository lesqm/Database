package ru.lesqm.db.logic;

import java.sql.Timestamp;

public class Log {

    private Long id;
    private Long type;
    private Long uid;
    private Long mid;
    private String userName;
    private Timestamp date;
    private String formula;
    private String baseName;
    private String nomeName;

    public Long getId() {
        return id;
    }

    public Long getType() {
        return type;
    }

    public Long getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public Timestamp getDate() {
        return date;
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

    public void setType(Long type) {
        this.type = type;
    }

    public void setUid(Long uid) {
        this.uid = uid;
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

    public Long getMid() {
        return mid;
    }

    public void setMid(Long mid) {
        this.mid = mid;
    }

}
