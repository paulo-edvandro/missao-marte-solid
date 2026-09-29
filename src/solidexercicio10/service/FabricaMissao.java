package solidexercicio10.service;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Astronauta;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Engenheiro;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Passageiro;
import solidexercicio10.model.Posicionavel;
import solidexercicio10.model.Professor;

/** Monta a missão. Os tipos concretos vêm do catálogo, sem switch no fluxo do jogo. */
public final class FabricaMissao {
    private final List<BiFunction<Integer, Integer, Passageiro>> catalogo;

    public FabricaMissao(List<BiFunction<Integer, Integer, Passageiro>> catalogo) {
        this.catalogo = List.copyOf(catalogo);
        if (this.catalogo.size() < 5) {
            throw new IllegalArgumentException("O catálogo precisa de pelo menos cinco passageiros");
        }
    }

    /** Configuração padrão preserva tipos, nomes e ordem do jogo original. */
    public static FabricaMissao padrao() {
        return new FabricaMissao(List.of(
                (x, y) -> new Professor("Dr. Silva", x, y),
                (x, y) -> new Engenheiro("Eng. Rosa", x, y),
                (x, y) -> new Professor("Dr. Lima", x, y),
                (x, y) -> new Engenheiro("Eng. Carlos", x, y),
                (x, y) -> new Astronauta("Ast. Maria", x, y)));
    }

    public Missao criar(Dificuldade dificuldade, Random random, int minX, int maxX, int minY, int maxY) {
        Objects.requireNonNull(dificuldade);
        Objects.requireNonNull(random);
        if (minX > 0 || maxX < 0 || minY > 0 || maxY < 0) {
            throw new IllegalArgumentException("O mapa precisa incluir a plataforma em (0,0)");
        }
        int passageiros = dificuldade == Dificuldade.FACIL ? 4 : catalogo.size();
        int asteroides = dificuldade == Dificuldade.FACIL ? 1 : dificuldade == Dificuldade.DIFICIL ? 3 : 2;
        long celulas = ((long) maxX - minX + 1) * ((long) maxY - minY + 1);
        if (celulas < 1L + passageiros + asteroides * 2L) {
            throw new IllegalArgumentException("Mapa sem espaço para a missão configurada");
        }
        Missao missao = new Missao(new Nave("A-1", catalogo.size()));
        for (int i = 0; i < passageiros; i++) {
            int[] posicao = sortearPosicaoLivre(missao, random, minX, maxX, minY, maxY);
            missao.adicionarPassageiro(catalogo.get(i).apply(posicao[0], posicao[1]));
        }
        for (int i = 0; i < asteroides; i++) {
            int[] posicao = sortearPosicaoLivre(missao, random, minX, maxX, minY, maxY);
            missao.adicionarAsteroide(new Asteroide(posicao[0], posicao[1]));
        }
        for (int i = 0; i < asteroides; i++) {
            int[] posicao = sortearPosicaoLivre(missao, random, minX, maxX, minY, maxY);
            missao.adicionarInimigo(new Inimigo(posicao[0], posicao[1]));
        }
        return missao;
    }

    private int[] sortearPosicaoLivre(Missao missao, Random random, int minX, int maxX, int minY, int maxY) {
        int largura = maxX - minX + 1;
        int altura = maxY - minY + 1;
        for (long tentativa = 0; tentativa < (long) largura * altura * 2; tentativa++) {
            int x = random.nextInt(largura) + minX;
            int y = random.nextInt(altura) + minY;
            if (!posicaoOcupada(missao, x, y)) return new int[] {x, y};
        }
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (!posicaoOcupada(missao, x, y)) return new int[] {x, y};
            }
        }
        throw new IllegalStateException("Mapa sem posições livres para criar a missão");
    }

    private boolean posicaoOcupada(Missao missao, int x, int y) {
        return mesmaPosicao(missao.getNave(), x, y)
                || missao.getPassageiros().stream().anyMatch(p -> mesmaPosicao(p, x, y))
                || missao.getAsteroides().stream().anyMatch(a -> mesmaPosicao(a, x, y))
                || missao.getInimigos().stream().anyMatch(i -> mesmaPosicao(i, x, y));
    }

    private boolean mesmaPosicao(Posicionavel entidade, int x, int y) {
        return entidade.getX() == x && entidade.getY() == y;
    }
}
