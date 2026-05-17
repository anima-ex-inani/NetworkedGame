package io.github.animaexinani.engine.font;

/**
 * Font weight record for text rendering.
 */
public record FontWeight(int value) {
    public FontWeight {
        if (value <= 0 || value > 1000) {
            throw new IllegalArgumentException("Font weight must be between 1 and 1000 (inclusive)");
        }
    }

    /**
     * Weight 100 - Thin/Hairline
     */
    public static final FontWeight THIN = new FontWeight(100);

    /**
     * Weight 100 - Thin/Hairline
     */
    public static final FontWeight HAIRLINE = FontWeight.THIN;

    /**
     * Weight 200 - Extra Light/Ultra Light
     */
    public static final FontWeight EXTRA_LIGHT = new FontWeight(200);

    /**
     * Weight 200 - Extra Light/Ultra Light
     */
    public static final FontWeight ULTRA_LIGHT = FontWeight.EXTRA_LIGHT;

    /**
     * Weight 300 - Light
     */
    public static final FontWeight LIGHT = new FontWeight(300);

    /**
     * Weight 400 - Normal/Regular/Book
     */
    public static final FontWeight BOOK = new FontWeight(400);

    /**
     * Weight 400 - Normal/Regular/Book
     */
    public static final FontWeight REGULAR = FontWeight.BOOK;

    /**
     * Weight 400 - Normal/Regular/Book
     */
    public static final FontWeight NORMAL = FontWeight.BOOK;

    /**
     * Weight 500 - Medium
     */
    public static final FontWeight MEDIUM = new FontWeight(500);

    /**
     * Weight 600 - Semi Bold
     */
    public static final FontWeight SEMI_BOLD = new FontWeight(600);

    /**
     * Weight 700 - Bold
     */
    public static final FontWeight BOLD = new FontWeight(700);

    /**
     * Weight 800 - Extra Bold/Ultra Bold
     */
    public static final FontWeight EXTRA_BOLD = new FontWeight(800);

    /**
     * Weight 800 - Extra Bold/Ultra Bold
     */
    public static final FontWeight ULTRA_BOLD = FontWeight.EXTRA_BOLD;

    /**
     * Weight 900 - Black/Heavy/Poster
     */
    public static final FontWeight BLACK = new FontWeight(900);

    /**
     * Weight 900 - Black/Heavy/Poster
     */
    public static final FontWeight HEAVY = FontWeight.BLACK;

    /**
     * Weight 900 - Black/Heavy/Poster
     */
    public static final FontWeight POSTER = FontWeight.BLACK;

    /**
     * Weight 950 - Extra Black/Ultra Black
     */
    public static final FontWeight EXTRA_BLACK = new FontWeight(950);

    /**
     * Weight 950 - Extra Black/Ultra Black
     */
    public static final FontWeight ULTRA_BLACK = FontWeight.EXTRA_BLACK;
}
