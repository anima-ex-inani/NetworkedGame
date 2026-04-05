package io.github.animaexinani.engine.assets;

import io.github.animaexinani.engine.internal.assets.AssetLoadingContextImpl;

import java.util.concurrent.Future;

public abstract sealed class AssetLoadingContext permits AssetLoadingContextImpl {
    /**
     * Loads an asset used inside another asset.
     * @param key The key to the asset
     * @return A future representing the load operation
     * @param <T> The type of the asset to load
     */
    public abstract <T extends Asset> Future<T> loadSubAsset(AssetKey<T> key);
}
