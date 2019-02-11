package cn.leo.test;

import cn.leo.tcp.file.FileTransfer;
import cn.leo.tcp.file.SimpleSendFileListener;

import java.io.File;
import java.util.Map;

/**
 * @author : Jarry Leo
 * @date : 2019/2/11 14:52
 */
public class TestSend {
    public static void main(String[] args) {
        FileTransfer transfer = FileTransfer.getInstance();
        transfer.setSendFileListener(new SimpleSendFileListener() {
            @Override
            public void onSendFailed(String fileName) {
                System.out.println(fileName + "发送失败！");
                transfer.close();
            }

            @Override
            public void onSendSuccess(String fileName) {
                System.out.println(fileName + "发送成功！");
            }

            @Override
            public void onSendFileProgress(Map<String, Integer> fileProgressMap) {
                for (Map.Entry<String, Integer> entry : fileProgressMap.entrySet()) {
                    System.out.println("发送文件：" + entry.getKey() + "(" + entry.getValue() + "%)");
                }
            }
        });
        File file = new File("H:/win10.iso");
        transfer.sendFile(file, "127.0.0.1", 25536);

    }
}
