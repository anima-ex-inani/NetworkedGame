package io.github.animaexinani.engine;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ApplicationOptions(
    @NotNull String name,
    @NotNull String version,
    @NotNull String identifier,
    @Nullable String creator,
    @Nullable String copyright,
    @Nullable String url
) {
    public ApplicationOptions {
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
        Objects.requireNonNull(identifier);
    }

    public ApplicationOptions(@NotNull String name, @NotNull String version, @NotNull String identifier) {
        this(name, version, identifier, null, null, null);
    }
}
