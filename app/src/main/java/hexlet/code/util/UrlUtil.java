package hexlet.code.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UrlUtil {
    private UrlUtil() {
        throw new UnsupportedOperationException("Это служебный класс, создание экземпляров запрещено");
    }
    public static String normalizeUrl(String urlString) throws URISyntaxException, MalformedURLException {
        URI uri = new URI(urlString);
        URL url = uri.toURL();

        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();

        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://").append(host);
        if (port != -1) {
            sb.append(":").append(port);
        }
        return sb.toString();
    }
}
