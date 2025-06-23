package hexlet.code.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class UrlUtil {
    private UrlUtil() {
        throw new UnsupportedOperationException("Это служебный класс, создание экземпляров запрещено");
    }

    public static String normalizeUrl(String urlString) throws URISyntaxException, MalformedURLException {

        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            urlString = "http://" + urlString;
        }

        URI uri = new URI(urlString);
        URL parsedUrl = uri.toURL();

        if (parsedUrl.getHost() == null || parsedUrl.getHost().isBlank()) {
            throw new MalformedURLException("Некорректный URL: отсутствует host");
        }

        // Форматируем нормализованный URL
        String normalizedUrl = String.format(
                "%s://%s%s",
                parsedUrl.getProtocol(),
                parsedUrl.getHost(),
                parsedUrl.getPort() == -1 ? "" : ":" + parsedUrl.getPort()
        ).toLowerCase();

        return normalizedUrl;
    }
}
