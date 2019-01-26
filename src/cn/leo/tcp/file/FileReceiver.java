package cn.leo.tcp.file;

import cn.leo.tcp.IOThreadPool;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Jarry Leo
 * @date : 2019/1/26 9:32
 */
public class FileReceiver extends Thread {
    private int port;
    private File file;
    private long length;

    public FileReceiver(int port, File file, long length) {
        this.port = port;
        this.file = file;
        this.length = length;
    }

    @Override
    public void run() {
        receiveFile();
    }

    private void receiveFile() {
        if (file == null) {
            return;
        }
        try {
            createSocketChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSocketChannel() throws Exception {
        long time1 = System.currentTimeMillis();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        int num = 1;
        long part = length;
        if (length > Constant.FILE_PART_SIZE) {
            num = Constant.FILE_PART_NUM;
            part = (length + Constant.FILE_PART_NUM) / Constant.FILE_PART_NUM;
        }
        List<Receiver> receiverList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            SocketChannel receiveChannel = serverSocketChannel.accept();
            try {
                long start = i * part;
                if (start + part > length) {
                    part = length - start;
                }
                FileChannel fileChannel = createClipFileChannel(file, start);
                Receiver receiver = new Receiver(receiveChannel, fileChannel);
                IOThreadPool.execute(receiver);
                receiverList.add(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long receiveLength = 0;
        while (receiveLength < length) {
            Thread.sleep(1000);
            for (Receiver receiver : receiverList) {
                receiveLength += receiver.getSize();
            }
            System.out.println("文件已接收:" + receiveLength + "/" + length);
        }
        long time2 = System.currentTimeMillis();
        System.out.println("文件接收完成:" + file.getName());
        System.out.println("耗时：" + (time2 - time1) + "ms");

    }

    private FileChannel createClipFileChannel(File file, long start) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = raf.getChannel();
        fileChannel = fileChannel.position(start);
        return fileChannel;
    }


    private class Receiver implements Runnable {
        private SocketChannel receiveChannel;
        private FileChannel fileChannel;
        private volatile long size;

        public Receiver(SocketChannel receiveChannel, FileChannel fileChannel) {
            this.receiveChannel = receiveChannel;
            this.fileChannel = fileChannel;
        }

        public long getSize() {
            return size;
        }

        @Override
        public void run() {
            receiveFile(receiveChannel, fileChannel);
        }

        private void receiveFile(SocketChannel receiveChannel, FileChannel fileChannel) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(Constant.BUFFER_SIZE);
                int len = 0;
                while ((len = receiveChannel.read(buffer)) >= 0) {
                    buffer.flip();
                    fileChannel.write(buffer);
                    buffer.clear();
                    size += len;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    receiveChannel.close();
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

}
