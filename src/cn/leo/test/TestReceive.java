package cn.leo.test;

import cn.leo.tcp.file.FileTransfer;
import cn.leo.tcp.file.SimpleReceiveFileListener;

import java.io.File;
import java.util.Map;

/**
 * @author : Jarry Leo
 * @date : 2019/2/11 14:52
 */
public class TestReceive {
    public static void main(String[] args) {
        FileTransfer transfer = FileTransfer.getInstance();
        transfer.startReceiver(25536, "H:/test", false);
        transfer.setReceiveFileListener(new SimpleReceiveFileListener() {
            @Override
            public void onFilesProgress(Map<String, Integer> fileProgressMap) {
                for (Map.Entry<String, Integer> entry : fileProgressMap.entrySet()) {
                    System.out.println("接收文件：" + entry.getKey() + "(" + entry.getValue() + "%)");
                }
            }

            @Override
            public void onFileReceiveSuccess(String fileName) {
                System.out.println(fileName + "接收完毕！");
            }
        });
        System.out.println("准备接收！");
    }
}
