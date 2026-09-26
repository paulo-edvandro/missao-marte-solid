package solidexercicio10.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Astronauta;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Engenheiro;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Passageiro;
import solidexercicio10.model.Professor;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingRepository;

/** Coordena o menu e as regras de cada partida. */
public class JogoService {
    private static final DateTimeFormatter DATA_RANKING = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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

        while (true) {
            exibirMenu();
            System.out.print("Escolha uma opção: ");
            if (!scanner.hasNextLine()) {
                return;
            }
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
        System.out.print("\nDigite o nome do piloto: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String nome = scanner.nextLine().trim();
        if (nome.isEmpty()) {
            nome = "Piloto Anônimo";
        }

        System.out.print("Escolha a Dificuldade (facil/medio/dificil): ");
        if (!scanner.hasNextLine()) {
            return;
        }
        Dificuldade dificuldade = Dificuldade.deString(scanner.nextLine());

        System.out.print("Tamanho do mapa (ex: 5 para -5 a +5): ");
        if (!scanner.hasNextLine()) {
            return;
        }
        int tamanhoMapa = lerTamanhoMapa(scanner.nextLine());
        int minX = -tamanhoMapa;
        int maxX = tamanhoMapa;
        int minY = -tamanhoMapa;
        int maxY = tamanhoMapa;

        System.out.printf("\nIniciando missão de %s na dificuldade %s; mapa de %d a %d.%n",
                nome, dificuldade, minX, maxX);
        System.out.println("Pressione Enter para decolar!");
        if (!scanner.hasNextLine()) {
            return;
        }
        scanner.nextLine();

        Missao missao = criarNovaMissao(dificuldade, minX, maxX, minY, maxY);
        jogarPartida(scanner, nome, dificuldade, missao, minX, maxX, minY, maxY);
    }

    private int lerTamanhoMapa(String entrada) {
        try {
            int tamanho = Integer.parseInt(entrada.trim());
            // Em -1..1, as posições livres são insuficientes para os obstáculos do jogo.
            if (tamanho >= 2 && tamanho <= 50) {
                return tamanho;
            }
        } catch (NumberFormatException ignored) {
            // Entrada vazia ou não numérica: usar a dimensão padrão.
        }
        System.out.println("Tamanho inválido. Usando o padrão (5).");
        return 5;
    }

    //dif media? 
    //PAREI AQUI
    private Missao criarNovaMissao(Dificuldade dificuldade, int minX, int maxX, int minY, int maxY) {
        int passageiros = dificuldade == Dificuldade.FACIL ? 4 : 5;
        int asteroides = dificuldade == Dificuldade.FACIL ? 1 : dificuldade == Dificuldade.DIFICIL ? 3 : 2;
        int inimigos = asteroides;
        Missao missao = new Missao(new Nave("A-1", passageiros));

        for (int i = 0; i < passageiros; i++) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY);
            missao.adicionarPassageiro(criarPassageiro(i, posicao[0], posicao[1]));
        }
        for (int i = 0; i < asteroides; i++) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY);
            missao.adicionarAsteroide(new Asteroide(posicao[0], posicao[1]));
        }
        for (int i = 0; i < inimigos; i++) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY);
            missao.adicionarInimigo(new Inimigo(posicao[0], posicao[1]));
        }
        return missao;
    }

    private Passageiro criarPassageiro(int indice, int x, int y) {
        return switch (indice % 5) {
            case 0 -> new Professor("Dr. Silva", x, y);
            case 1 -> new Engenheiro("Eng. Rosa", x, y);
            case 2 -> new Professor("Dr. Lima", x, y);
            case 3 -> new Engenheiro("Eng. Carlos", x, y);
            default -> new Astronauta("Ast. Maria", x, y);
        };
    }

    private int[] sortearPosicaoLivre(Missao missao, int minX, int maxX, int minY, int maxY) {
        int largura = maxX - minX + 1;
        int altura = maxY - minY + 1;
        // Após tentativas aleatórias, procura uma vaga para garantir o término da geração.
        for (int tentativa = 0; tentativa < largura * altura * 2; tentativa++) {
            int x = random.nextInt(largura) + minX;
            int y = random.nextInt(altura) + minY;
            if (!posicaoOcupada(missao, x, y)) {
                return new int[] {x, y};
            }
        }
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (!posicaoOcupada(missao, x, y)) {
                    return new int[] {x, y};
                }
            }
        }
        throw new IllegalStateException("Mapa sem posições livres para criar a missão");
    }

    private boolean posicaoOcupada(Missao missao, int x, int y) {
        if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
            return true;
        }
        for (Passageiro passageiro : missao.getPassageiros()) {
            if (passageiro.getX() == x && passageiro.getY() == y) {
                return true;
            }
        }
        for (Asteroide asteroide : missao.getAsteroides()) {
            if (asteroide.getX() == x && asteroide.getY() == y) {
                return true;
            }
        }
        for (Inimigo inimigo : missao.getInimigos()) {
            if (inimigo.getX() == x && inimigo.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private int pontuacaoInicial(Dificuldade dificuldade) {
        return switch (dificuldade) {
            case FACIL -> 30;
            case MEDIO -> 20;
            case DIFICIL -> 15;
        };
    }

    private void jogarPartida(Scanner scanner, String nome, Dificuldade dificuldade,
                            Missao missao, int minX, int maxX, int minY, int maxY) {
        Nave nave = missao.getNave();
        int score = pontuacaoInicial(dificuldade);
        int movimentos = 0;
        long tempoInicio = System.currentTimeMillis();

        while (true) {
            mapaRenderer.desenhar(missao, minX, maxX, minY, maxY, score, nome);
            System.out.printf("Nave em (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes: %d%n",
                    nave.getX(), nave.getY(), score, nave.getVidas(),
                    nave.getPassageiros().size(), nave.getCapacidade(), missao.getPassageiros().size());
            System.out.print("Comando (w/s/a/d/c/q): ");
            if (!scanner.hasNextLine()) {
                return;
            }
            String entrada = scanner.nextLine().trim().toLowerCase(java.util.Locale.ROOT);
            if (entrada.isEmpty()) {
                continue;
            }
            char comando = entrada.charAt(0);
            if (comando == 'q') {
                System.out.println("Missão abortada pelo piloto.");
                return;
            }
            if (comando == 'c') {
                Passageiro passageiro = missao.passagemNaPosicao();
                if (passageiro == null) {
                    System.out.println("Nenhum passageiro nesta posição.");
                } else if (missao.embarcarPassageiroNaPosicao()) {
                    int bonus = passageiro.getPontuacao();
                    score += bonus;
                    System.out.printf("Passageiro %s embarcado com sucesso! +%d pontos!%n",
                            passageiro.getNome(), bonus);
                } else {
                    System.out.println("Nave cheia! Não há espaço para mais passageiros.");
                }
            } else if (comando == 'w' || comando == 's' || comando == 'a' || comando == 'd') {
                nave.moverComLimites(comando, minX, maxX, minY, maxY);
                score--;
                movimentos++;
            } else {
                System.out.println("Comando inválido.");
                continue;
            }

            missao.moverInimigos(random, minX, maxX, minY, maxY);
            if (missao.verificaColisao()) {
                nave.perderVida();
                if (nave.getVidas() == 0) {
                    System.out.println("GAME OVER! A nave foi destruída.");
                    return;
                }
                System.out.printf("Alerta! Colisão detectada! Vidas restantes: %d%n", nave.getVidas());
            }
            if (score <= 0) {
                System.out.println("Combustível/Pontuação zerada! Missão perdida.");
                return;
            }
            if (missao.todosEmbarcados()) {
                if (nave.getX() == 0 && nave.getY() == 0) {
                    long tempoSegundos = (System.currentTimeMillis() - tempoInicio) / 1000;
                    System.out.println("Missão cumprida! Nave acoplada à plataforma em (0,0).");
                    exibirEstatisticas(score, movimentos, tempoSegundos, nave.getPassageiros().size());
                    salvarSeEntrarNoRanking(nome, score, dificuldade,
                            nave.getPassageiros().size(), tempoSegundos);
                    return;
                }
                System.out.println("Todos resgatados! Retorne à plataforma L em (0,0).");
            }
        }
    }

    private void exibirEstatisticas(int score, int movimentos, long tempoSegundos, int passageiros) {
        System.out.println("Estatísticas da Partida:");
        System.out.printf(" - Pontuação Final: %d pontos%n", score);
        System.out.printf(" - Movimentos Efetuados: %d%n", movimentos);
        System.out.printf(" - Tempo de Jogo: %d segundos%n", tempoSegundos);
        System.out.printf(" - Passageiros Resgatados: %d%n", passageiros);
        List<RankingEntry> ranking = rankingRepository.listar();
        if (!ranking.isEmpty()) {
            RankingEntry primeiro = ranking.get(0);
            if (score > primeiro.score()) {
                System.out.println("Novo recorde absoluto do sistema!");
            } else {
                System.out.printf(" - Recorde atual: %d pontos (Piloto: %s)%n",
                        primeiro.score(), primeiro.name());
            }
        }
    }

    private void salvarSeEntrarNoRanking(String nome, int score, Dificuldade dificuldade,
                                         int passageiros, long tempoSegundos) {
        List<RankingEntry> ranking = rankingRepository.listar();
        if (score <= 0 || ranking.size() >= 5 && ranking.stream()
                .mapToInt(RankingEntry::score).min().orElse(0) >= score) {
            return;
        }
        RankingEntry entrada = new RankingEntry(nome, score, dificuldade, passageiros,
                LocalDateTime.now().format(DATA_RANKING), tempoSegundos);
        rankingRepository.salvar(entrada);
        System.out.println("Parabéns! Você entrou para o Top 5 de pilotos!");
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
