package io.github.animaexinani.engine.assets.internal;

import io.github.animaexinani.engine.assets.Asset;
import io.github.animaexinani.engine.assets.AssetKey;
import io.github.animaexinani.engine.assets.AssetLoadingContext;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public final class AssetLoadingContextImpl extends AssetLoadingContext {
    private final @NotNull AssetManagerImpl manager;

    private final @NotNull AssetKey<? extends Asset> @NotNull [] loadStack;

    public @NotNull AssetKey<? extends Asset> @NotNull [] loadStack() {
        return this.loadStack;
    }

    @Override
    public <T extends Asset> Future<T> loadSubAsset(AssetKey<T> key) {
        return manager.loadSubasset(key, this);
    }

    public AssetLoadingContextImpl(@NotNull AssetManagerImpl manager, @NotNull AssetKey<?> @NotNull [] loadStack) {
        this.manager = manager;
        this.loadStack = loadStack;
    }
}
