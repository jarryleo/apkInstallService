package cn.leo.tcp.download;

/**
 * @author : Jarry Leo
 * @date : 2019/2/26 8:38
 */
public interface DownLoadManager {
    void download(String path, String dir) throws Exception;
}
