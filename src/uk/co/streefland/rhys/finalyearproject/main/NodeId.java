package uk.co.streefland.rhys.finalyearproject.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

/**
 * Created by Rhys on 07/07/2016.
 */
public class NodeId implements Serializable {

    public final transient static int ID_LENGTH = 160;
    public final transient static int BYTES_LENGTH = ID_LENGTH / 8;
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

    public NodeId(byte[] id) {
        if (idBytes.length != BYTES_LENGTH) {
            throw new IllegalArgumentException("Data needs to be " + BYTES_LENGTH + " characters long");
        }
    }

    public NodeId(DataInputStream in) throws IOException
    {
        this.fromStream(in);
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
        }
        return false;
    }

    public NodeId xor(NodeId nodeId) {
        byte[] nodeIdBytes = nodeId.getIdBytes();
        byte[] result = new byte[BYTES_LENGTH];

        for (int i = 0; i < BYTES_LENGTH; i++) {
            result[i] = (byte) (this.idBytes[i] ^ nodeIdBytes[i]);
        }

        NodeId resultNodeId = new NodeId(result);

        return resultNodeId;
    }

    public byte[] getIdBytes() {
        return idBytes;
    }

    public int getFirstSetBitIndex() {
        int prefixLength = 0;

        for (byte b : this.idBytes) {
            if (b == 0) {
                prefixLength += 8;
            } else {
                int count = 0;
                for (int i = 7; i >= 0; i--) {
                    boolean a = (b & (1 << i)) == 0;
                    if (a) {
                        count++;
                    } else {
                        break;   // Reset the count if we encounter a non-zero number
                    }
                }

                /* Add the count of MSB 0s to the prefix length */
                prefixLength += count;

                /* Break here since we've now covered the MSB 0s */
                break;
            }
        }
        return prefixLength;
    }

    public int getDistance(NodeId otherNode) {
        /**
         * Compute the xor of this and to
         * Get the index i of the first set bit of the xor returned NodeId
         * The distance between them is ID_LENGTH - i
         */
        return ID_LENGTH - this.xor(otherNode).getFirstSetBitIndex();
    }

    public NodeId generateNodeIdByDistance(int distance) {
        byte result[] = new byte[BYTES_LENGTH];

        // distance = ID-LENGTH - prefixLength
        int numberByteZeroes = (ID_LENGTH - distance) / 8;
        int numberBitZeroes = 8 - (distance % 8);

        // Fill byte zeroes
        for (int i = 0; i < numberByteZeroes; i++) {
            result[i] = 0;
        }

        // Fill bit zeroes
        BitSet bits = new BitSet(8);
        bits.set(0, 8);

        for (int i = 0; i < numberBitZeroes; i++) {
            /* Shift 1 zero into the start of the value */
            bits.clear(i);
        }

        bits.flip(0, 8);        // Flip the bits since they're in reverse order
        result[numberByteZeroes] = bits.toByteArray()[0];

        /* Set the remaining bytes to Maximum value */
        for (int i = numberByteZeroes + 1; i < result.length; i++) {
            result[i] = Byte.MAX_VALUE;
        }

        return this.xor(new NodeId(result));
    }

    //@Override
    public void toStream(DataOutputStream out) throws IOException {
        out.write(this.getIdBytes());
    }

    //@Override
    public final void fromStream(DataInputStream in) throws IOException {
        byte[] input = new byte[BYTES_LENGTH];
        in.readFully(input);
        this.idBytes = input;
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

    public String toBinary() {
        String output = "";
        for (byte b : idBytes) {
            output = output + " " + (Integer.toBinaryString(b & 255 | 256).substring(1));
        }
        return output;
    }

    /* SLOWER TO HEX METHOD
    @Override
    public String toString() {
        // Returns the hex format of this NodeId
        BigInteger bi = new BigInteger(1, this.idBytes);
        return String.format("%0" + (this.idBytes.length << 1) + "X", bi);
    } */
}