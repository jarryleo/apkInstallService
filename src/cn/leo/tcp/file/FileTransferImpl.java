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
    private FileReceiver fileReceiver;
    private String dir;
    private boolean rename;

    @Override
    public void startReceiver(int port, String dir, boolean rename) {
        this.dir = dir;
        this.rename = rename;
        fileReceiver = new FileReceiver(port, dir, rename, receiveFileListener);
        //开启接收文件端口
        fileReceiver.start();
    }

    @Override
    public void setReceiveFileListener(ReceiveFileListener receiveFileListener) {
        this.receiveFileListener = receiveFileListener;
        fileReceiver.setReceiveFileListener(receiveFileListener);
    }

    @Override
    public void sendFiles(List<File> fileList, String host, int port) {
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
        HashMap<String, Integer> sendProgressMap = new HashMap<>();
        HashMap<String, Integer> receiveProgressMap = new HashMap<>();
        for (; ; ) {
            try {
                Thread.sleep(1000);
                //发送进度回调
                if (sendFileListener != null) {
                    for (Map.Entry<String, List<FileSender.Sender>> listEntry : fileSendProgressMap.entrySet()) {
                        String fileName = listEntry.getKey();
                        List<FileSender.Sender> value = listEntry.getValue();
                        Long fileLength = fileSize.get(fileName);
                        long size = 0;
                        for (FileSender.Sender sender : value) {
                            size += sender.getSendSize();
                        }
                        //删除已经发送完毕的回调
                        Integer integer = sendProgressMap.get(fileName);
                        if (integer != null && integer == 100) {
                            //发送完毕删除回调记录
                            fileSendProgressMap.remove(fileName);
                            sendProgressMap.remove(fileName);
                        } else {
                            int percent = (int) (size * 100 / fileLength);
                            sendProgressMap.put(fileName, percent);
                        }
                    }
                    sendFileListener.onSendFileProgress(sendProgressMap);
                }
                //接收进度轮询
                ConcurrentHashMap<String, List<FileReceiver.Receiver>> receiverMap = fileReceiver.getReceiverMap();
                for (Map.Entry<String, List<FileReceiver.Receiver>> receiverEntry : receiverMap.entrySet()) {
                    String fileName = receiverEntry.getKey();
                    File breakPointFile = new File(dir, fileName + ".bp");
                    List<FileReceiver.Receiver> receivers = receiverEntry.getValue();
                    long fileSize = 0;
                    long receivedSize = 0;
                    for (FileReceiver.Receiver receiver : receivers) {
                        //断点保存
                        long partSize = receiver.getPartSize();
                        fileSize += partSize;
                        long partReceivedSize = receiver.getReceivedSize();
                        //存储 文件名，利用start/part作为索引，partReceivedSize,作为value
                        int partIndex = receiver.getFileInfo().getPartIndex();
                        BreakPoint.savePoint(breakPointFile, partIndex, partReceivedSize);
                        receivedSize += partReceivedSize;
                    }
                    //删除已经接收完毕的回调
                    Integer integer = receiveProgressMap.get(fileName);
                    if (integer != null && integer == 100) {
                        receiverMap.remove(fileName);
                        receiveProgressMap.remove(fileName);
                        //重命名零时文件
                        File file = new File(dir, fileName + ".tmp");
                        File dest = new File(dir, fileName);
                        if (rename) {
                            dest = getRename(dest);
                        } else {
                            //不重名就覆盖同名文件。先删除
                            if (dest.exists()) {
                                dest.delete();
                            }
                        }
                        boolean b = file.renameTo(dest);
                        //删除断点记录文件
                        breakPointFile.delete();
                      /*if (b) {
                            //传送文件成功回调 TODO
                            System.out.println("重命名成功");
                            System.out.println(dest.getPath());
                        } else {
                            System.out.println("重命名失败");
                        }*/
                    } else {
                        int percent = (int) (receivedSize * 100 / fileSize);
                        receiveProgressMap.put(fileName, percent);
                    }
                }
                if (receiveFileListener != null) {
                    receiveFileListener.onFilesProgress(receiveProgressMap);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //获取一个文件不存在的重命名
    private File getRename(File dest) {
        if (!dest.exists()) {
            return dest;
        }
        String name = dest.getName();
        String parent = dest.getParent();
        int lastIndexOf = name.lastIndexOf(".");
        String mainName = name.substring(0, lastIndexOf);
        String extName = name.substring(lastIndexOf);
        int i = 0;
        do {
            i++;
            String fileRename = mainName + "(" + i + ")" + extName;
            dest = new File(parent, fileRename);
        } while (dest.exists());
        return dest;
    }
}
