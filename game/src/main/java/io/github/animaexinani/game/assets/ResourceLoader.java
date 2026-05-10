package io.github.animaexinani.game.assets;

import io.github.animaexinani.engine.assets.*;
import io.github.animaexinani.engine.audio.AudioSource;
import io.github.animaexinani.game.util.function.ThrowingFunction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

public final class ResourceLoader extends AssetLoader {
    private static final Set<Class<? extends Asset>> SUPPORTED_TYPES = Set.of(Image.class, AudioSource.class);

    @Override
    public <T extends Asset> boolean supports(@NotNull Class<T> type) {
        return SUPPORTED_TYPES.contains(type);
    }

    private final Map<Class<? extends Asset>, ThrowingFunction<InputStream, ? extends Asset, IOException>> processMapping = Map.of(
        Image.class, ImageProcessor::processData,
        AudioSource.class, AudioProcessor::processData
    );

    @Override
    public <T extends Asset> T load(@NotNull AssetKey<T> key, @NotNull AssetLoadingContext context) throws IOException {
        if (!SUPPORTED_TYPES.contains(key.type())) {
            throw new UnsupportedFormatException("This loader only supports: " + SUPPORTED_TYPES);
        }

        try (var stream = ResourceLoader.class.getResourceAsStream(key.key())) {
            if (stream == null) {
                throw new MissingResourceException("Resource does not exist in the classpath", ResourceLoader.class.getName(), key.key());
            }

            //noinspection unchecked
            return (T) this.processMapping.get(key.type()).apply(stream);
        }
    }
}
