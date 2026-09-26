package solidexercicio10.model;

import java.util.Objects;

public abstract class Passageiro extends EntidadeMapa {
    private final String nome;
    private final String tipo;

    protected Passageiro(String nome, String tipo, int x, int y) {
        super(x, y);
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "tipo não pode ser nulo");
    }

    public final String getNome() {
        return nome;
    }

    public final String getTipo() {
        return tipo;
    }

    @Override
    public String getSimbolo() {
        return "P";
    }

    public abstract int getPontuacao();
}
