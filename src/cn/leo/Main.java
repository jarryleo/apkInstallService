package cn.leo;

import cn.leo.tcp.file.FileInfo;
import cn.leo.tcp.file.FileTransfer;
import cn.leo.tcp.file.NewFileRequest;
import cn.leo.tcp.file.ReceiveFileListener;
import cn.leo.udp.OnDataArrivedListener;
import cn.leo.udp.UdpFrame;
import cn.leo.udp.UdpListener;
import cn.leo.udp.UdpSender;

import java.io.File;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
//        testUdp();
        testFileTransfer();
    }

    private static void testFileTransfer() {
        FileTransfer transfer = FileTransfer.getInstance();
        transfer.startReceiver(25536, "H:/test", false);
        transfer.setReceiveFileListener(new ReceiveFileListener() {
            @Override
            public void onNewFile(FileInfo fileinfo, NewFileRequest callback) {
                callback.accept();
            }

            @Override
            public void onFilesProgress(Map<String, Integer> fileProgressMap) {
                for (Map.Entry<String, Integer> entry : fileProgressMap.entrySet()) {
                    System.out.println(entry.getKey() + "(" + entry.getValue() + ")");
                }
            }

            @Override
            public void onTransferFailed(FileInfo fileinfo) {
                System.out.println("文件传送失败：" + fileinfo.getFileName());
            }
        });
        File fileSend = new File("H:/cn_windows_10_multiple_editions_version_1703_updated_july_2017_x64_dvd_10925382.iso");
        transfer.sendFile(fileSend, "127.0.0.1", 25536);
    }

    private static void testUdp() {
        UdpListener udpListener = UdpFrame.getListener();
        udpListener.subscribe(25535, new OnDataArrivedListener() {
            @Override
            public void onDataArrived(byte[] data, String host) {
                String s = new String(data);
                System.out.println(s);
            }
        });
        UdpSender sender = UdpFrame.getSender(25535);
        sender.send("测试文字".getBytes());
    }
}
