package io.github.animaexinani.game.attributes;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a modifier to an attribute.
 * 
 * @param id the unique identifier of the attribute modifier
 * @param value the value of the attribute modifier
 * @param operation the operation to perform on the attribute
 */
public record AttributeModifier(@NotNull UUID id, float value, @NotNull AttributeModifierOperation operation) {
}
