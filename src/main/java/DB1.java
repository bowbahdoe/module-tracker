import org.sqlite.SQLiteDataSource;

public class DB1 {
    public static void main(String[] args) throws Exception {

        var db = new SQLiteDataSource();
        db.setUrl("jdbc:sqlite:modules.db");

        try (var conn = db.getConnection()) {
            try (var stmt = conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS module(
                       name text not null unique,
                       latest_file_url text,
                       github_url text,
                       stupid_name boolean not null default false,
                       framework text,
                       reviewed boolean not null default false,
                       narrow_focus boolean not null default false,
                       category text not null default 'unorganized',
                       notes text not null default ''
                    )
                    """)) {
                stmt.execute();
            }
        }
    }
}
