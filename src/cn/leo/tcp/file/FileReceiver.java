package cn.leo.tcp.file;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author : Jarry Leo
 * @date : 2019/1/26 9:32
 */
class FileReceiver extends Thread {
    private int port;
    private String dir;
    private boolean rename;
    private ReceiveFileListener receiveFileListener;

    public FileReceiver(int port, String dir, boolean rename, ReceiveFileListener receiveFileListener) {
        this.port = port;
        this.dir = dir;
        this.rename = rename;
        this.receiveFileListener = receiveFileListener;
    }

    @Override
    public void run() {
        receiveFile();
    }

    private void receiveFile() {
        try {
            createSocketChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSocketChannel() throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        for (; ; ) {
            SocketChannel receiveChannel = serverSocketChannel.accept();
            //判断连接是申请文件传输还是多线程文件传输
            dispatchChannel(receiveChannel);
        }
    }

    private void dispatchChannel(SocketChannel receiveChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Constant.BUFFER_SIZE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int readLength = 0;
        while ((readLength = receiveChannel.read(buffer)) > 0) {
            buffer.flip();
            baos.write(buffer.array());
            buffer.clear();
            // 最后一包读取特殊处理,不然会一直等待读入
            if (readLength != buffer.capacity()) {
                break;
            }
        }
        String json = baos.toString();
        FileInfo fileInfo = JSONObject.parseObject(json, FileInfo.class);
        if (fileInfo.getType() == Constant.CONNECTION_TYPE_REQUEST) {
            //文件请求类型
            isRequest(receiveChannel, fileInfo);
        } else if (fileInfo.getType() == Constant.CONNECTION_TYPE_THREAD) {
            //文件传输类型
            fileReceive(receiveChannel, fileInfo);
        }

    }

    private void fileReceive(SocketChannel receiveChannel, FileInfo fileInfo) {
        try {
            long start = fileInfo.getStart();
            long part = fileInfo.getPartSize();
            long length = fileInfo.getFileSize();
            if (start + part > length) {
                part = length - start;
            }
            File file = new File(dir, fileInfo.getFileName());
            if (file.exists() && rename) {
                //如果文件存在并且需要重命名 TODO
            }
            FileChannel fileChannel = createClipFileChannel(file, start);
            Receiver receiver = new Receiver(receiveChannel, fileChannel);
            IOThreadPool.execute(receiver);
            //receiverList.add(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void isRequest(SocketChannel receiveChannel, FileInfo fileInfo) throws IOException {
        if (receiveFileListener == null) {
            //没有监听，自动同意接收文件
            sendAcceptCode(receiveChannel);
        } else {
            receiveFileListener.onNewFile(fileInfo, new NewFileRequest() {
                @Override
                public void accept() {
                    //同意接收文件
                    try {
                        sendAcceptCode(receiveChannel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void denied() {
                    //拒绝接受文件
                    try {
                        sendDeniedCode(receiveChannel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void sendAcceptCode(SocketChannel receiveChannel) throws IOException {
        //发送同意接收文件的代码 TODO
        receiveChannel.close();
    }

    private void sendDeniedCode(SocketChannel receiveChannel) throws IOException {
        //发送拒绝接受文件的代码 TODO
        receiveChannel.close();
    }
    /*private void createSocketChannel() throws Exception {
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
            receiveLength = 0;
            Thread.sleep(1000);
            for (Receiver receiver : receiverList) {
                receiveLength += receiver.getSize();
            }
            System.out.println("文件已接收:" + receiveLength + "/" + length);
        }
        receiverList.clear();
        long time2 = System.currentTimeMillis();
        System.out.println("文件接收完成:" + file.getName());
        System.out.println("耗时：" + (time2 - time1) + "ms");

    }*/

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
