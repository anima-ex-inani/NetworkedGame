package io.github.animaexinani.game.assets;

import io.github.animaexinani.engine.assets.*;
import io.github.animaexinani.game.util.function.ThrowingFunction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.MissingResourceException;

public final class ResourceLoader extends AssetLoader {
    @Override
    public <T extends Asset> boolean supports(@NotNull Class<T> type) {
        return type.equals(Image.class);
    }

    private final Map<Class<? extends Asset>, ThrowingFunction<InputStream, ? extends Asset, IOException>> processMapping = Map.of(
        Image.class, ImageProcessor::processData
    );

    @Override
    public <T extends Asset> T load(@NotNull AssetKey<T> key, @NotNull AssetLoadingContext context) throws IOException {
        if (key.type() != Image.class) {
            throw new UnsupportedFormatException("This loader only supports image assets");
        }

        try (var stream = ResourceLoader.class.getResourceAsStream(key.key())) {
            if (stream == null) {
                throw new MissingResourceException("Resource does not exist in the classpath", ResourceLoader.class.getName(), key.key());
            }

            //noinspection unchecked
            return (T) processMapping.get(key.type()).apply(stream);
        }
    }
}
