package ru.lesqm.db;

import com.bunjlabs.fugaframework.FugaApp;
import ru.lesqm.db.logic.Database;

public class LesqmDatabaseApp extends FugaApp {

    private Database database;

    @Override
    public void prepare() {

        getRouter().load("routes/main.routes");
        getConfiguration().load("config/default.properties");

        database = new Database(this);
    }
    
    public static void main(String args[]) throws Exception {
        new LesqmDatabaseApp().start();
    }

    public Database getDatabase() {
        return database;
    }
}
