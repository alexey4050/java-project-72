package hexlet.code.util;

public class NamedRoutes {
    private static final String URLS_BASE_PATH = "/urls/";
    public static String rootPath() {
        return "/";
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
