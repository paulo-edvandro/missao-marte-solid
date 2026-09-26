package solidexercicio10.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Missao {
    private final Nave nave;
    private final List<Passageiro> passageiros = new ArrayList<>();
    private final List<Asteroide> asteroides = new ArrayList<>();
    private final List<Inimigo> inimigos = new ArrayList<>();

    public Missao(Nave nave) {
        this.nave = Objects.requireNonNull(nave, "nave não pode ser nula");
    }

    public Nave getNave() {
        return nave;
    }

    public List<Passageiro> getPassageiros() {
        return Collections.unmodifiableList(passageiros);
    }

    public List<Asteroide> getAsteroides() {
        return Collections.unmodifiableList(asteroides);
    }

    public List<Inimigo> getInimigos() {
        return Collections.unmodifiableList(inimigos);
    }

    public void adicionarPassageiro(Passageiro passageiro) {
        passageiros.add(Objects.requireNonNull(passageiro, "passageiro não pode ser nulo"));
    }

    public void adicionarAsteroide(Asteroide asteroide) {
        asteroides.add(Objects.requireNonNull(asteroide, "asteroide não pode ser nulo"));
    }

    public void adicionarInimigo(Inimigo inimigo) {
        inimigos.add(Objects.requireNonNull(inimigo, "inimigo não pode ser nulo"));
    }

    public Passageiro passagemNaPosicao() {
        return passageiros.stream()
                .filter(passageiro -> mesmaPosicao(passageiro, nave))
                .findFirst()
                .orElse(null);
    }

    public boolean embarcarPassageiroNaPosicao() {
        Passageiro passageiro = passagemNaPosicao();
        if (passageiro == null || !nave.embarcar(passageiro)) {
            return false;
        }
        return passageiros.remove(passageiro);
    }

    public void moverInimigos(Random random, int minX, int maxX, int minY, int maxY) {
        Objects.requireNonNull(random, "random não pode ser nulo");
        inimigos.forEach(inimigo -> inimigo.mover(random, minX, maxX, minY, maxY));
    }

    public boolean verificaColisao() {
        return asteroides.stream().anyMatch(asteroide -> mesmaPosicao(asteroide, nave))
                || inimigos.stream().anyMatch(inimigo -> mesmaPosicao(inimigo, nave));
    }

    public boolean todosEmbarcados() {
        return passageiros.isEmpty();
    }

    private static boolean mesmaPosicao(Posicionavel primeiro, Posicionavel segundo) {
        return primeiro.getX() == segundo.getX() && primeiro.getY() == segundo.getY();
    }
}
