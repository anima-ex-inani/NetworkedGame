package io.github.animaexinani.engine.color;

public final class Color {
    public static final Color BLACK = new Color(0.0f, 0.0f, 0.0f, 1.0f);

    private float red;

    public float getRed() {
        return this.red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    private float green;

    public float getGreen() {
        return this.green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    private float blue;

    public float getBlue() {
        return this.blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    private float alpha;

    public float getAlpha() {
        return this.alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Color(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }
}
