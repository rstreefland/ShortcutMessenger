package uk.co.streefland.rhys.finalyearproject.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IPDiscovery {

    String externalIp;
    String internalIp;

    public IPDiscovery() throws IOException {
        externalIp = determineExternalIp();
        internalIp = determineInternalIp();
    }

    public String determineExternalIp() throws IOException {
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

    public String determineInternalIp() throws SocketException {
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

    public String getExternalIp() {
        return externalIp;
    }

    public String getInternalIp() {
        return internalIp;
    }
}
