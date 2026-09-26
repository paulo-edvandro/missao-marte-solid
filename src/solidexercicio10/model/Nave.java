package solidexercicio10.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Nave extends EntidadeMapa implements Movel {
    private final String id;
    private final int capacidade;
    private int vidas;
    private final List<Passageiro> passageiros = new ArrayList<>();

    public Nave(String id, int capacidade) {
        super(0, 0);
        this.id = Objects.requireNonNull(id, "id não pode ser nulo");
        if (capacidade < 0) {
            throw new IllegalArgumentException("capacidade não pode ser negativa");
        }
        this.capacidade = capacidade;
        this.vidas = 3;
    }

    public String getId() {
        return id;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getVidas() {
        return vidas;
    }

    public List<Passageiro> getPassageiros() {
        return Collections.unmodifiableList(passageiros);
    }

    public boolean embarcar(Passageiro passageiro) {
        Objects.requireNonNull(passageiro, "passageiro não pode ser nulo");
        if (passageiros.size() >= capacidade) {
            return false;
        }
        passageiros.add(passageiro);
        return true;
    }

    public void perderVida() {
        if (vidas > 0) {
            vidas--;
        }
    }

    @Override
    public void mover(int dx, int dy) {
        setPosicao(getX() + dx, getY() + dy);
    }

    public void moverComLimites(char comando, int minX, int maxX, int minY, int maxY) {
        validarLimites(minX, maxX, minY, maxY);
        int novoX = getX();
        int novoY = getY();
        switch (comando) {
            case 'w' -> novoY--;
            case 's' -> novoY++;
            case 'a' -> novoX--;
            case 'd' -> novoX++;
            default -> {
                return;
            }
        }
        if (novoX >= minX && novoX <= maxX && novoY >= minY && novoY <= maxY) {
            setPosicao(novoX, novoY);
        }
    }

    @Override
    public String getSimbolo() {
        return "N";
    }

    private static void validarLimites(int minX, int maxX, int minY, int maxY) {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Limites do mapa inválidos");
        }
    }
}
