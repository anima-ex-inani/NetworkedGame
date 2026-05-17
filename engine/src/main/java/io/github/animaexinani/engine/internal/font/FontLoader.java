package io.github.animaexinani.engine.internal.font;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.animaexinani.engine.assets.Asset;
import io.github.animaexinani.engine.assets.AssetKey;
import io.github.animaexinani.engine.assets.AssetLoader;
import io.github.animaexinani.engine.assets.AssetLoadingContext;
import io.github.animaexinani.engine.assets.UnsupportedFormatException;
import io.github.animaexinani.engine.font.Font;
import io.github.animaexinani.engine.font.FontFace;

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
        return type.equals(Font.class) || type.equals(FontFace.class);
    }

    @Override
    public <T extends Asset> T load(@NotNull AssetKey<T> key, @NotNull AssetLoadingContext context) throws IOException {
        if (!this.supports(key.type())) {
            throw new UnsupportedFormatException("This loader only supports Font and FontFace assets");
        }

        try (var stream = FontLoader.class.getResourceAsStream(key.key())) {
            if (stream == null) {
                throw new IOException("Font resource not found: " + key.key());
            }

            byte[] bytes = stream.readAllBytes();
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
            buffer.put(bytes).flip();

            // The FreeTypeFontFace will keep a reference to the buffer to prevent it from being GC'd
            // while the FreeType library is using it.
            return (T) new FreeTypeFontFace(buffer, this.freetypeLibrary());
        }
    }
}
