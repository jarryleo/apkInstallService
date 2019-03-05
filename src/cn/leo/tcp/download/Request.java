package cn.leo.tcp.download;

import sun.security.ssl.SSLServerSocketFactoryImpl;
import sun.security.ssl.SSLSocketFactoryImpl;
import sun.security.ssl.SSLSocketImpl;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author : Jarry Leo
 * @date : 2019/3/5 10:52
 */
public class Request {
    private String url;

    public Request(String url) {
        this.url = url;
    }

    public void execute() throws IOException {
        Header header = new Header();
        header.setUrl(url);
        byte[] bytes = header.getHeaderBytes();
        SocketChannel channel;
        if (header.isHttps()) {
            channel = createSSLSocketChannel(header.getHost(), header.getPort());
        } else {
            channel = createSocketChannel(header.getHost(), header.getPort());
        }
        channel.write(ByteBuffer.wrap(bytes));
        read(channel);
    }

    //区分连接类型
    private void read(SocketChannel receiveChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int readLength = 0;
        while ((readLength = receiveChannel.read(buffer)) != -1) {
            buffer.flip();
            baos.write(buffer.array());
            buffer.clear();
            if (readLength != buffer.capacity()) {
                break;
            }
        }
        String result = baos.toString("UTF-8");
        System.out.println(result);
    }

    private SocketChannel createSocketChannel(String host, int port) throws IOException {
        System.out.println(host + ":" + port);
        SocketChannel sendChannel = SocketChannel.open();
        sendChannel.connect(new InetSocketAddress(host, port));
        return sendChannel;
    }

    private SocketChannel createSSLSocketChannel(String host, int port) throws IOException {
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
        SocketChannel channel = socket.getChannel();

        return channel;
    }
}
