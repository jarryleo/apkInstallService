package cn.leo.tcp.file;

import java.util.Map;

/**
 * @author : Jarry Leo
 * @date : 2019/1/26 14:33
 */
public interface ReceiveFileListener {
    /**
     * 新文件传输请求
     *
     * @param callback
     */
    void onNewFile(FileInfo fileBean, NewFileRequest callback);

    /**
     * 文件传输进度
     *
     * @param fileProgressMap 文件名对应进度集合
     */
    void onFilesProgress(Map<String, Integer> fileProgressMap);

    /**
     * 文件传输出错
     */
    void onTransferFailed(FileInfo fileinfo);
}
