package io.github.animaexinani.game.assets;

import io.github.animaexinani.engine.assets.AssetKey;
import io.github.animaexinani.engine.assets.UnsupportedFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.MissingResourceException;

import static org.junit.jupiter.api.Assertions.*;

class ResourceLoaderTest {

    private ResourceLoader loader;

    @BeforeEach
    void setUp() {
        this.loader = new ResourceLoader();
    }

    // --- supports ---

    @Test
    void supports_imageClass_returnsTrue() {
        assertTrue(this.loader.supports(Image.class));
    }

    @Test
    void supports_objectClass_returnsFalse() {
        assertFalse(this.loader.supports(Object.class));
    }

    @Test
    void supports_assetBaseInterface_returnsFalse() {
        // The changed code uses type.equals(Image.class), not isAssignableFrom
        assertFalse(this.loader.supports(io.github.animaexinani.engine.assets.Asset.class));
    }

    // --- load with unsupported type ---

    @Test
    void load_nonImageType_throwsUnsupportedFormatException() {
        AssetKey<io.github.animaexinani.engine.assets.Asset> badKey =
            new AssetKey<>(io.github.animaexinani.engine.assets.Asset.class, "/some/path");
        // context is not used before the type check, so null is safe here
        assertThrows(UnsupportedFormatException.class,
            () -> this.loader.load(badKey, null));
    }

    // --- load with missing resource ---

    @Test
    void load_imageKey_missingResource_throwsMissingResourceException() {
        AssetKey<Image> key = new AssetKey<>(Image.class, "/this/resource/does/not/exist.png");
        // context is not used before the missing-resource check, so null is safe
        assertThrows(MissingResourceException.class,
            () -> this.loader.load(key, null));
    }

    // --- error message for unsupported type ---

    @Test
    void load_unsupportedType_exceptionMessageMentionsImages() {
        AssetKey<io.github.animaexinani.engine.assets.Asset> badKey =
            new AssetKey<>(io.github.animaexinani.engine.assets.Asset.class, "/path");
        UnsupportedFormatException ex = assertThrows(UnsupportedFormatException.class,
            () -> this.loader.load(badKey, null));
        assertTrue(ex.getMessage().toLowerCase().contains("image"),
            "Expected exception message to mention 'image' but was: " + ex.getMessage());
    }
}