package io.github.animaexinani.engine.assets;

import java.io.IOException;

public abstract class AssetLoader {
    /**
     * Checks if the loader supports loading the given asset type.
     * @param type The type of the asset to check
     * @return <code>true</code> if the loader supports loading the asset, <code>false</code> otherwise.
     * @param <T> The type of the asset to check
     */
    public abstract <T extends Asset> boolean supports(Class<T> type);

    /**
     * Loads an asset.
     * @param key The key to the asset
     * @param context The context in which the asset is being loaded
     * @return The loaded asset
     * @param <T> The type of the asset to load
     * @throws IOException if an I/O error occurs while loading the asset
     *
     * @implSpec
     * If the loader does not support loading the format of the asset (e.g.: a texture loader which does not support
     * JPEG images), this function should throw an {@link UnsupportedFormatException}. The engine will catch these
     * exceptions to delegate loading to a different loader.
     */
    public abstract <T extends Asset> T load(AssetKey<T> key, AssetLoadingContext context)
        throws IOException;
}
