import org.sqlite.SQLiteDataSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Import {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("module.properties");
        Files.writeString(path, HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(URI.create("https://raw.githubusercontent.com/sormuras/modules/main/com.github.sormuras.modules/com/github/sormuras/modules/modules.properties"))
                        .build(),
                        HttpResponse.BodyHandlers.ofString()).body());
        var properties = new Properties();
        try (var is = Files.newInputStream(path)) {
            properties.load(is);
        }

        var db = new SQLiteDataSource();
        db.setUrl("jdbc:sqlite:modules.db");

        try (var conn = db.getConnection()) {
            for (var entry : properties.entrySet()) {
                try (var stmt = conn.prepareStatement("""
                    INSERT OR IGNORE INTO module(name) VALUES (?)
                    """)) {
                    stmt.setObject(1, entry.getKey());
                    stmt.execute();
                }
                try (var stmt = conn.prepareStatement("""
                    UPDATE module SET latest_file_url = ? WHERE name = ?
                    """)) {
                    stmt.setObject(1, entry.getValue());
                    stmt.setObject(2, entry.getKey());
                    stmt.execute();
                }
            }
        }
    }
}
