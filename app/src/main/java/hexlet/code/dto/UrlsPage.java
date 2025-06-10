package hexlet.code.dto;

import hexlet.code.model.Url;

import hexlet.code.model.UrlCheck;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter

public final class UrlsPage extends BasePage {
    private List<Url> urls;
    private Map<Long, UrlCheck> lastChecks;

    public UrlsPage(List<Url> urls, Map<Long, UrlCheck> lastChecks) {
        this.urls = urls;
        this.lastChecks = lastChecks;
    }
}
