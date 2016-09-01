package uk.co.streefland.rhys.finalyearproject.node;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.message.Streamable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Node class that represents each node on the network. Stores the NodeId, InetAddress, and port.
 */
public class Node implements Streamable, Serializable {

    private NodeId nodeId;
    private InetAddress inetAddress;
    private int port;

    public Node(NodeId nid, InetAddress ip, int port) {
        this.nodeId = nid;
        this.inetAddress = ip;
        this.port = port;
    }

    public Node(DataInputStream in) throws IOException
    {
        fromStream(in);
    }

    @Override
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

    public void setInetAddress(InetAddress ip) {
        this.inetAddress = ip;
    }

    /**
     * Returns the InetSocketAddress for the InetAddress and port for the node
     */
    public InetSocketAddress getSocketAddress()
    {
        return new InetSocketAddress(this.inetAddress, this.port);
    }

    public NodeId getNodeId() {
        return this.nodeId;
    }

    /**
     * Returns the hashcode of the Node's NodeId
     */
    @Override
    public int hashCode()
    {
        return getNodeId().hashCode();
    }

    /**
     * Returns the HEX representation of the Node's NodeId as a string
     */
    @Override
    public String toString()
    {
        return getNodeId().toString();
    }

}

