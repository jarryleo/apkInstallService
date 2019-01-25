package cn.leo.udp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:55
 */
public class UdpSenderImpl implements UdpSender {
    private String remoteHost = "127.0.0.1";
    private int port = UdpConfig.DEFAULT_LISTEN_PORT;
    private PacketProcessor packetProcessor = new DefaultPacketProcessor();
    private UdpSendCore udpSendCore = new UdpSendCore();
    private String broadcastHost;

    public UdpSenderImpl() {
    }

    public UdpSenderImpl(String remoteHost, int port) {
        this.remoteHost = remoteHost;
        this.port = port;
    }

    public UdpSenderImpl(String remoteHost, int port, PacketProcessor packetProcessor) {
        this.remoteHost = remoteHost;
        this.port = port;
        this.packetProcessor = packetProcessor;
    }

    @Override
    public UdpSender setRemoteHost(String host) {
        remoteHost = host;
        return this;
    }

    @Override
    public UdpSender setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public UdpSender setPacketProcessor(PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
        return this;
    }

    @Override
    public UdpSender send(byte[] data) {
        List<byte[]> bytes = packetProcessor.subPacket(data);
        for (byte[] aByte : bytes) {
            udpSendCore.sendData(aByte, remoteHost, port);
        }
        return this;
    }

    @Override
    public UdpSender sendBroadcast(byte[] data) {
        if (broadcastHost == null) {
            getBroadcastHost();
        }
        List<byte[]> bytes = packetProcessor.subPacket(data);
        for (byte[] aByte : bytes) {
            udpSendCore.sendData(aByte, broadcastHost, port);
        }
        return this;
    }

    private void getBroadcastHost() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            byte[] bytes = address.getAddress();
            bytes[3] = (byte) 255;
            broadcastHost = InetAddress.getByAddress(bytes).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
