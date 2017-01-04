package uk.co.streefland.rhys.finalyearproject.node;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the KeyId class
 */
public class KeyIdTest {

    private KeyId key1 = new KeyId("sdlkfhjsd");
    private KeyId key2 = new KeyId("iuqaghjvfldfsdf");
    private KeyId key3 = new KeyId("sjkld");

    @Test
    public void testHashcode() throws Exception {
        assertEquals(key1.hashCode(), -1478346900);
        assertEquals(key2.hashCode(), -265373120);
        assertEquals(key3.hashCode(), 672281216);
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(key1.equals(key1));
        assertTrue(key2.equals(key2));
        assertTrue(key3.equals(key3));

        assertFalse(key1.equals(key2));
        assertFalse(key2.equals(key3));
        assertFalse(key3.equals(key1));
    }

    @Test
    public void testKeyIdGeneration() throws Exception {
        String key1Sha = key1.toString();
        String key2Sha = key2.toString();
        String key3Sha = key3.toString();

        assertEquals("696e88685917b4790d10352f2c2b1c28479c3143".toUpperCase(), key1Sha);
        assertEquals("0e4b3ef46e2c70fed23dc492888160b55f646c6c".toUpperCase(), key2Sha);
        assertEquals("fa8732816f63f1c66f8347a1a657959d837f7255".toUpperCase(), key3Sha);
    }

    @Test
    public void testGenerateKeyIdUsingDistance() throws Exception {
        KeyId newKey1 = key1.generateKeyIdUsingDistance(5);
        KeyId newKey2 = key2.generateKeyIdUsingDistance(41);
        KeyId newKey3 = key3.generateKeyIdUsingDistance(89);

        assertEquals(newKey1.getDistance(key1), 5);
        assertEquals(newKey2.getDistance(key2), 41);
        assertEquals(newKey3.getDistance(key3), 89);
    }

    @Test
    public void testGetFirstSetBitLocation() throws Exception {
        assertEquals(key1.getFirstSetBitLocation(), 1);
        assertEquals(key2.getFirstSetBitLocation(), 4);
        assertEquals(key3.getFirstSetBitLocation(), 0);
    }

    @Test
    public void testGetDistance() throws Exception {
        KeyId newKey = key2.generateKeyIdUsingDistance(23);

        assertEquals(key1.getDistance(key2), 159);
        assertEquals(key2.getDistance(key3), 160);
        assertEquals(key3.getDistance(key1), 160);
        assertEquals(key2.getDistance(newKey), 23);
    }

}