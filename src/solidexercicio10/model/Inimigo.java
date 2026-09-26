package solidexercicio10.model;

import java.util.Objects;
import java.util.Random;

public class Inimigo extends EntidadeMapa implements Movel {
    public Inimigo(int x, int y) {
        super(x, y);
    }

    @Override
    public String getSimbolo() {
        return "I";
    }

    @Override
    public void mover(int dx, int dy) {
        setPosicao(getX() + dx, getY() + dy);
    }

    public void mover(Random random, int minX, int maxX, int minY, int maxY) {
        Objects.requireNonNull(random, "random não pode ser nulo");
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Limites do mapa inválidos");
        }

        int novoX = getX();
        int novoY = getY();
        switch (random.nextInt(4)) {
            case 0 -> novoX++;
            case 1 -> novoX--;
            case 2 -> novoY++;
            case 3 -> novoY--;
            default -> throw new IllegalStateException("Direção inválida");
        }

        if (novoX >= minX && novoX <= maxX
                && novoY >= minY && novoY <= maxY) {
            setPosicao(novoX, novoY);
        }
    }
}
