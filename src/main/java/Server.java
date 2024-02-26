import dev.mccue.html.Html;
import dev.mccue.html.HtmlEncodable;
import dev.mccue.jdbc.ResultSets;
import dev.mccue.jdbc.StatementPreparer;
import dev.mccue.microhttp.handler.DelegatingHandler;
import dev.mccue.microhttp.handler.RouteHandler;
import dev.mccue.microhttp.html.HtmlResponse;
import dev.mccue.urlparameters.UrlParameters;
import org.microhttp.EventLoop;
import org.microhttp.Options;
import org.microhttp.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dev.mccue.html.Html.HTML;

record CategoryButtons(String moduleName, String category) implements HtmlEncodable {
    @Override
    public Html toHtml() {
        var buttons = new ArrayList<Html>();
        record CategoryButton(String moduleName, String categoryTitle, String category)
                implements HtmlEncodable {
            @Override
            public Html toHtml() {
                return HTML."""
                <button
                    hx-post="/categorize/\{category}/\{moduleName}"
                    hx-target="#\{moduleName.replace(".", "-")}"
                    hx-swap="outerHTML">
                  \{categoryTitle}
                </button>
            """;
            }
        }

        return Html.of(
                Stream.of(
                        new CategoryButton(moduleName, "Unorganized", "unorganized"),
                        new CategoryButton(moduleName, "Bad", "bad"),
                        new CategoryButton(moduleName, "Interesting", "interesting"),
                        new CategoryButton(moduleName, "Boring", "boring"),
                        new CategoryButton(moduleName, "Amateur", "amateur"),
                        new CategoryButton(moduleName, "Framework", "framework"),
                        new CategoryButton(moduleName, "Good", "good"),
                        new CategoryButton(moduleName, "Mine", "mine"),
                        new CategoryButton(moduleName, "Cloud Provider", "cloud_provider"),
                        new CategoryButton(moduleName, "JavaFX", "javafx"),
                        new CategoryButton(moduleName, "Crypto Bullshit", "crypto_bullshit")
                )
                .filter(button -> !button.category.equals(category))
                .toList()
        );
    }
}

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

class IndexHandler extends RouteHandler {
    private final SQLiteDataSource db;

    IndexHandler(SQLiteDataSource db) {
        super("GET", Pattern.compile("/"));
        this.db = db;
    }

    @Override
    protected HtmlResponse handleRoute(Matcher matcher, Request request) throws Exception {

        record Row(String name, String category, boolean reviewed, boolean stupidName) {
        }

        class Rows extends ArrayList<Row> implements HtmlEncodable {
            @Override
            public Html toHtml() {
                return HTML."""
                        <table role="grid">
                          <thead style="position: sticky; top: 0;">
                            <tr>
                              <th scope="col" style="position: sticky; top: 0;">Module</th>
                              <th scope="col" style="position: sticky; top: 0;">Categorize</th>
                            </tr>
                          </thead>
                          <tbody>
                            \{stream().map(row -> HTML."""
                               <tr id="\{row.name.replace(".", "-")}">
                                 <th scope="row"> \{row.name} </th>
                                 <td> \{new CategoryButtons(row.name, row.category)} </td>
                               </tr>
                               """)}
                          </tbody>
                        </table>
                        """;
            }
        }

        var category = UrlParameters.parse(URI.create(request.uri()))
                .firstValue("category")
                .orElse("unorganized");

        var rows = new Rows();
        try (var conn = db.getConnection();
             var stmt = StatementPreparer.of(conn)."""
                     SELECT name, category, reviewed, stupid_name FROM module
                     WHERE category = \{category}
                     ORDER BY name asc
                     """) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(new Row(
                        rs.getString("name"),
                        rs.getString("category"),
                        ResultSets.getBooleanNotNull(rs, "reviewed"),
                        ResultSets.getBooleanNotNull(rs, "stupid_name")
                ));
            }
        }

        return new HtmlResponse(HTML."""
                <html data-theme="light" lang="en">
                  <head>
                    <script src="https://unpkg.com/htmx.org@1.9.9"></script>
                    <title>Module Tracker</title>
                  </head>
                  <body>
                    <ul>
                      \{rows}
                    </ul>
                  </body>
                </html>
        """);
    }
}

public class Server {
    static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        var notFound = new HtmlResponse(404, HTML."Not Found");
        var error = new HtmlResponse(500, HTML."Internal Server Error");

        var db = new SQLiteDataSource();
        db.setUrl("jdbc:sqlite:modules.db");

        var handlers = List.of(
                new IndexHandler(db),
                new CategorizeHandler(db)
        );

        var rootHandler = new DelegatingHandler(handlers, notFound);
        var eventLoop = new EventLoop(
                Options.builder().withPort(5544).build(),
                (request, consumer) -> {
                    var requestId = UUID.randomUUID();
                    Thread.ofVirtual()
                            .name(requestId.toString())
                            .start(() -> {
                                try {
                                    LOG.info("method={} uri={}", request.method(), request.uri());
                                    consumer.accept(rootHandler.handle(request).intoResponse());
                                } catch (Exception e) {
                                    LOG.error("Internal Server Error", e);
                                    consumer.accept(error.intoResponse());
                                }
                            });
                });
        eventLoop.start();
        eventLoop.join();

    }
}
