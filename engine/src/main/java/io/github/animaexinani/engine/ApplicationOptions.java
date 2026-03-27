package io.github.animaexinani.engine;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public final class ApplicationOptions {
    @NotNull
    private String name;

    @NotNull
    public String getName() {
        return this.name;
    }

    public void setName(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    @NotNull
    private String version;

    @NotNull
    public String getVersion() {
        return this.version;
    }

    public void setVersion(@NotNull String version) {
        this.version = Objects.requireNonNull(version);
    }

    @NotNull
    private String identifier;

    @NotNull
    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(@NotNull String identifier) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    /**
     * @param name The name of the application.
     * @param version The version of the application. This can be a semantic version number, a date, or an entirely
     *                unstructured version text.
     * @param identifier An identifier for the application, in reverse-domain format.
     */
    public ApplicationOptions(@NotNull String name, @NotNull String version, @NotNull String identifier) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
        Objects.requireNonNull(identifier);

        this.name = name;
        this.version = version;
        this.identifier = identifier;
    }
}
