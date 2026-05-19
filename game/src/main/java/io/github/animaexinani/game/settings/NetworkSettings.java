package io.github.animaexinani.game.settings;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the networking settings for the player.
 */
public class NetworkSettings {
    private static final Logger LOGGER = Logger.getLogger(NetworkSettings.class.getName());
    
    private int preferredPort = 12345;
    private String networkInterface = "all";

    public int getPreferredPort() {
        return this.preferredPort;
    }

    public void setPreferredPort(int preferredPort) {
        this.preferredPort = preferredPort;
    }

    public String getNetworkInterface() {
        return this.networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
        this.validateNetworkInterface();
    }

    /**
     * Validates the network interface setting and falls back to "all" if the specified interface doesn't exist.
     */
    public void validateNetworkInterface() {
        if ("all".equals(this.networkInterface)) {
            return;
        }

        try {
            var nets = NetworkInterface.getNetworkInterfaces();
            var netsIterator = nets.asIterator();
            boolean interfaceExists = false;
            while (netsIterator.hasNext()) {
                var netint = netsIterator.next();
                if (netint.isUp() && netint.getName().equals(this.networkInterface)) {
                    interfaceExists = true;
                    break;
                }
            }

            if (!interfaceExists) {
                LOGGER.log(Level.WARNING, "Network interface {0} not found, falling back to all interfaces", this.networkInterface);
                this.networkInterface = "all";
            }
        } catch (SocketException e) {
            LOGGER.log(Level.WARNING, "Failed to validate network interface, falling back to all interfaces", e);
            this.networkInterface = "all";
        }
    }
}
