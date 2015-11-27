package ru.lesqm.db.logic;

import com.bunjlabs.fugaframework.FugaApp;
import com.bunjlabs.fugaframework.configuration.Configuration;
import java.util.ArrayList;
import java.util.List;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Database {

    private final Configuration config;
    private final Sql2o sql2o;

    public Database(FugaApp app) {
        this.config = app.getConfiguration();
        this.sql2o = new Sql2o(
                config.get("lesqm.db.path", "undefined"),
                config.get("lesqm.db.user", "undefined"),
                config.get("lesqm.db.password", "undefined"));
    }

    public List<Molecule> searchMilecule(String query) {
        String sql
                = "SELECT molecule.*, users.name AS userName "
                + "FROM molecule "
                + "LEFT JOIN users ON molecule.uid = users.id "
                + "WHERE formula LIKE concat('%', :query, '%') "
                + "OR baseName LIKE concat('%', :query, '%') "
                + "OR nomeName LIKE concat('%', :query, '%') "
                + "OR molecule.id IN ( "
                + "  SELECT keywords_bindings.cid "
                + "  FROM keywords_bindings "
                + "  WHERE kid IN ( "
                + "    SELECT keywords.id "
                + "    FROM keywords "
                + "    WHERE name LIKE concat('%', :query, '%') "
                + "  ) "
                + ") "
                + "ORDER BY date DESC LIMIT 50";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("query", query)
                    .executeAndFetch(Molecule.class);
        }

    }

    public List<Molecule> getMoleculeByIds(long[] ids) {
        if (ids.length == 0) {
            return new ArrayList<>();
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < ids.length; i++) {
            if (i == 0) {
                sb.append(ids[i]);
            } else {
                sb.append(", ").append(ids[i]);
            }
        }

        String sql
                = "SELECT molecule.*, users.name AS userName "
                + "FROM molecule "
                + "LEFT JOIN users ON molecule.uid = users.id "
                + "WHERE molecule.id IN (" + sb.toString() + ") "
                + "ORDER BY date DESC";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    //.addParameter("mid", mid)
                    .executeAndFetch(Molecule.class);
        }
    }

    public List<Keyword> getMoleculeKeywords(long id) {
        String sql
                = "SELECT * "
                + "FROM keywords WHERE id IN "
                + "(SELECT kid FROM keywords_bindings WHERE cid = :id)";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Keyword.class);
        }
    }

    public List<MClass> getMoleculeClasses(long id) {
        String sql
                = "SELECT * "
                + "FROM classes WHERE id IN "
                + "(SELECT clid FROM classes_bindings WHERE cid = :id)";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(MClass.class);
        }
    }

    public Molecule getMoleculeById(long id) {
        String sql
                = "SELECT molecule.*, users.name AS userName "
                + "FROM molecule "
                + "LEFT JOIN users ON molecule.uid = users.id "
                + "WHERE molecule.id = :id "
                + "ORDER BY date DESC";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Molecule.class);
        }
    }

    public Molecule getMoleculeByMidAndId(long mid, long id) {
        String sql
                = "SELECT molecule.*, users.name AS userName "
                + "FROM molecule "
                + "LEFT JOIN users ON molecule.uid = users.id "
                + "WHERE mid = :mid AND molecule.id = :id "
                + "ORDER BY date DESC";

        try (Connection con = sql2o.open()) {
            Molecule m = con.createQuery(sql)
                    .addParameter("mid", mid)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Molecule.class);
            return m.getCtype() == 3 ? null : m;
        }
    }

    public Molecule getMoleculeByMid(long mid) {
        String sql
                = "SELECT molecule.*, users.name AS userName "
                + "FROM molecule "
                + "LEFT JOIN users ON molecule.uid = users.id "
                + "WHERE mid = :mid "
                + "ORDER BY date DESC LIMIT 1";

        try (Connection con = sql2o.open()) {
            Molecule m = con.createQuery(sql)
                    .addParameter("mid", mid)
                    .executeAndFetchFirst(Molecule.class);
            return m.getCtype() == 3 ? null : m;
        }
    }

    public List<Molecule> getMoleculeByMidAll(long mid) {
        String sql
                = "SELECT molecule.*, users.name AS userName "
                + "FROM molecule "
                + "LEFT JOIN users ON molecule.uid = users.id "
                + "WHERE mid = :mid "
                + "ORDER BY date DESC";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("mid", mid)
                    .executeAndFetch(Molecule.class);
        }
    }

    public List<Molecule> getMoleculeByUid(long uid) {
        String sql
                = "SELECT molecule.*, users.name AS userName "
                + "FROM molecule "
                + "LEFT JOIN users ON molecule.uid = users.id "
                + "WHERE uid = :uid "
                + "ORDER BY date DESC";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("uid", uid)
                    .executeAndFetch(Molecule.class);
        }
    }

    public List<Molecule> getMoleculeChangesAll() {
        String sql
                = "SELECT molecule.*, users.name AS userName "
                + "FROM molecule "
                + "LEFT JOIN users ON molecule.uid = users.id "
                + "ORDER BY date DESC "
                + "LIMIT 100";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(Molecule.class);
        }
    }

    public long putMoleculeChange(Molecule m) {
        String sql
                = "INSERT INTO molecule(mid, uid, date, formula, picture, baseName, codeName, nomeName, nomeNameEng, ctype, content, literature) "
                + "VALUES (:mid, :uid, now(), :formula, :picture, :baseName, :codeName, :nomeName, :nomeNameEng, 1, :content, :literature)";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).bind(m).executeUpdate().getKey(Long.class);
        }
    }

    public long putMolecule(Molecule m) {
        String sql
                = "INSERT INTO molecule(mid, uid, date, formula, picture, baseName, codeName, nomeName, nomeNameEng, ctype, content, literature) "
                + "SELECT MAX(mid) + 1, :uid, now(), :formula, :picture, :baseName, :codeName, :nomeName, :nomeNameEng, 0, :content, :literature FROM molecule";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).bind(m).executeUpdate().getKey(Long.class);
        }
    }

    public long putMoleculeDelete(Molecule m) {
        String sql
                = "INSERT INTO molecule(mid, uid, date, ctype) "
                + "VALUES (:mid, :uid, now(), 3)";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).bind(m).executeUpdate().getKey(Long.class);
        }
    }

    public long putMoleculeRevert(Molecule m) {
        String sql
                = "INSERT INTO molecule(mid, uid, date, formula, picture, baseName, codeName, nomeName, nomeNameEng, ctype, content, literature) "
                + "VALUES (:mid, :uid, now(), :formula, :picture, :baseName, :codeName, :nomeName, :nomeNameEng, 2, :content, :literature)";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).bind(m).executeUpdate().getKey(Long.class);
        }
    }

    public void putKeywordsToMoleculeById(long id, List<Long> kids) {
        String sql
                = "INSERT INTO keywords_bindings(cid, kid) "
                + "VALUES (:cid, :kid)";

        try (Connection con = sql2o.beginTransaction()) {
            kids.stream().forEach((kid) -> {
                con.createQuery(sql)
                        .addParameter("cid", id)
                        .addParameter("kid", kid)
                        .executeUpdate().getKey(Long.class);
            });
            con.commit();
        }
    }

    public long putKeyword(String name) {
        String sql
                = "INSERT INTO keywords (name) "
                + "SELECT * FROM (SELECT :name) AS tmp "
                + "WHERE NOT EXISTS ( "
                + "    SELECT name FROM keywords WHERE name = :name "
                + ") LIMIT 1;";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("name", name)
                    .executeUpdate();

        }
        sql
                = "SELECT * FROM keywords WHERE name = :name";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("name", name)
                    .executeAndFetchFirst(Keyword.class).getId();
        }
    }

    public void putClassesToMoleculeById(long id, List<Long> cids) {
        String sql
                = "INSERT INTO classes_bindings(cid, clid) "
                + "VALUES (:cid, :clid)";

        try (Connection con = sql2o.beginTransaction()) {
            cids.stream().forEach((cid) -> {
                con.createQuery(sql)
                        .addParameter("cid", id)
                        .addParameter("clid", cid)
                        .executeUpdate().getKey(Long.class);
            });
            con.commit();
        }
    }

    public long putClass(String name) {
        String sql
                = "INSERT INTO classes (name) "
                + "SELECT * FROM (SELECT :name) AS tmp "
                + "WHERE NOT EXISTS ( "
                + "    SELECT name FROM classes WHERE name = :name "
                + ") LIMIT 1;";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("name", name)
                    .executeUpdate();

        }
        sql
                = "SELECT * FROM classes WHERE name = :name";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("name", name)
                    .executeAndFetchFirst(Keyword.class).getId();
        }
    }

    public User getUserById(long id) {
        String sql
                = "SELECT * "
                + "FROM users "
                + "WHERE id = :id";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(User.class);
        }
    }

    public User getUserByEmail(String email) {
        String sql
                = "SELECT * "
                + "FROM users "
                + "WHERE email = :email";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("email", email)
                    .executeAndFetchFirst(User.class);
        }
    }
}
