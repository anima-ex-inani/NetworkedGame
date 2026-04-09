package io.github.animaexinani.game.assets;

import io.github.animaexinani.engine.assets.Asset;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.PixelFormat;

import java.nio.ByteBuffer;

public record Image(ByteBuffer pixelData, Size size, PixelFormat pixelFormat) implements Asset {
    @Override
    public boolean isValid() {
        return true;
    }

}
