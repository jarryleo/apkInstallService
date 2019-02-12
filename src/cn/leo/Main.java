package cn.leo;

import cn.leo.tcp.file.FileInfo;
import cn.leo.udp.OnDataArrivedListener;
import cn.leo.udp.UdpFrame;
import cn.leo.udp.UdpListener;
import cn.leo.udp.UdpSender;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static String fileList;

    public static void main(String[] args) {
        listen();
        for (; ; ) {
            try {
                fileList = getDirFiles();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void listen() {
        UdpSender sender = UdpFrame.getSender(25536);
        UdpListener udpListener = UdpFrame.getListener();
        udpListener.subscribe(25535, new OnDataArrivedListener() {
            @Override
            public void onDataArrived(byte[] data, String host, int port) {
                String s = new String(data);
                System.out.println("来自 " + host + " :" + s);
                sender.setRemoteHost(host);
                sender.send(fileList.getBytes());
            }
        });
    }

    /**
     * 获取当前文件夹文件列表json
     */
    private static String getDirFiles() {
        List<FileInfo> fileList = new ArrayList<>();
        File file = new File("./");
        File[] files = file.listFiles();
        for (File f : files) {
            if (!f.isDirectory()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(f.getName());
                fileInfo.setFileSize(f.length());
                fileList.add(fileInfo);
            }
        }
        return JSON.toJSONString(fileList);
    }
}
