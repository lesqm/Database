package ru.lesqm.db;

import com.bunjlabs.fugaframework.FugaApp;
import ru.lesqm.db.services.DatabaseService;

public class LesqmDatabaseApp extends FugaApp {

    @Override
    public void prepare() {

        getRouter().load("default.froutes");
        getConfiguration().load("default.properties");

        getServiceManager().register(DatabaseService.class);
    }

    public static void main(String args[]) throws Exception {
        launch(LesqmDatabaseApp.class);
    }
}
