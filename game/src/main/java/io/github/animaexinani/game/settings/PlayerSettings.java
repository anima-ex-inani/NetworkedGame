package io.github.animaexinani.game.settings;

/**
 * Root class for player settings, stored in TOML format.
 */
public class PlayerSettings {
    private String playerName = "Player";
    private NetworkSettings networking = new NetworkSettings();

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public NetworkSettings getNetworking() {
        return this.networking;
    }

    public void setNetworking(NetworkSettings networking) {
        this.networking = networking;
    }
}
