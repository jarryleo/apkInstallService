package cn.leo;

import cn.leo.tcp.file.FileTransfer;
import cn.leo.tcp.file.SimpleReceiveFileListener;
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
        transfer.setReceiveFileListener(new SimpleReceiveFileListener() {
            @Override
            public void onFilesProgress(Map<String, Integer> fileProgressMap) {
                for (Map.Entry<String, Integer> entry : fileProgressMap.entrySet()) {
                    System.out.println(entry.getKey() + "(" + entry.getValue() + "%)");
                }
            }
        });
        File file = new File("H:/win10.iso");
        transfer.sendFile(file, "127.0.0.1", 25536);

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
