package cn.leo.tcp.file;

import sun.dc.pr.PRError;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author : Jarry Leo
 * @date : 2019/1/26 8:50
 */
class FileSender {
    private FileSender() {

    }

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


    class Sender implements Runnable {
        private SocketChannel sendChannel;
        private FileChannel fileChannel;
        private File file;
        private long start;
        private long length;
        private volatile long sendSize;

        public long getSendSize() {
            return sendSize;
        }

        public Sender(SocketChannel sendChannel,
                      FileChannel fileChannel,
                      File file,
                      long start,
                      long length) {
            this.sendChannel = sendChannel;
            this.fileChannel = fileChannel;
            this.file = file;
            this.start = start;
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
                while ((len = fileChannel.read(buffer)) != -1 && sendSize < length) {
                    buffer.flip();
                    if (sendSize + len > length) {
                        len = (int) (length - sendSize);
                        buffer.limit(len);
                    }
                    sendChannel.write(buffer);
                    buffer.clear();
                    sendSize += len;
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
    public static void send(File file, String host, int port, Map<String, List<Sender>> fileProgressMap) {
        if (file == null || !file.exists()) {
            return;
        }
        long length = file.length();
        if (length == 0) {
            return;
        }
        try {
            //1.创建文件发送对象
            FileSender fileSender = new FileSender();
            //2.发送文件信息等待应答
            SocketChannel askChannel = fileSender.createSocketChannel(host, port);
            askChannel.write(ByteBuffer.wrap(file.getName().getBytes()));
            //3.等待应答信息
            ByteBuffer buffer = ByteBuffer.allocate(6);
            StringBuffer answerCode = new StringBuffer();
            int len = 0;
            while ((len = askChannel.read(buffer)) != -1) {
                buffer.flip();
                answerCode.append(new String(buffer.array()));
                if (len != buffer.capacity()) {
                    break;
                }
                buffer.clear();
            }
            System.out.println("server answer is " + answerCode.toString().trim());
            if (!answerCode.toString().trim().equals("0000")) {
                System.out.println("rec server answer error");
                askChannel.close();
                return;
            }
            //4.开始发送文件
            List<Sender> senderList = new ArrayList<>();
            if (length > Constant.FILE_PART_SIZE) {
                //文件分段
                long part = (length + Constant.FILE_PART_NUM) / Constant.FILE_PART_NUM;
                long start = 0;
                do {
                    if (length - start < part) {
                        part = length - start;
                    }
                    Sender sender = fileSender.new Sender(
                            fileSender.createSocketChannel(host, port),
                            fileSender.createClipFileChannel(file, start),
                            file,
                            start,
                            part);
                    IOThreadPool.execute(sender);
                    senderList.add(sender);
                } while ((start += part) < length);
            } else {
                //文件不分段
                Sender sender = fileSender.new Sender(
                        fileSender.createSocketChannel(host, port),
                        fileSender.createClipFileChannel(file, 0),
                        file,
                        0,
                        length);
                IOThreadPool.execute(sender);
                senderList.add(sender);
            }
            fileProgressMap.put(file.getName(), senderList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
