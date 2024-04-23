import dev.mccue.microhttp.handler.DelegatingHandler;
import dev.mccue.microhttp.html.HtmlResponse;
import org.microhttp.EventLoop;
import org.microhttp.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import java.util.List;
import java.util.UUID;

import static dev.mccue.html.Html.HTML;

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
