import dev.mccue.html.Html;
import dev.mccue.html.HtmlEncodable;

import java.util.stream.Stream;

import static dev.mccue.html.Html.HTML;

record CategoryButtons(String moduleName, String category) implements HtmlEncodable {
    @Override
    public Html toHtml() {
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
