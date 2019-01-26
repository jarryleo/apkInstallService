package cn.leo.tcp.file;

import java.util.Map;

/**
 * @author : Jarry Leo
 * @date : 2019/1/26 14:42
 */
public interface SendFileListener {
    /**
     * 文件发送进度
     *
     * @param fileProgressMap 文件名对应进度集合
     */
    void onSendFileProgress(Map<String, Integer> fileProgressMap);

    /**
     * 对方接受文件传输
     */
    void onAccept();

    /**
     * 对方拒绝文件传输
     */
    void onDenied();

    /**
     * 文件传输出错
     */
    void onTransferFailed(FileInfo fileinfo);
}
