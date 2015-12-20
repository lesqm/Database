package ru.lesqm.db.utils;

public class JsonError {

    public JsonError() {

    }

    public JsonError(String msg) {
        this.msg = msg;
    }
    public String status = "error";
    public String msg;
}
