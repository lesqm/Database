package ru.lesqm.db.utils;

public class JsonOkUrl {

    public JsonOkUrl(String url) {
        this.url = url;
    }

    public JsonOkUrl(String url, String msg) {
        this.url = url;
        this.msg = msg;
    }

    public String status = "ok";
    public String msg;
    public String url;
}
