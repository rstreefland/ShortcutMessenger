package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IPTools {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    boolean isConnected = false;
    String publicIp;
    String privateIp;

    public IPTools() throws IOException {
        checkConnectivity();

        if (isConnected) {
            publicIp = determinePublicIp();
        }

        privateIp = determinePrivateIp();
    }

    public void checkConnectivity() {
       // InetAddress testAddress = InetAddress.getByAddress(new byte[] {62, 252,73,93});
       // isConnected = testAddress.isReachable(2000);

        try {
            Socket s = new Socket("8.8.8.8", 53);
            isConnected = true;
            s.close();
        } catch (IOException e) {
            isConnected = false;
        }

        if (isConnected) {
            logger.info("Internet connection is operational");
        } else {
            logger.warn("Internet connection failure");
        }
    }

    /**
     * Determines the node's public IP by querying a website
     *
     * @return The node's public IP as a string
     */
    public String determinePublicIp() {

        String[] urls = new String[3];
        urls[0] = "http://checkip.amazonaws.com";
        urls[1] = "https://api.ipify.org/";
        urls[2] = "https://wtfismyip.com/text";

        String ip = null;
        BufferedReader in = null;

        /* Check a different source if one fails */
        for (int i = 0; i < 3; i++) {
            try {
                /*URL ipUrl = new URL(urls[i]);
                in = new BufferedReader(new InputStreamReader(
                        ipUrl.openStream()));
                ip = in.readLine();*/

                URL url = new URL(urls[i]);
                URLConnection con = url.openConnection();

                con.setConnectTimeout(1000);
                con.setReadTimeout(1000);

                InputStream inputStream = con.getInputStream();
                in = new BufferedReader(new InputStreamReader(inputStream));
                ip = in.readLine();
            } catch (IOException e) {
                logger.warn("couldn't get IP from source:" + i);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (ip != null) {
                logger.info("Public IP address is: {}", ip);
                return ip;
            }
        }
        logger.warn("Could not determine public IP");
        return null;
    }

    /**
     * Determines the node's private IP by selecting the IP address of network interface that is most
     * likely to be the primary interfaces
     *
     * @return
     * @throws SocketException
     */
    public String determinePrivateIp() throws SocketException {
        List<InetAddress> ipAddresses = new ArrayList<>();

        /* Loop through network interfaces adding all suitable InetAdresses to an ArrayList*/
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements(); ) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress() && addr.isSiteLocalAddress()) {
                    if (addr instanceof Inet4Address) {
                        ipAddresses.add(addr);
                    }
                }
            }
        }

        /* Pick the last IP address */
        if (ipAddresses.size() > 0) {
            logger.info("Private IP address is: {}", ipAddresses.get(ipAddresses.size() - 1).getHostAddress());
            return ipAddresses.get(ipAddresses.size() - 1).getHostAddress();
        } else {
            logger.warn("Could not determine private IP");
            return null;
        }
    }

    /**
     * Converts a IP/URL into an InetAddress
     *
     * @param host The IP/URL of the host
     * @return
     * @throws UnknownHostException
     */
    public InetAddress validateAddress(String host) throws UnknownHostException {
        return InetAddress.getByName(host);
    }

    public String getPublicIp() {
        return publicIp;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public InetAddress getPublicInetAddress() throws UnknownHostException {
        return InetAddress.getByName(publicIp);
    }

    public InetAddress getPrivateInetAddress() throws UnknownHostException {
        return InetAddress.getByName(publicIp);
    }

    public boolean isConnected() {
        return isConnected;
    }
}
