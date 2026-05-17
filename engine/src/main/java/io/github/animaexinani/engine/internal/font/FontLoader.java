package io.github.animaexinani.engine.internal.font;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.animaexinani.engine.assets.Asset;
import io.github.animaexinani.engine.assets.AssetKey;
import io.github.animaexinani.engine.assets.AssetLoader;
import io.github.animaexinani.engine.assets.AssetLoadingContext;
import io.github.animaexinani.engine.assets.UnsupportedFormatException;
import io.github.animaexinani.engine.font.Font;

public class FontLoader extends AssetLoader {
    private @Nullable FreetypeLibrary freetypeLibrary;

    private @NotNull FreetypeLibrary freetypeLibrary() {
        if (this.freetypeLibrary == null) {
            this.freetypeLibrary = new FreetypeLibrary();
        }
        return this.freetypeLibrary;
    }

    @Override
    public <T extends Asset> boolean supports(@NotNull Class<T> type) {
        return type.equals(Font.class);
    }

    @Override
    public <T extends Asset> T load(@NotNull AssetKey<T> key, @NotNull AssetLoadingContext context) throws IOException {
        if (!this.supports(key.type())) {
            throw new UnsupportedFormatException("This loader only supports Font assets");
        }

        throw new UnsupportedOperationException("Font loading is not yet implemented");
    }
    
}
