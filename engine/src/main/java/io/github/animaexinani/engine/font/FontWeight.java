package io.github.animaexinani.engine.font;

import org.jetbrains.annotations.NotNull;

/**
 * Font weight record for text rendering.
 */
public record FontWeight(int value) implements Comparable<FontWeight> {
    public FontWeight {
        if (value <= 0 || value > 1000) {
            throw new IllegalArgumentException("Font weight must be between 1 and 1000 (inclusive)");
        }
    }

    /**
     * Factory method for creating a {@link FontWeight} from an integer value.
     * 
     * @param value The weight value.
     * @return The corresponding predefined {@link FontWeight} if available,
     *         otherwise a new {@link FontWeight}.
     */
    public static @NotNull FontWeight of(int value) {
        return switch (value) {
            case 100 -> THIN;
            case 200 -> EXTRA_LIGHT;
            case 300 -> LIGHT;
            case 400 -> BOOK;
            case 500 -> MEDIUM;
            case 600 -> SEMI_BOLD;
            case 700 -> BOLD;
            case 800 -> EXTRA_BOLD;
            case 900 -> BLACK;
            case 950 -> EXTRA_BLACK;
            default -> new FontWeight(value);
        };
    }

    public static int compare(@NotNull FontWeight a, @NotNull FontWeight b) {
        return Integer.compare(a.value, b.value);
    }

    @Override
    public int compareTo(@NotNull FontWeight o) {
        return FontWeight.compare(this, o);
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
