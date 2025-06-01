package hexlet.code.dto;

import hexlet.code.model.Url;

import lombok.Getter;

import java.util.List;

@Getter

public class UrlsPage extends BasePage {
    private List<Url> urls;

    public UrlsPage(List<Url> urls) {
        this.urls = urls;
    }
}
