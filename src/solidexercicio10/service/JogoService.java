package solidexercicio10.service;

import java.util.Objects;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingRepository;

/** Coordena o menu e as regras de cada partida. */
public class JogoService {
    private final RankingRepository rankingRepository;
    private final MapaRenderer mapaRenderer;
    private final Random random;

    public JogoService(RankingRepository rankingRepository, MapaRenderer mapaRenderer, Random random) {
        this.rankingRepository = Objects.requireNonNull(rankingRepository);
        this.mapaRenderer = Objects.requireNonNull(mapaRenderer);
        this.random = Objects.requireNonNull(random);
    }

    public void executarLoop(Scanner scanner) {
        Objects.requireNonNull(scanner);
        exibirBoasVindas();

        while (scanner.hasNextLine()) {
            exibirMenu();
            System.out.print("Escolha uma opção: ");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> iniciarPartida(scanner);
                case "2" -> exibirRanking();
                case "3" -> resetarRanking(scanner);
                case "4" -> {
                    System.out.println("Obrigado por jogar a Missão Marte Unifor!");
                    return;
                }
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private void exibirBoasVindas() {
        System.out.println("================================================");
        System.out.println("         MISSÃO MARTE UNIFOR — SOLID");
        System.out.println("================================================");
    }

    private void exibirMenu() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Iniciar Nova Missão");
        System.out.println("2. Visualizar Ranking Top 5");
        System.out.println("3. Resetar Histórico de Ranking");
        System.out.println("4. Sair do Jogo");
    }

    private void iniciarPartida(Scanner scanner) {
        // A criação da missão e o loop de comandos entram na próxima etapa.
        System.out.println("Partida em desenvolvimento.");
    }

    private void exibirRanking() {
        System.out.println("\n====== RANKING TOP 5 PILOTOS ======");
        List<RankingEntry> entradas = rankingRepository.listar();
        if (entradas.isEmpty()) {
            System.out.println("Nenhum registro encontrado. Seja o primeiro a jogar!");
        } else {
            int posicao = 1;
            for (RankingEntry entrada : entradas) {
                System.out.printf("%d. %s - %d pts | Dificuldade: %s | Coletados: %d | Tempo: %ds | %s%n",
                        posicao++, entrada.name(), entrada.score(), entrada.dificuldade(),
                        entrada.passageirosColetados(), entrada.tempoJogo(), entrada.dataHora());
            }
        }
        System.out.println("===================================");
    }

    private void resetarRanking(Scanner scanner) {
        System.out.print("Você realmente deseja limpar o histórico de ranking? (s/n): ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String resposta = scanner.nextLine().trim();
        if (resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("sim")) {
            rankingRepository.limpar();
            System.out.println("Ranking resetado com sucesso!");
        } else {
            System.out.println("Operação cancelada.");
        }
    }
}
