package uk.co.streefland.rhys.finalyearproject.node;

import uk.co.streefland.rhys.finalyearproject.message.Streamable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Represents each individual node on the network. Stores the NodeId, InetAddress, and port.
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
    public void toStream(DataOutputStream out) throws IOException
    {
         /* Add the NodeId to the stream */
        nodeId.toStream(out);

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
    public final void fromStream(DataInputStream in) throws IOException
    {
        /* Read the NodeId */
        nodeId = new NodeId(in);

        /* Read the IP Address */
        byte[] ip = new byte[4];
        in.readFully(ip);
        inetAddress = InetAddress.getByAddress(ip);

        /* Read the port */
        port = in.readInt();
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
            return getNodeId().equals(n.getNodeId());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return getNodeId().hashCode();
    }

    /**
     * Returns the HEX representation of the NodeId as a string
     */
    @Override
    public String toString()
    {
        return getNodeId().toString();
    }

    /**
     * Returns the InetSocketAddress for the InetAddress and port for the node
     */
    public InetSocketAddress getSocketAddress()
    {
        return new InetSocketAddress(inetAddress, port);
    }

    public NodeId getNodeId() {
        return nodeId;
    }
}

