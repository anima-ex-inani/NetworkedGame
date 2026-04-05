package io.github.animaexinani.engine.assets;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public record AssetKey<T extends Asset>(@NotNull Class<T> type, @NotNull String key) implements Serializable {
    public AssetKey {
        Objects.requireNonNull(type);
        Objects.requireNonNull(key);
    }
}
