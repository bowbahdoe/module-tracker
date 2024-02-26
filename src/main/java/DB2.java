import org.sqlite.SQLiteDataSource;

public class DB2 {
    public static void main(String[] args) throws Exception {
        var db = new SQLiteDataSource();
        db.setUrl("jdbc:sqlite:modules.db");

        try (var conn = db.getConnection()) {
            try (var stmt = conn.prepareStatement("""
                    ALTER TABLE module DROP COLUMN stupid_name;
                    """)) {
                stmt.execute();
            }
            try (var stmt = conn.prepareStatement("""
                    ALTER TABLE module DROP COLUMN github_url;
                    """)) {
                stmt.execute();
            }
            try (var stmt = conn.prepareStatement("""
                    ALTER TABLE module DROP COLUMN framework;
                    """)) {
                stmt.execute();
            }
            try (var stmt = conn.prepareStatement("""
                    ALTER TABLE module DROP COLUMN narrow_focus;
                    """)) {
                stmt.execute();
            }
            try (var stmt = conn.prepareStatement("""
                    ALTER TABLE module DROP COLUMN notes;
                    """)) {
                stmt.execute();
            }

            /*

                       github_url text,
                       stupid_name boolean not null default false,
                       framework text,
                       reviewed boolean not null default false,
                       narrow_focus boolean not null default false,
                       notes text not null default ''
             */
        }
    }
}
