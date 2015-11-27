package ru.lesqm.db.logic;

import com.bunjlabs.fugaframework.FugaApp;
import com.bunjlabs.fugaframework.configuration.Configuration;
import org.sphx.api.SphinxClient;
import org.sphx.api.SphinxException;
import org.sphx.api.SphinxMatch;
import org.sphx.api.SphinxResult;

public class DatabaseSearch {

    private final Configuration config;
    private final SphinxClient s;

    public DatabaseSearch(FugaApp app) {
        this.config = app.getConfiguration();
        this.s = new SphinxClient(
                config.get("lesqm.sphinx.host", "localhost"),
                config.getInt("lesqm.sphinx.port", 9312));
    }

    public long[] search(String query) {

        try {
            s.setMatchMode(SphinxClient.SPH_MATCH_EXTENDED);

            SphinxResult res = s.query(query, "moleculeIndex");

            long[] result = new long[res.totalFound];
            int i = 0;
            for (SphinxMatch f : res.getMatches()) {
                result[i++] = f.getDocId();
            }

            return result;
        } catch (SphinxException ex) {
            return new long[0];
        }
    }
}
