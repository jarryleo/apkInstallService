package cn.leo.tcp.file;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Jarry Leo
 * @date : 2019/1/26 14:48
 */
class FileTransferImpl extends FileTransfer implements Runnable {
    private ReceiveFileListener receiveFileListener;
    private SendFileListener sendFileListener;
    private ConcurrentHashMap<String, List<FileSender.Sender>> fileSendProgressMap =
            new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> fileSize = new ConcurrentHashMap<>();

    @Override
    public void startReceiver(int port, String dir, boolean rename) {
        FileReceiver fileReceiver = new FileReceiver(port, dir, rename, new NewFileRequest() {
            @Override
            public void accept() {

            }

            @Override
            public void denied() {

            }
        });
        //开启接收文件端口
        fileReceiver.start();
    }

    @Override
    public void setReceiveFileListener(ReceiveFileListener receiveFileListener) {
        this.receiveFileListener = receiveFileListener;
    }

    @Override
    public void sendFiles(String host, int port, List<File> fileList) {
        for (File file : fileList) {
            sendFile(file, host, port);
        }
    }

    @Override
    public void sendFile(File file, String host, int port) {
        if (file == null) return;
        fileSize.put(file.getName(), file.length());
        FileSender.send(file, host, port, fileSendProgressMap);
    }

    @Override
    public void setSendFileListener(SendFileListener sendFileListener) {
        this.sendFileListener = sendFileListener;
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                Thread.sleep(1000);
                //发送进度回调
                if (sendFileListener != null) {
                    HashMap<String, Integer> progressMap = new HashMap<>();
                    for (Map.Entry<String, List<FileSender.Sender>> listEntry : fileSendProgressMap.entrySet()) {
                        String fileName = listEntry.getKey();
                        List<FileSender.Sender> value = listEntry.getValue();
                        Long fileLength = fileSize.get(fileName);
                        int size = 0;
                        for (FileSender.Sender sender : value) {
                            size += sender.getSendSize();
                        }
                        int percent = (int) (size * 100 / fileLength);
                        progressMap.put(fileName, percent);
                    }
                    sendFileListener.onSendFileProgress(progressMap);
                }
                //接收进度回调
                if (receiveFileListener != null) {
                    //TODO
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
