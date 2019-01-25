package cn.leo.udp;

import java.util.List;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:27
 */
public  class BigPacketProcessor extends PacketProcessor {

    @Override
    public List<byte[]> subPacket(byte[] data) {
        return null;
    }

    @Override
    public void mergePacket(byte[] data, String host) {

    }

    @Override
    public boolean isMergeSuccess(String host) {
        return false;
    }

    @Override
    public byte[] getMergedData(String host) {
        return new byte[0];
    }
}
