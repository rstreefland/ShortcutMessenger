package uk.co.streefland.rhys.finalyearproject.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IPTools {

    String publicIp;
    String privateIp;

    public IPTools() throws IOException {
        publicIp = determinePublicIp();
        privateIp = determinePrivateIp();
    }

    public String determinePublicIp() throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public String determinePrivateIp() throws SocketException {
        List<InetAddress> ipAddresses = new ArrayList<>();

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

        if (ipAddresses.size() > 1) {
            return ipAddresses.get(ipAddresses.size() - 1).getHostAddress();
        } else if (ipAddresses.size() == 1) {
            return ipAddresses.get(0).getHostAddress();
        } else {
            return null;
        }
    }

    public InetAddress validateAddress(String host) throws UnknownHostException {
        return InetAddress.getByName(host);
    }

    public String getPublicIp() {
        return publicIp;
    }

    public String getPrivateIp() {
        return privateIp;
    }
}
