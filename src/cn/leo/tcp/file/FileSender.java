package cn.leo.tcp.file;

import cn.leo.tcp.IOThreadPool;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @author : Jarry Leo
 * @date : 2019/1/26 8:50
 */
public class FileSender {
    /**
     * 获取读取文件片段
     *
     * @param file  文件
     * @param start 开始位置
     * @return 文件片段通道
     * @throws Exception 文件异常
     */
    private FileChannel createClipFileChannel(File file, long start) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel fileChannel = raf.getChannel();
        fileChannel = fileChannel.position(start);
        return fileChannel;
    }


    /**
     * 获取连接服务器通道
     *
     * @param host 地址
     * @param port 端口
     * @return socket通道
     * @throws IOException 读写异常
     */
    private SocketChannel createSocketChannel(String host, int port) throws IOException {
        SocketChannel sendChannel = SocketChannel.open();
        sendChannel.connect(new InetSocketAddress(host, port));
        return sendChannel;
    }


    private class Sender implements Runnable {
        private SocketChannel sendChannel;
        private FileChannel fileChannel;
        private long length;


        public Sender(SocketChannel sendChannel, FileChannel fileChannel, long length) {
            this.sendChannel = sendChannel;
            this.fileChannel = fileChannel;
            this.length = length;
        }

        @Override
        public void run() {
            sendFile(sendChannel, fileChannel, length);
        }

        /**
         * 发送文件
         *
         * @param sendChannel 发送频道
         * @param fileChannel 文件频道
         */
        private void sendFile(SocketChannel sendChannel, FileChannel fileChannel, long length) {
            // 发送文件流
            try {
                ByteBuffer buffer = ByteBuffer.allocate(Constant.BUFFER_SIZE);
                int len = 0;
                int sum = 0;
                while ((len = fileChannel.read(buffer)) != -1 && sum < length) {
                    buffer.flip();
                    if (sum + len > length) {
                        len = (int) (length - sum);
                        buffer.limit(len);
                    }
                    sendChannel.write(buffer);
                    buffer.clear();
                    sum += len;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    sendChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送文件
     *
     * @param file 文件
     * @param host 对方主机
     * @param port 对方端口
     */
    public static void send(File file, String host, int port) {
        if (file == null || !file.exists()) {
            return;
        }
        long length = file.length();
        if (length == 0) {
            return;
        }
        try {
            FileSender fileSender = new FileSender();
            if (length > Constant.FILE_PART_SIZE) {
                //1、文件分段
                long part = (length + Constant.FILE_PART_NUM) / Constant.FILE_PART_NUM;
                long start = 0;
                do {
                    if (length - start < part) {
                        part = length - start;
                    }
                    Sender sender = fileSender.new Sender(
                            fileSender.createSocketChannel(host, port),
                            fileSender.createClipFileChannel(file, start),
                            part);
                    IOThreadPool.execute(sender);
                } while ((start += part) < length);
            } else {
                Sender sender = fileSender.new Sender(
                        fileSender.createSocketChannel(host, port),
                        fileSender.createClipFileChannel(file, 0),
                        length);
                IOThreadPool.execute(sender);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
