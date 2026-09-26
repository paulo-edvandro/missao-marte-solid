package solidexercicio10.model;

import java.text.Normalizer;
import java.util.Locale;

public enum Dificuldade {
    FACIL,
    MEDIO,
    DIFICIL;

    public static Dificuldade deString(String valor) {
        if (valor == null) {
            return MEDIO;
        }

        String normalizado = Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return switch (normalizado) {
            case "facil" -> FACIL;
            case "dificil" -> DIFICIL;
            default -> MEDIO;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case FACIL -> "Fácil";
            case MEDIO -> "Médio";
            case DIFICIL -> "Difícil";
        };
    }
}
