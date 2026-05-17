package io.github.animaexinani.engine.font;

/**
 * Font weight record for text rendering.
 */
public record FontWeight(int value) {
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
    public static final FontWeight EXTRALIGHT = new FontWeight(200);

    /**
     * Weight 200 - Extra Light/Ultra Light
     */
    public static final FontWeight ULTRALIGHT = FontWeight.EXTRALIGHT;

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
    public static final FontWeight SEMIBOLD = new FontWeight(600);

    /**
     * Weight 700 - Bold
     */
    public static final FontWeight BOLD = new FontWeight(700);

    /**
     * Weight 800 - Extra Bold/Ultra Bold
     */
    public static final FontWeight EXTRABOLD = new FontWeight(800);

    /**
     * Weight 800 - Extra Bold/Ultra Bold
     */
    public static final FontWeight ULTRABOLD = FontWeight.EXTRABOLD;

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
    public static final FontWeight EXTRABLACK = new FontWeight(950);

    /**
     * Weight 950 - Extra Black/Ultra Black
     */
    public static final FontWeight ULTRABLACK = FontWeight.EXTRABLACK;
}
