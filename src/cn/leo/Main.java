package cn.leo;

import cn.leo.tcp.file.*;
import cn.leo.udp.OnDataArrivedListener;
import cn.leo.udp.UdpFrame;
import cn.leo.udp.UdpListener;
import cn.leo.udp.UdpSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
                    System.out.println("接收文件：" + entry.getKey() + "(" + entry.getValue() + "%)");
                }
            }

            @Override
            public void onTransferFailed(FileInfo fileinfo) {
                System.out.println("文件传送失败：" + fileinfo.getFileName());
            }
        });
        transfer.setSendFileListener(new SendFileListener() {
            @Override
            public void onSendFileProgress(Map<String, Integer> fileProgressMap) {
                for (Map.Entry<String, Integer> entry : fileProgressMap.entrySet()) {
                    System.out.println(entry.getKey() + "(" + entry.getValue() + "%)");
                }
            }

            @Override
            public void onAccept() {
                System.out.println("同意接收文件");
            }

            @Override
            public void onDenied() {
                System.out.println("拒绝接收文件");
            }

            @Override
            public void onTransferFailed(FileInfo fileinfo) {

            }
        });
        File fileSend1 = new File("H:/win10.iso");
        //File fileSend2 = new File("H:/net.ship56.hyfwpt_shipper.zip");
        List<File> files = new ArrayList<>();
        files.add(fileSend1);
        //files.add(fileSend2);
        transfer.sendFiles(files, "127.0.0.1", 25536);

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
