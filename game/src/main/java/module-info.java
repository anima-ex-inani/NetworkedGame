module io.github.animaexinani.game {
    requires transitive org.jetbrains.annotations;

    requires io.github.animaexinani.engine;
    requires org.dyn4j;
    requires java.logging;
    requires java.desktop;

    requires tools.jackson.databind;
    requires tools.jackson.dataformat.toml;

    opens io.github.animaexinani.game.settings to tools.jackson.databind;
}
