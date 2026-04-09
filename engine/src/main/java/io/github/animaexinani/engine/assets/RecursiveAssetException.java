package io.github.animaexinani.engine.assets;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RecursiveAssetException extends IllegalStateException {
    private final @NotNull AssetKey<? extends Asset> @NotNull [] keys;

    public @NotNull AssetKey<? extends Asset> @NotNull [] assetCycle() {
        return this.keys;
    }

    private static @NotNull String formatAssetCycle(@NotNull AssetKey<?> @NotNull [] keys) {
        return Arrays.stream(keys).map(Object::toString).collect(Collectors.joining(" -> ", "Circular dependency detected:", ""));
    }

    public RecursiveAssetException(@NotNull AssetKey<?> @NotNull ... keys) {
        super(formatAssetCycle(keys));
        this.keys = keys;
    }
}
