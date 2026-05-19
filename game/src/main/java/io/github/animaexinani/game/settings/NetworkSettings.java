package io.github.animaexinani.game.settings;

/**
 * Represents the networking settings for the player.
 */
public class NetworkSettings {
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
    }
}
