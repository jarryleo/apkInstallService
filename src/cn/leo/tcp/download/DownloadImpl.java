package cn.leo.tcp.download;

/**
 * @author : Jarry Leo
 * @date : 2019/2/26 8:40
 */
public class DownloadImpl implements DownLoadManager {
    @Override
    public void download(String path, String dir) throws Exception {
        Request request = new Request(path);
        request.execute();
    }
}
