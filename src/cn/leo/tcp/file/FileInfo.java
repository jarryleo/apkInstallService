package cn.leo.tcp.file;

/**
 * @author : Jarry Leo
 * @date : 2019/1/26 10:04
 */
public class FileInfo {
    private String fileName;
    private long fileSize;
    private long start;
    private long partSize;
    private int type;//1 申请发送文件，2 多线程传输模块


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
