package uk.co.streefland.rhys.finalyearproject.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Represents each individual node on the network. Stores the KeyId, InetAddress, and port.
 */
public class Node implements Serializable {

    private KeyId nodeId;
    private InetAddress publicIp;
    private InetAddress privateIp;
    private int publicPort;
    private int privatePort;

    public Node(KeyId nid, InetAddress publicIp, InetAddress privateIp, int publicPort, int privatePort) {
        this.nodeId = nid;
        this.publicIp = publicIp;
        this.privateIp = privateIp;
        this.publicPort = publicPort;
        this.privatePort = privatePort;
    }

    public Node(DataInputStream in) throws IOException {
        fromStream(in);
    }

    public void toStream(DataOutputStream out) throws IOException {
         /* Add the KeyId to the stream */
        nodeId.toStream(out);

        /* Add the Node's IP address to the stream */
        byte[] a = publicIp.getAddress();
        if (a.length != 4) {
            throw new RuntimeException("I expected an InetAddress of 4 bytes, here's what I actually got: " + a.length);
        }
        out.write(a);

        /* Add the Node's IP address to the stream */
        byte[] b = privateIp.getAddress();
        if (b.length != 4) {
            throw new RuntimeException("I expected an InetAddress of 4 bytes, here's what I actually got: " + b.length);
        }
        out.write(b);

        /* Add the ports to the stream */
        out.writeInt(publicPort);
        out.writeInt(privatePort);
    }

    private void fromStream(DataInputStream in) throws IOException {
        /* Read the KeyId */
        nodeId = new KeyId(in);

        /* Read the IP Address */
        byte[] ip = new byte[4];
        in.readFully(ip);
        publicIp = InetAddress.getByAddress(ip);

        /* Read the IP Address */
        byte[] ip2 = new byte[4];
        in.readFully(ip2);
        privateIp = InetAddress.getByAddress(ip2);

        /* Read the ports */
        publicPort = in.readInt();
        privatePort = in.readInt();
    }

    /**
     * Returns the InetSocketAddress for the InetAddress and port for the node
     */
    public InetSocketAddress getPublicSocketAddress() {
        return new InetSocketAddress(publicIp, publicPort);
    }

    public InetSocketAddress getPrivateSocketAddress() {
        return new InetSocketAddress(privateIp, privatePort);
    }

    public InetAddress getPublicInetAddress() {
        return publicIp;
    }

    public void setPublicInetAddress(InetAddress publicIp) {
        this.publicIp = publicIp;
    }

    public InetAddress getPrivateInetAddress() {
        return privateIp;
    }

    public void setPrivateInetAddress(InetAddress privateIp) {
        this.privateIp = privateIp;
    }

    public KeyId getNodeId() {
        return nodeId;
    }

    public int getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(int publicPort) {
        this.publicPort = publicPort;
    }

    public int getPrivatePort() {
        return privatePort;
    }

    public void setPrivatePort(int privatePort) {
        this.privatePort = privatePort;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node n = (Node) o;
            if (n == this) {
                return true;
            }
            return getNodeId().equals(n.getNodeId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getNodeId().hashCode();
    }

    /**
     * Returns the HEX representation of the KeyId as a string
     */
    @Override
    public String toString() {
        return getNodeId().toString();
    }
}

