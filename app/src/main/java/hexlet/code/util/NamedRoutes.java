package hexlet.code.util;

public class NamedRoutes {
    private static final String ROOT_PATH = "/";
    private static final String URLS_BASE_PATH = "/urls/";

    private NamedRoutes() {
        throw new UnsupportedOperationException("Это служебный класс, создание экземпляров запрещено");
    }
    public static String rootPath() {
        return ROOT_PATH;
    }
    public static String urlsPath() {
        return URLS_BASE_PATH;
    }

    public static String urlPath(Long id) {
        return URLS_BASE_PATH + id;
    }

    public static String urlPath(String id) {
        return URLS_BASE_PATH + id;
    }

    public static String urlChecksPath(String urlId) {
        return URLS_BASE_PATH + urlId + "/checks";
    }

    public static String urlChecksPath(Long urlId) {
        return URLS_BASE_PATH + urlId + "/checks";
    }
}
