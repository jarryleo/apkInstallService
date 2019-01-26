package cn.leo;

import cn.leo.tcp.file.FileReceiver;
import cn.leo.tcp.file.FileSender;
import cn.leo.udp.OnDataArrivedListener;
import cn.leo.udp.UdpFrame;
import cn.leo.udp.UdpListener;
import cn.leo.udp.UdpSender;

import java.io.File;

public class Main {

    public static void main(String[] args) {
//        testUdp();
        testFileTransfer();
    }

    private static void testFileTransfer() {
        File fileSend = new File("H:/cn_windows_10_multiple_editions_version_1703_updated_july_2017_x64_dvd_10925382.iso");
        File fileReceive = new File("H:/test/test_win10.iso");
        FileReceiver fileReceiver = new FileReceiver(25536, fileReceive, fileSend.length());
        fileReceiver.start();
        FileSender.send(fileSend, "127.0.0.1", 25536);
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
