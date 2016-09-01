package uk.co.streefland.rhys.finalyearproject.node;

import uk.co.streefland.rhys.finalyearproject.message.Streamable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Rhys on 07/07/2016.
 */
public class NodeId implements Streamable, Serializable {

    public static final int ID_LENGTH = 160;   // Length of NodeId im bits
    public static final int BYTES_LENGTH = ID_LENGTH / 8; // Length of NodeId in bytes

    private byte[] idBytes;

    public NodeId() {
        idBytes = new byte[BYTES_LENGTH];
        new Random().nextBytes(idBytes);
    }

    public NodeId(String id) {
        idBytes = id.getBytes();

        if (idBytes.length != BYTES_LENGTH) {
            throw new IllegalArgumentException("Data needs to be " + BYTES_LENGTH + " characters long");
        }
    }

    public NodeId(byte[] idBytes) {
        if (idBytes.length != BYTES_LENGTH) {
            throw new IllegalArgumentException("Data needs to be " + BYTES_LENGTH + " characters long");
        }
        this.idBytes = idBytes;
    }

    public NodeId(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        out.write(this.getIdBytes());
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        byte[] input = new byte[BYTES_LENGTH];
        in.readFully(input);
        this.idBytes = input;
    }

    @Override
    public int hashCode() {
        int hash = 4;
        hash = 37 * hash + Arrays.hashCode(this.idBytes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeId) {
            NodeId nid = (NodeId) obj;
            return this.hashCode() == nid.hashCode();
        } else {
            return false;
        }
    }

    private NodeId xor(NodeId nodeId) {
        byte[] nodeIdBytes = nodeId.getIdBytes();
        byte[] result = new byte[BYTES_LENGTH];

        for (int i = 0; i < BYTES_LENGTH; i++) {
            result[i] = (byte) (idBytes[i] ^ nodeIdBytes[i]);
        }

        return new NodeId(result);
    }

    public NodeId generateNodeIdUsingDistance(int distance) {
        byte[] newNodeIdBytes = idBytes.clone();

        int bitsToFlip = distance;

        /* For each byte in byte array */
        for (int i = newNodeIdBytes.length-1; i >= 0; i--) {

            for (int j = 0; j <= 7; j++) {

                if (bitsToFlip > 0) {
                    if ((newNodeIdBytes[i] >> j & 1) == 1) {
                        newNodeIdBytes[i] &= ~(1 << j); // clear the bit
                    } else {
                        newNodeIdBytes[i] |= (1 << j); // set the bit
                    }
                    bitsToFlip--;
                } else {
                    return new NodeId(newNodeIdBytes);
                }
            }
        }
        return new NodeId(newNodeIdBytes);
    }

    public int getFirstSetBitLocation() {
        int currentBit = 0;

        /* For each byte in byte array */
        for (int i = 0; i < idBytes.length; i++) {
            if (idBytes[i] == 0) {
                currentBit += 8; // save unnecessary processing if all bits are empty in the byte
            } else {
                for (int j = 7; j >= 0; j--) {
                /* If bit is set return the currentBit value */
                    if ((idBytes[i] >> j & 1) == 1) {
                        return currentBit;
                    }
                    currentBit++;   // Increment the current bit
                }
            }
        }
        return currentBit;
    }

    public int getDistance(NodeId otherNode) {
        /**
         * Compute the xor of this and to
         * Get the index i of the first set bit of the xor returned NodeId
         * The distance between them is ID_LENGTH - i
         */
        return ID_LENGTH - xor(otherNode).getFirstSetBitLocation();
    }

    public byte[] getIdBytes() {
        return idBytes;
    }

    /**
     * @return The BigInteger representation of the key
     */
    public BigInteger getInt() {
        return new BigInteger(1, getIdBytes());
    }


    public String toBinary() {
        String output = "";
        for (byte b : idBytes) {
            output = output + " " + (Integer.toBinaryString(b & 255 | 256).substring(1));
        }
        return output;
    }

    @Override
    public String toString() {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[idBytes.length * 2];
        for (int j = 0; j < idBytes.length; j++) {
            int v = idBytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}