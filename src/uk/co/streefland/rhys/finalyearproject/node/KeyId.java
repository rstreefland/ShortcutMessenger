package uk.co.streefland.rhys.finalyearproject.node;

import uk.co.streefland.rhys.finalyearproject.message.Streamable;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * The KeyId class stores and represents the 160 bit key that identifies every node on the network.
 */
public class KeyId implements Streamable, Serializable {

    public static final int ID_LENGTH = 160;   // Length of KeyId im bits
    public static final int BYTES_LENGTH = ID_LENGTH / 8; // Length of KeyId in bytes

    private byte[] idBytes;

    public KeyId() {
        idBytes = new byte[BYTES_LENGTH];
        new Random().nextBytes(idBytes);
    }

    public KeyId(String id) {

        /* Calculate SHA1 digest of input string - this creates a 160 bit byte array */
        MessageDigest digest = null;
        byte[] digestBytes = null;

        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.update(id.getBytes("utf8"));
            digestBytes = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        idBytes = digestBytes;

        if (idBytes.length != BYTES_LENGTH) {
            throw new IllegalArgumentException("Data needs to be " + BYTES_LENGTH + " characters long");
        }
    }

    public KeyId(byte[] idBytes) {
        if (idBytes.length != BYTES_LENGTH) {
            throw new IllegalArgumentException("Data needs to be " + BYTES_LENGTH + " characters long");
        }
        this.idBytes = idBytes;
    }

    public KeyId(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        out.write(getIdBytes());
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        byte[] input = new byte[BYTES_LENGTH];
        in.readFully(input);
        idBytes = input;
    }

    /**
     * Performs the XOR operation on this KeyId and another KeyId provided as the parameter. Used to calculate the distance from one KeyId to another
     *
     * @param nodeId The nodeId to be XOR'ed with the current KeyId
     * @return A KeyId object created with the XOR'ed array
     */
    private KeyId xor(KeyId nodeId) {
        byte[] nodeIdBytes = nodeId.getIdBytes();
        byte[] result = new byte[BYTES_LENGTH];

        /* XOR each byte of the arrays and store in the result array */
        for (int i = 0; i < BYTES_LENGTH; i++) {
            result[i] = (byte) (idBytes[i] ^ nodeIdBytes[i]);
        }

        return new KeyId(result);
    }

    /**
     * Generates a KeyId that is a certain distance away (in bits) from this KeyId
     *
     * @param distance the distance in bits
     * @return A KeyId object which is distance bits away from this KeyId
     */
    public KeyId generateNodeIdUsingDistance(int distance) {

        byte[] newNodeIdBytes = idBytes.clone(); // Clone the byte array so we don't modify it

        int bitsToFlip = distance; // Number of bits left to flip

        /* For each byte in byte array */
        for (int i = newNodeIdBytes.length - 1; i >= 0; i--) {
            /* For each bit in the byte */
            for (int j = 0; j <= 7; j++) {

                if (bitsToFlip > 0) {
                    /* Invert the bit */
                    if ((newNodeIdBytes[i] >> j & 1) == 1) {
                        newNodeIdBytes[i] &= ~(1 << j); // Clear the bit
                    } else {
                        newNodeIdBytes[i] |= (1 << j); // Set the bit
                    }
                    bitsToFlip--;
                } else {
                    /* No bits left to flip - return the array as a new KeyId object */
                    return new KeyId(newNodeIdBytes);
                }
            }
        }
        return new KeyId(newNodeIdBytes); // impossible to reach this
    }


    public int getFirstSetBitLocation() {
        int currentBit = 0;

        /* For each byte in byte array */
        for (int i = 0; i < idBytes.length; i++) {
            if (idBytes[i] == 0) {
                currentBit += 8; // save unnecessary processing if all bits are empty in the byte
            } else {
                /* For each bit in the byte */
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

    /**
     * Calculates the distances between two nodeIds in bits.
     *
     * @param otherNode The nodeId you would like to calculate the distance relative to
     * @return The distance in bits between the two nodes
     */
    public int getDistance(KeyId otherNode) {
        /* Gets the first set bit of the KeyId generated by the XOR method and subtracts it from ID_LENGTH (160)*/
        return ID_LENGTH - xor(otherNode).getFirstSetBitLocation();
    }

    @Override
    public int hashCode() {
        int hash = 4;
        hash = 37 * hash + Arrays.hashCode(idBytes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KeyId) {
            KeyId nid = (KeyId) obj;
            return this.hashCode() == nid.hashCode();
        } else {
            return false;
        }
    }

    /**
     * Returns the binary representation of the KeyId byte array split into the individual bytes. Very helpful for debugging
     *
     * @return The binary representation of the KeyId byte array
     */
    public String toBinary() {
        String output = "";
        for (byte b : idBytes) {
            output = output + " " + (Integer.toBinaryString(b & 255 | 256).substring(1));
        }
        return output;
    }

    /**
     * Returns the hex representation of the KeyId in a string
     *
     * @return hex representation of the KeyId in a string
     */
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

    public byte[] getIdBytes() {
        return idBytes;
    }

    /**
     * @return The BigInteger representation of the key
     */
    public BigInteger getInt() {
        return new BigInteger(1, getIdBytes());
    }


}