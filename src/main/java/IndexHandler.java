import dev.mccue.html.Html;
import dev.mccue.html.HtmlEncodable;
import dev.mccue.jdbc.StatementPreparer;
import dev.mccue.microhttp.handler.RouteHandler;
import dev.mccue.microhttp.html.HtmlResponse;
import dev.mccue.urlparameters.UrlParameters;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.mccue.html.Html.HTML;

class IndexHandler extends RouteHandler {
    private final SQLiteDataSource db;

    IndexHandler(SQLiteDataSource db) {
        super("GET", Pattern.compile("/"));
        this.db = db;
    }

    @Override
    protected HtmlResponse handleRoute(Matcher matcher, Request request) throws Exception {
        var category = UrlParameters.parse(URI.create(request.uri()))
                .firstValue("category")
                .orElse("unorganized");

        var rows = new Rows();
        try (var conn = db.getConnection();
             var stmt = StatementPreparer.of(conn)."""
                     SELECT name, category FROM module
                     WHERE category = \{category}
                     ORDER BY name
                     """) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(new Row(
                        rs.getString("name"),
                        rs.getString("category")
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

    record Row(String name, String category) {
    }

    static final class Rows extends ArrayList<Row> implements HtmlEncodable {
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
}
