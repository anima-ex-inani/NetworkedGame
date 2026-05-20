package io.github.animaexinani.game.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * Root class for player settings, stored in TOML format.
 */
public class PlayerSettings {
    private String playerName = "Player";
    private NetworkSettings networking = new NetworkSettings();
    private Map<String, Integer> keybinds = new HashMap<>();

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

    public Map<String, Integer> getKeybinds() {
        return this.keybinds;
    }

    public void setKeybinds(Map<String, Integer> keybinds) {
        this.keybinds = keybinds;
    }
}
