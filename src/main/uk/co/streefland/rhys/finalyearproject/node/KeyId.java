package uk.co.streefland.rhys.finalyearproject.node;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * The KeyId class stores and represents the 160 bit key that identifies every key on the network.
 * Keys are currently used to represent nodes and users on the network. Eventually they will also be used to represent
 * messages and other content
 */
public class KeyId implements Serializable {

    /* Network constants */
    public static final int ID_LENGTH = 160;   // Length of KeyId im bits
    private static final int BYTES_LENGTH = ID_LENGTH / 8; // Length of KeyId in bytes

    private byte[] idBytes; // the byte array that stores the KeyID

    /**
     * Default constructor - generates the KeyId randomly
     */
    public KeyId() {
        idBytes = new byte[BYTES_LENGTH];
        new Random().nextBytes(idBytes);
    }

    /**
     * Generates the KeyId based on the SHA1 digest of an input string
     *
     * @param id
     */
    public KeyId(String id) {

        /* Calculate SHA1 digest of input string - this creates a 160 bit byte array */
        MessageDigest digest;
        byte[] digestBytes = null;

        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.update(id.getBytes("utf8"));
            digestBytes = digest.digest();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        idBytes = digestBytes;

        if (idBytes.length != BYTES_LENGTH) {
            throw new IllegalArgumentException("Data needs to be " + BYTES_LENGTH + " characters long");
        }
    }

    /**
     * Stores a KeyId based on an existing 160 bit byte array
     *
     * @param idBytes
     */
    private KeyId(byte[] idBytes) {
        if (idBytes.length != BYTES_LENGTH) {
            throw new IllegalArgumentException("Data needs to be " + BYTES_LENGTH + " characters long");
        }
        this.idBytes = idBytes;
    }

    public KeyId(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    public void toStream(DataOutputStream out) throws IOException {
        out.write(getIdBytes());
    }

    private void fromStream(DataInputStream in) throws IOException {
        byte[] input = new byte[BYTES_LENGTH];
        in.readFully(input);
        idBytes = input;
    }

    /**
     * Performs the XOR operation on this KeyId and another KeyId provided as the parameter.
     * Used to calculate the distance from one KeyId to another
     *
     * @param keyId The KeyId to be XOR'ed with the current KeyId
     * @return A KeyId object created with the XOR'ed array
     */
    private KeyId xor(KeyId keyId) {
        byte[] keyIdBytes = keyId.getIdBytes();
        byte[] result = new byte[BYTES_LENGTH];

        /* XOR each byte of the arrays and store in the result array */
        for (int i = 0; i < BYTES_LENGTH; i++) {
            result[i] = (byte) (idBytes[i] ^ keyIdBytes[i]);
        }

        return new KeyId(result);
    }

    /**
     * Generates a KeyId that is a certain distance away (in bits) from this KeyId
     *
     * @param distance the distance in bits
     * @return A KeyId object which is distance bits away from this KeyId
     */
    public KeyId generateKeyIdUsingDistance(int distance) {

        byte[] newKeyIdBytes = idBytes.clone(); // Clone the byte array so we don't modify it

        int bitsToFlip = distance; // Number of bits left to flip

        /* For each byte in byte array */
        for (int i = newKeyIdBytes.length - 1; i >= 0; i--) {
            /* For each bit in the byte */
            for (int j = 0; j <= 7; j++) {

                if (bitsToFlip > 0) {
                    /* Invert the bit */
                    if ((newKeyIdBytes[i] >> j & 1) == 1) {
                        newKeyIdBytes[i] &= ~(1 << j); // Clear the bit
                    } else {
                        newKeyIdBytes[i] |= (1 << j); // Set the bit
                    }
                    bitsToFlip--;
                } else {
                    /* No bits left to flip - return the array as a new KeyId object */
                    return new KeyId(newKeyIdBytes);
                }
            }
        }
        return new KeyId(newKeyIdBytes); // impossible to reach this
    }

    /**
     * Calculates and returns the index of the first set bit in the byte array
     *
     * @return
     */
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
                    currentBit++; // Increment the current bit
                }
            }
        }
        return currentBit;
    }

    /**
     * Calculates the distances between two KeyIds in bits.
     *
     * @param otherKey The KeyId you would like to calculate the distance relative to
     * @return The distance in bits between the two keys
     */
    public int getDistance(KeyId otherKey) {
        /* Gets the first set bit of the KeyId generated by the XOR method and subtracts it from ID_LENGTH (160)*/
        return ID_LENGTH - xor(otherKey).getFirstSetBitLocation();
    }

    /**
     * Generates and returns the hashcode of the KeyId as an integer
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 4;
        hash = 37 * hash + Arrays.hashCode(idBytes);
        return hash;
    }

    /**
     * Compares two keys and returns true if they match
     *
     * @param obj
     * @return
     */
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
     * Returns the binary representation of the KeyId byte array split into the individual bytes.
     * Very helpful for the early stages of debugging but can likely be removed once proper testing has taken place.
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