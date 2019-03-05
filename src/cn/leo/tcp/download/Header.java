package cn.leo.tcp.download;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author : Jarry Leo
 * @date : 2019/3/5 10:51
 */
public class Header {
    private static final String[] methods = {
            "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
    };
    private StringBuilder sb = new StringBuilder();
    private URIWrapper uriWrapper;
    private String mMethod = "GET";
    private String httpVersion = "HTTP/1.1";
    private String space = " ";
    private String newLine = "\r\n";
    private Map<String, String> requestPropertys = new HashMap<>();

    public void setUrl(String url) {
        uriWrapper = new URIWrapper(url);
    }

    public void setMethod(String method) {
        String s = method.toUpperCase();
        boolean contains = Arrays.asList(methods).contains(s);
        if (contains) {
            mMethod = s;
        }
    }

    public void addRequestProperty(String key, String value) {
        requestPropertys.put(key, value);
    }


    private void createPostMethod() {
        //请求头第一行
        sb
                .append(mMethod)
                .append(space)
                .append(uriWrapper.getPath())
                .append(space)
                .append(httpVersion)
                .append(newLine);
        //请求头第二行，POST 内容长度
        if (uriWrapper.getQuery() != null) {
            int length = uriWrapper.getQuery().length();
            addRequestProperty("Length", String.valueOf(length));
        }


    }

    private void createGetMethod() {
        String path = uriWrapper.getPath();
        if (uriWrapper.getQuery() != null) {
            path = path.concat("?").concat(uriWrapper.getQuery());
        }
        //请求头第一行
        sb
                .append(mMethod)
                .append(space)
                .append(path)
                .append(space)
                .append(httpVersion)
                .append(newLine);
    }

    public byte[] getHeaderBytes() {
        checkUrl();
        if ("POST".equals(mMethod)) {
            createPostMethod();
            appendRequestProperty();
            sb
                    .append(newLine)
                    .append(uriWrapper.getQuery())
                    .append(newLine)
                    .append(newLine);
        } else {
            createGetMethod();
            appendRequestProperty();
            sb.append(newLine);
        }
        byte[] bytes = null;
        try {
            String s = sb.toString();
            System.out.println(s);
            bytes = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.delete(0, sb.length());
        return bytes;
    }

    private void appendRequestProperty() {
        for (Map.Entry<String, String> entry : requestPropertys.entrySet()) {
            sb.append(entry.getKey())
                    .append(":")
                    .append(entry.getValue())
                    .append(newLine);
        }
    }

    public String getHost() {
        checkUrl();
        return uriWrapper.getHost();
    }

    public int getPort() {
        checkUrl();
        return uriWrapper.getPort();
    }

    private void checkUrl() {
        if (uriWrapper == null) {
            throw new IllegalArgumentException("url is null");
        }
    }

    public boolean isHttps() {
        checkUrl();
        return "https".equals(uriWrapper.getScheme());
    }
}
