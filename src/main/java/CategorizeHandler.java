import dev.mccue.jdbc.SettableParameter;
import dev.mccue.jdbc.StatementPreparer;
import dev.mccue.microhttp.handler.RouteHandler;
import dev.mccue.microhttp.html.HtmlResponse;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.mccue.html.Html.HTML;

class CategorizeHandler extends RouteHandler {
    private final SQLiteDataSource db;

    CategorizeHandler(SQLiteDataSource db) {
        super("POST", Pattern.compile("/categorize/(?<category>.+)/(?<moduleName>.+)"));
        this.db = db;
    }

    @Override
    protected HtmlResponse handleRoute(Matcher matcher, Request request) throws Exception {
        var category = matcher.group("category");
        var moduleName = matcher.group("moduleName");

        try (var conn = db.getConnection();
             var stmt = StatementPreparer.of(conn)."""
                     UPDATE module
                     SET category = \{category}
                     WHERE name = \{moduleName}
                     """) {
            stmt.execute();

            return new HtmlResponse(HTML."");
        }
    }
}
