package io.github.animaexinani.engine.color;

public record Color(float red, float green, float blue, float alpha) {
    public static final Color BLACK = new Color(0.0f, 0.0f, 0.0f, 1.0f);

    public Color {
        red = StrictMath.clamp(red, 0.0F, 1.0F);
        green = StrictMath.clamp(green, 0.0F, 1.0F);
        blue = StrictMath.clamp(blue, 0.0F, 1.0F);
        alpha = StrictMath.clamp(alpha, 0.0F, 1.0F);
    }
}
