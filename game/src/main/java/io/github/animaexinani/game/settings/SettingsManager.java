package io.github.animaexinani.game.settings;

import tools.jackson.dataformat.toml.TomlMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages loading and saving of player settings using Jackson TOML.
 */
public class SettingsManager {
    private static final Logger LOGGER = Logger.getLogger(SettingsManager.class.getName());
    private static final Path SETTINGS_PATH = Paths.get("settings.toml");
    
    private final TomlMapper mapper;
    private PlayerSettings settings;

    public SettingsManager() {
        this.mapper = new TomlMapper();
        this.settings = this.load();
    }

    /**
     * Loads settings from the file. If the file doesn't exist, returns default settings.
     * @return the loaded or default settings
     */
    public PlayerSettings load() {
        if (Files.exists(SETTINGS_PATH)) {
            try {
                return this.mapper.readValue(SETTINGS_PATH.toFile(), PlayerSettings.class);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to load settings from " + SETTINGS_PATH, e);
            }
        }
        return new PlayerSettings();
    }

    /**
     * Saves the current settings to the file.
     */
    public void save() {
        try {
            this.mapper.writeValue(SETTINGS_PATH.toFile(), this.settings);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save settings to " + SETTINGS_PATH, e);
        }
    }

    public PlayerSettings getSettings() {
        return this.settings;
    }

    public void setSettings(PlayerSettings settings) {
        this.settings = settings;
    }
}
