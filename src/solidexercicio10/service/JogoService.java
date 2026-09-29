package solidexercicio10.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Passageiro;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.presentation.JogoConsole;
import solidexercicio10.presentation.JogoConsole.Encerramento;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingRepository;

/** Coordena o menu e as regras de cada partida. */
public class JogoService {
    private static final DateTimeFormatter DATA_RANKING = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RankingRepository rankingRepository;
    private final MapaRenderer mapaRenderer;
    private final Random random;
    private final JogoConsole console;
    private final FabricaMissao fabricaMissao;

    public JogoService(RankingRepository rankingRepository, MapaRenderer mapaRenderer, Random random,
                       JogoConsole console, FabricaMissao fabricaMissao) {
        this.console = Objects.requireNonNull(console);
        this.fabricaMissao = Objects.requireNonNull(fabricaMissao);
        this.rankingRepository = Objects.requireNonNull(rankingRepository);
        this.mapaRenderer = Objects.requireNonNull(mapaRenderer);
        this.random = Objects.requireNonNull(random);
    }

    public void executarLoop(Scanner scanner) {
        Objects.requireNonNull(scanner);
        console.boasVindas();

        while (true) {
            console.menu();
            if (!scanner.hasNextLine()) {
                return;
            }
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> iniciarPartida(scanner);
                case "2" -> exibirRanking();
                case "3" -> resetarRanking(scanner);
                case "4" -> {
                    console.despedida();
                    return;
                }
                default -> console.opcaoInvalida();
            }
        }
    }

    private void iniciarPartida(Scanner scanner) {
        console.pedirNome();
        if (!scanner.hasNextLine()) {
            return;
        }
        String nome = scanner.nextLine().trim();
        if (nome.isEmpty()) {
            nome = "Piloto Anônimo";
        }

        console.pedirDificuldade();
        if (!scanner.hasNextLine()) {
            return;
        }
        Dificuldade dificuldade = Dificuldade.deString(scanner.nextLine());

        console.pedirTamanhoMapa();
        if (!scanner.hasNextLine()) {
            return;
        }
        int tamanhoMapa = lerTamanhoMapa(scanner.nextLine());
        int minX = -tamanhoMapa;
        int maxX = tamanhoMapa;
        int minY = -tamanhoMapa;
        int maxY = tamanhoMapa;

        console.prepararDecolagem(nome, dificuldade, minX, maxX);
        if (!scanner.hasNextLine()) {
            return;
        }
        scanner.nextLine();

        Missao missao = fabricaMissao.criar(dificuldade, random, minX, maxX, minY, maxY);
        jogarPartida(scanner, nome, dificuldade, missao, minX, maxX, minY, maxY);
    }

    private int lerTamanhoMapa(String entrada) {
        try {
            int tamanho = Integer.parseInt(entrada.trim());
            // O mínimo garante espaço para todas as dificuldades; o máximo limita a saída no console.
            if (tamanho >= 2 && tamanho <= 50) {
                return tamanho;
            }
        } catch (NumberFormatException ignored) {
        }
        console.tamanhoInvalido();
        return 5;
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
            console.estadoPartida(missao, score);
            if (!scanner.hasNextLine()) {
                finalizarPartida(Encerramento.FIM_ENTRADA, nave, score, movimentos, tempoInicio);
                return;
            }
            String entrada = scanner.nextLine().trim().toLowerCase(java.util.Locale.ROOT);
            if (entrada.isEmpty()) {
                continue;
            }
            char comando = entrada.charAt(0);
            if (comando == 'q') {
                finalizarPartida(Encerramento.ABANDONO, nave, score, movimentos, tempoInicio);
                return;
            }
            if (comando == 'c') {
                Passageiro passageiro = missao.passagemNaPosicao();
                if (passageiro == null) {
                    console.nenhumPassageiro();
                } else if (missao.embarcarPassageiroNaPosicao()) {
                    int bonus = passageiro.getPontuacao();
                    score += bonus;
                    console.embarque(passageiro.getNome(), bonus);
                } else {
                    console.naveCheia();
                }
            } else if (comando == 'w' || comando == 's' || comando == 'a' || comando == 'd') {
                nave.moverComLimites(comando, minX, maxX, minY, maxY);
                score--;
                movimentos++;
            } else {
                console.comandoInvalido();
                continue;
            }

            missao.moverInimigos(random, minX, maxX, minY, maxY);
            if (missao.verificaColisao()) {
                nave.perderVida();
                if (nave.getVidas() == 0) {
                    finalizarPartida(Encerramento.SEM_VIDAS, nave, score, movimentos, tempoInicio);
                    return;
                }
                console.colisao(nave.getVidas());
            }
            if (score <= 0) {
                finalizarPartida(Encerramento.SEM_PONTOS,
                        nave, score, movimentos, tempoInicio);
                return;
            }
            if (missao.todosEmbarcados()) {
                if (nave.getX() == 0 && nave.getY() == 0) {
                    long tempoSegundos = finalizarPartida(Encerramento.VITORIA,
                            nave, score, movimentos, tempoInicio);
                    salvarSeEntrarNoRanking(nome, score, dificuldade,
                            nave.getPassageiros().size(), tempoSegundos);
                    return;
                }
                console.retornoPouso();
            }
        }
    }

    private long finalizarPartida(Encerramento motivo, Nave nave, int score, int movimentos, long tempoInicio) {
        long tempoSegundos = Math.max(0, (System.currentTimeMillis() - tempoInicio) / 1000);
        console.encerramento(motivo);
        exibirEstatisticas(score, movimentos, tempoSegundos, nave.getPassageiros().size());
        return tempoSegundos;
    }

    private void exibirEstatisticas(int score, int movimentos, long tempoSegundos, int passageiros) {
        console.estatisticas(score, movimentos, tempoSegundos, passageiros);

        try {
            List<RankingEntry> ranking = rankingRepository.listar();
            if (!ranking.isEmpty()) {
                console.recorde(ranking.get(0));
            }
        } catch (IOException e) {
            console.falhaRecorde();
        }
    }

    private void salvarSeEntrarNoRanking(String nome, int score, Dificuldade dificuldade,
                                         int passageiros, long tempoSegundos) {
        try {
            List<RankingEntry> ranking = rankingRepository.listar();
            
            boolean novoRecorde = !ranking.isEmpty() && score > ranking.get(0).score();
            
            if (score <= 0 || ranking.size() >= 5 && ranking.stream()
                    .mapToInt(RankingEntry::score).min().orElse(0) >= score) {
                return;
            }
            RankingEntry entrada = new RankingEntry(nome, score, dificuldade, passageiros,
                    LocalDateTime.now().format(DATA_RANKING), tempoSegundos);
            rankingRepository.salvar(entrada);
            if (novoRecorde) {
                console.novoRecorde();
            }
            console.entradaTop5();
        } catch (IOException e) {
            console.falhaSalvar(e.getMessage());
        }
    }

    private void exibirRanking() {
        try {
            console.ranking(rankingRepository.listar());
        } catch (IOException e) {
            console.falhaLer(e.getMessage());
        }
    }

    private void resetarRanking(Scanner scanner) {
        console.confirmarReset();
        if (!scanner.hasNextLine()) {
            return;
        }
        String resposta = scanner.nextLine().trim();
        if (resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("sim")) {
            try {
                rankingRepository.limpar();
                console.resetConcluido();
            } catch (IOException e) {
                console.falhaLimpar(e.getMessage());
            }
        } else {
            console.resetCancelado();
        }
    }
}
