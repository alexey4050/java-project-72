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
            throw new MalformedURLException("URL должен начинаться с http:// или https://");
        }

        URI uri = new URI(urlString);
        URL url = uri.toURL();

        if (url.getHost() == null || url.getHost().isBlank()) {
            throw new MalformedURLException("Некорректный URL: отсутствует host");
        }

        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();

        StringBuilder normalized = new StringBuilder();
        normalized.append(protocol).append("://").append(host);

        if (port != -1 && !((protocol.equals("http") && port == 80)
                || (protocol.equals("https") && port == 443))) {
            normalized.append(":").append(port);
        }

        return normalized.toString();
    }
}
