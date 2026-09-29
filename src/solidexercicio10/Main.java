package solidexercicio10;

import java.util.Random;
import java.util.Scanner;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.presentation.JogoConsole;
import solidexercicio10.repository.ArquivoRankingRepository;
import solidexercicio10.repository.RankingRepository;
import solidexercicio10.service.JogoService;
import solidexercicio10.service.FabricaMissao;

/** Ponto de entrada: cria as dependências e inicia o jogo. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        RankingRepository ranking = new ArquivoRankingRepository("ranking-solid-exercicio10.json");
        MapaRenderer mapa = new MapaRenderer();
        JogoConsole console = new JogoConsole(System.out);
        FabricaMissao fabrica = FabricaMissao.padrao();
        JogoService jogo = new JogoService(ranking, mapa, new Random(), console, fabrica);

        try (Scanner scanner = new Scanner(System.in)) {
            jogo.executarLoop(scanner);
        }
    }
}
