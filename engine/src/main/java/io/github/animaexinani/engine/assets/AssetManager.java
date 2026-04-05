package io.github.animaexinani.engine.assets;

import io.github.animaexinani.engine.assets.internal.AssetManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public abstract sealed class AssetManager permits AssetManagerImpl {
    protected final @NotNull List<@NotNull AssetLoader> loaders = new CopyOnWriteArrayList<>();

    public final boolean registerLoader(@NotNull AssetLoader loader) {
        Objects.requireNonNull(loader);
        return this.loaders.add(loader);
    }

    public final boolean removeLoader(@NotNull AssetLoader loader) {
        Objects.requireNonNull(loader);
        return this.loaders.remove(loader);
    }

    public abstract <T extends Asset> Future<T> load(AssetKey<T> key);
}
