package cn.leo.test;

import cn.leo.tcp.download.DownLoadManager;
import cn.leo.tcp.download.DownloadImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author : Jarry Leo
 * @date : 2019/3/5 10:27
 */
public class Test {
    public static void main(String[] args) throws Exception {
        test1();
    }

    private static void test1() throws Exception {
        String url = "https://www.cnblogs.com/xzmblog/p/4505237.html";
        DownLoadManager downLoadManager = new DownloadImpl();
        downLoadManager.download(url, "");
    }

    public static void test(){
        InetAddress address = null;
        try {
            address = InetAddress.getByName("www.cnblogs.com");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("获取失败");
        }
        System.out.println(address.getHostAddress().toString());

    }
}
