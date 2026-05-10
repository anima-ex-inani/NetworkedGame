package io.github.animaexinani.game.attributes;

/**
 * Represents the different operations that can be used to apply attribute modifiers.
 */
public enum AttributeModifierOperation {
    /**
     * Adds the value of the attribute modifier to the attribute's base value before multipliers.
     */
    ADD_BASE_EARLY,

    /**
     * Multiplies the attribute's base value by the value of the attribute modifier, stacking
     * additively with other multipliers.
     */
    MULTIPLY_BASE_ADDITIVE,

    /**
     * Multiplies the attribute's base value by the value of the attribute modifier, stacking
     * multiplicatively with other multipliers.
     */
    MULTIPLY_BASE_MULTIPLICATIVE,

    /**
     * Adds the value of the attribute modifier to the attribute's base value after multipliers.
     */
    ADD_BASE_LATE,

    /**
     * Clamps the attribute's base value to above the value of the attribute modifier.
     */
    MIN_BASE,

    /**
     * Clamps the attribute's base value to below the value of the attribute modifier.
     */
    MAX_BASE,

    /**
     * Sets the attribute's base value to the value of the attribute modifier, overriding other
     * modifiers.
     */
    SET_BASE,

    /**
     * Adds the value of the attribute modifier to the attribute's value before multipliers.
     */
    ADD_TOTAL_EARLY,

    /**
     * Multiplies the attribute's value by the value of the attribute modifier, stacking
     * additively with other multipliers.
     */
    MULTIPLY_TOTAL_ADDITIVE,

    /**
     * Multiplies the attribute's value by the value of the attribute modifier, stacking
     * multiplicatively with other multipliers.
     */
    MULTIPLY_TOTAL_MULTIPLICATIVE,

    /**
     * Adds the value of the attribute modifier to the attribute's value after multipliers.
     */
    ADD_TOTAL_LATE,

    /**
     * Clamps the attribute's value to above the value of the attribute modifier.
     */
    MIN_TOTAL,

    /**
     * Clamps the attribute's value to below the value of the attribute modifier.
     */
    MAX_TOTAL,

    /**
     * Sets the attribute's value to the value of the attribute modifier, overriding other
     * modifiers.
     */
    SET_TOTAL
}
