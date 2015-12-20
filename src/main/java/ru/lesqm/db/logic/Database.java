package ru.lesqm.db.logic;

import com.bunjlabs.fugaframework.FugaApp;
import com.bunjlabs.fugaframework.configuration.Configuration;
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
                = "SELECT * FROM molecule "
                + "WHERE MATCH (formula, baseName, codeName, nomeName, nomeNameEng) AGAINST(:query IN BOOLEAN MODE) "
                + "OR molecule.id IN ( "
                + "  SELECT keywords_bindings.cid "
                + "  FROM keywords_bindings "
                + "  WHERE kid IN ( "
                + "    SELECT keywords.id "
                + "    FROM keywords "
                + "    WHERE MATCH(name) AGAINST(:query IN BOOLEAN MODE) "
                + "  ) "
                + ") "
                + "OR molecule.id IN ( "
                + "  SELECT classes_bindings.cid "
                + "  FROM classes_bindings "
                + "  WHERE clid IN ( "
                + "    SELECT classes.id "
                + "    FROM classes "
                + "    WHERE MATCH(name) AGAINST(:query IN BOOLEAN MODE) "
                + "  ) "
                + ") "
                + "LIMIT 50";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("query", query)
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
                = "SELECT * "
                + "FROM molecule "
                + "WHERE id = :id";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Molecule.class);
        }
    }

    public List<Log> getChangesAll() {
        String sql
                = "SELECT logs.*, users.name AS userName, molecule.formula, molecule.baseName, molecule.nomeName "
                + "FROM logs "
                + "LEFT JOIN users ON logs.uid = users.id "
                + "LEFT JOIN molecule ON logs.mid = molecule.id "
                + "ORDER BY date DESC "
                + "LIMIT 10";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(Log.class);
        }
    }

    public void putChange(long uid, long mid, int type) {
        String sql
                = "INSERT INTO logs "
                + "VALUES (NULL, :type, :uid, NULL, :mid)";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("uid", uid)
                    .addParameter("mid", mid)
                    .addParameter("type", type)
                    .executeUpdate();
        }
    }

    public long putMolecule(Molecule m) {
        String sql
                = "INSERT INTO molecule "
                + "VALUES (NULL, :formula, :picture, :baseName, :codeName, :nomeName, :nomeNameEng, :content, :literature)";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).bind(m).executeUpdate().getKey(Long.class);
        }
    }

    public void updateMolecule(Molecule m) {
        String sql
                = "UPDATE molecule SET "
                + "formula = :formula, "
                + "picture = :picture, "
                + "baseName = :baseName, "
                + "codeName = :codeName, "
                + "nomeName = :nomeName, "
                + "nomeNameEng = :nomeNameEng, "
                + "content = :content, "
                + "literature = :literature "
                + "WHERE id = :id";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).bind(m).executeUpdate();
        }
    }

    public void deleteMolecule(long id) {
        String sql
                = "DELETE FROM molecule "
                + "WHERE id = :id";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).addParameter("id", id).executeUpdate();
        }
    }

    public void deleteKeywordBindings(long id) {
        String sql
                = "DELETE FROM keywords_bindings "
                + "WHERE cid = :id";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).addParameter("id", id).executeUpdate();
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

    public void deleteClassBindings(long id) {
        String sql
                = "DELETE FROM classes_bindings "
                + "WHERE cid = :id";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).addParameter("id", id).executeUpdate();
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

        sql = "SELECT * FROM classes WHERE name = :name";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("name", name)
                    .executeAndFetchFirst(Keyword.class).getId();
        }
    }

    public List<User> getUserAll() {
        String sql
                = "SELECT * "
                + "FROM users ";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(User.class);
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
