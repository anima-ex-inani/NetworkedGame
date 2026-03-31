module io.github.animaexinani.engine {
    requires org.lwjgl;
    requires org.lwjgl.sdl;

    requires transitive org.jetbrains.annotations;

    exports io.github.animaexinani.engine;
    exports io.github.animaexinani.engine.color;
    exports io.github.animaexinani.engine.listeners;
    exports io.github.animaexinani.engine.point;
    exports io.github.animaexinani.engine.rendering;
    exports io.github.animaexinani.engine.size;
    exports io.github.animaexinani.engine.texture;
    exports io.github.animaexinani.engine.vertex;
    exports io.github.animaexinani.engine.windowing;
}
