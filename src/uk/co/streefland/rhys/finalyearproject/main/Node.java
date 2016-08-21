package uk.co.streefland.rhys.finalyearproject.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by Rhys on 07/07/2016.
 */
public class Node implements  Serializable {

    private NodeId nodeId;
    private InetAddress inetAddress;
    private int port;
    private final String strRepresentation;

    public Node(NodeId nid, InetAddress ip, int port) {
        this.nodeId = nid;
        this.inetAddress = ip;
        this.port = port;
        this.strRepresentation = this.nodeId.toString();
    }

    public Node(DataInputStream in) throws IOException
    {
        this.fromStream(in);
        this.strRepresentation = this.nodeId.toString();
    }

    public void setInetAddress(InetAddress ip) {
        this.inetAddress = ip;
    }

    public NodeId getNodeId() {
       return this.nodeId;
    }

    /**
     * Create a SocketAddress for this node
     */
    public InetSocketAddress getSocketAddress()
    {
        return new InetSocketAddress(this.inetAddress, this.port);
    }

    //@Override
    public void toStream(DataOutputStream out) throws IOException
    {
         /* Add the NodeId to the stream */
        this.nodeId.toStream(out);

        /* Add the Node's IP address to the stream */
        byte[] a = inetAddress.getAddress();
        if (a.length != 4)
        {
            throw new RuntimeException("Expected InetAddress of 4 bytes, got " + a.length);
        }
        out.write(a);

        /* Add the port to the stream */
        out.writeInt(port);
    }

    //@Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        /* Load the NodeId */
        this.nodeId = new NodeId(in);

        /* Load the IP Address */
        byte[] ip = new byte[4];
        in.readFully(ip);
        this.inetAddress = InetAddress.getByAddress(ip);

        /* Read in the port */
        this.port = in.readInt();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Node)
        {
            Node n = (Node) o;
            if (n == this)
            {
                return true;
            }
            return this.getNodeId().equals(n.getNodeId());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getNodeId().hashCode();
    }

    @Override
    public String toString()
    {
        return this.getNodeId().toString();
    }

}

