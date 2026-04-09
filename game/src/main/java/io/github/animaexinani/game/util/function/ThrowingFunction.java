package io.github.animaexinani.game.util.function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
