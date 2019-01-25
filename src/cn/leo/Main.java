package cn.leo;

import cn.leo.udp.OnDataArrivedListener;
import cn.leo.udp.UdpFrame;
import cn.leo.udp.UdpListener;
import cn.leo.udp.UdpSender;

public class Main {

    public static void main(String[] args) {
        UdpListener Udp = UdpFrame.getListener();
        Udp.subscribe(25535, new OnDataArrivedListener() {
            @Override
            public void onDataArrived(byte[] data, String host) {
                String s = new String(data);
                System.out.println(s);
            }
        });
        UdpSender sender = UdpFrame.getSender();
        sender.setPort(25535);
        sender.send("测试文字".getBytes());
    }
}
