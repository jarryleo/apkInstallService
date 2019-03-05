package cn.leo.tcp.download;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author : Jarry Leo
 * @date : 2019/3/5 9:57
 */
public class URIWrapper {
    private String url;
    private String host;
    private String path;
    private String query;
    private String scheme;
    private int port = -1;


    public URIWrapper(String url) {
        this.url = url;
        parseUrl();
    }


    private void parseUrl() {
        if (url == null || url.trim().length() == 0) {
            return;
        }
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        host = uri.getHost();
        port = uri.getPort();
        scheme = uri.getScheme();
        path = uri.getPath();
        query = uri.getQuery();
        if (port < 0) {
            if (url.startsWith("https")) {
                this.port = 443;
            } else {
                port = 80;
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public String getScheme() {
        return scheme;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public int getPort() {
        return port;
    }
}
