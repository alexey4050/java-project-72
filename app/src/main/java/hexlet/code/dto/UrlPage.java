package hexlet.code.dto;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import lombok.*;

import java.util.List;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor

public class UrlPage extends BasePage {
    @NonNull private Url url;
    @NonNull private List<UrlCheck> urlChecks;
    private String exampleData;

    public UrlPage(@NonNull Url url, @NonNull List<UrlCheck> urlChecks) {
        this.url = url;
        this.urlChecks = urlChecks;
    }
}
