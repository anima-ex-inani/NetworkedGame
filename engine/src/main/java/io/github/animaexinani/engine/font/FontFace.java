package io.github.animaexinani.engine.font;

import io.github.animaexinani.engine.assets.Asset;

public interface FontFace extends Asset {
    Font fontAt(float pixelSize, FontWeight weight, FontStyle style);
}
