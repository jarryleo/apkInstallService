package cn.leo;

import cn.leo.tcp.file.FileInfo;
import cn.leo.tcp.file.FileTransfer;
import cn.leo.tcp.file.SimpleSendFileListener;
import cn.leo.udp.OnDataArrivedListener;
import cn.leo.udp.UdpFrame;
import cn.leo.udp.UdpListener;
import cn.leo.udp.UdpSender;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    private static String fileList;

    public static void main(String[] args) {
        listen();
        System.out.println("文件服务开启成功！");
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
        FileTransfer fileTransfer = FileTransfer.getInstance();
        fileTransfer.setSendFileListener(new SimpleSendFileListener() {
            @Override
            public void onSendFailed(String fileName) {
                System.out.println(fileName + "发送失败！");
            }

            @Override
            public void onSendSuccess(String fileName) {
                System.out.println(fileName + "发送成功！");
            }

            @Override
            public void onSendFileProgress(Map<String, Integer> fileProgressMap) {
                for (Map.Entry<String, Integer> entry : fileProgressMap.entrySet()) {
                    System.out.println("正在发送文件：" + entry.getKey() + "(" + entry.getValue() + "%)");
                }
            }
        });
        UdpSender sender = UdpFrame.getSender(25536);
        UdpListener udpListener = UdpFrame.getListener();
        udpListener.subscribe(25535, new OnDataArrivedListener() {
            @Override
            public void onDataArrived(byte[] data, String host) {
                String s = new String(data, Charset.forName("UTF-8"));
                if ("list".equals(s)) {
                    sender.setRemoteHost(host);
                    sender.send(fileList.getBytes(Charset.forName("UTF-8")));
                } else {
                    File file = new File(s);
                    if (file.exists()) {
                        fileTransfer.sendFile(file, host, 25537);
                    }
                }
                System.out.println("来自 " + host + " 的请求: " + s);
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
            if (!f.isDirectory() /*&& f.getName().endsWith("apk")*/) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(f.getName());
                fileInfo.setFileSize(f.length());
                fileInfo.setStart(f.lastModified());
                fileList.add(fileInfo);
            }
        }
        return JSON.toJSONString(fileList);
    }
}
