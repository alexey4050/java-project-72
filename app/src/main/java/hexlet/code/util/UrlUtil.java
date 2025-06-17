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
        if (urlString == null || urlString.isBlank()) {
            throw new MalformedURLException("URL не может быть пустым");
        }

        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            urlString = "http://" + urlString;
        }

        URI uri = new URI(urlString).parseServerAuthority();
        URL url = uri.toURL();

        if (url.getHost() == null || url.getHost().isBlank()) {
            throw new MalformedURLException("Некорректный URL: отсутствует host");
        }

        String protocol = url.getProtocol().toLowerCase();
        String host = url.getHost().toLowerCase();
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
