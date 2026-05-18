package io.github.animaexinani.engine.font;

/**
 * Represents the origin point of a text component for layout purposes.
 */
public enum TextOrigin {
    /**
     * Aligns to the top-left of the text. 
     * X is at the beginning of the text, Y is at the font's ascender (top bound).
     */
    TOP_LEFT,
    
    /**
     * Aligns to the top-center of the text. 
     * X is at the horizontal center of the text, Y is at the font's ascender (top bound).
     */
    TOP_CENTER,
    
    /**
     * Aligns to the top-right of the text. 
     * X is at the end of the text, Y is at the font's ascender (top bound).
     */
    TOP_RIGHT,
    
    /**
     * Aligns to the center-left of the text. 
     * X is at the beginning of the text, Y is at the vertical center of the text bounds.
     */
    CENTER_LEFT,
    
    /**
     * Aligns to the exact center of the text. 
     * X is at the horizontal center of the text, Y is at the vertical center of the text bounds.
     */
    CENTER,
    
    /**
     * Aligns to the center-right of the text. 
     * X is at the end of the text, Y is at the vertical center of the text bounds.
     */
    CENTER_RIGHT,
    
    /**
     * Aligns to the bottom-left of the text. 
     * X is at the beginning of the text, Y is at the font's descender (bottom bound).
     */
    BOTTOM_LEFT,
    
    /**
     * Aligns to the bottom-center of the text. 
     * X is at the horizontal center of the text, Y is at the font's descender (bottom bound).
     */
    BOTTOM_CENTER,
    
    /**
     * Aligns to the bottom-right of the text. 
     * X is at the end of the text, Y is at the font's descender (bottom bound).
     */
    BOTTOM_RIGHT,
    
    /**
     * Aligns to the baseline-left of the text. 
     * X is at the beginning of the text, Y is exactly on the font's baseline.
     */
    BASELINE_LEFT,
    
    /**
     * Aligns to the baseline-center of the text. 
     * X is at the horizontal center of the text, Y is exactly on the font's baseline.
     */
    BASELINE_CENTER,
    
    /**
     * Aligns to the baseline-right of the text. 
     * X is at the end of the text, Y is exactly on the font's baseline.
     */
    BASELINE_RIGHT
}
