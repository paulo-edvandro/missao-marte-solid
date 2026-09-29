package solidexercicio10.presentation;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.repository.RankingEntry;

/** Apresenta menu, mensagens e estatísticas; não decide regras nem acessa arquivos. */
public class JogoConsole {
    public enum Encerramento {
        FIM_ENTRADA, ABANDONO, SEM_VIDAS, SEM_PONTOS, VITORIA
    }

    private final PrintStream saida;

    public JogoConsole(PrintStream saida) {
        this.saida = Objects.requireNonNull(saida);
    }

    public void boasVindas() {
        saida.println("================================================");
        saida.println("        MISSÃO MARTE UNIFOR — SOLID");
        saida.println("================================================");
    }

    public void menu() {
        saida.println("\n--- MENU PRINCIPAL ---");
        saida.println("1. Iniciar Nova Missão");
        saida.println("2. Visualizar Ranking Top 5");
        saida.println("3. Resetar Histórico de Ranking");
        saida.println("4. Sair do Jogo");
        saida.print("Escolha uma opção: ");
    }

    public void pedirNome() { saida.print("\nDigite o nome do piloto: "); }
    public void pedirDificuldade() { saida.print("Escolha a Dificuldade (facil/medio/dificil): "); }
    public void pedirTamanhoMapa() { saida.print("Tamanho do mapa (2 a 50; ex: 5 para -5 a +5): "); }
    public void tamanhoInvalido() { saida.println("Tamanho inválido. Usando o padrão (5)."); }
    public void despedida() { saida.println("Obrigado por jogar a Missão Marte Unifor!"); }
    public void opcaoInvalida() { saida.println("Opção inválida. Tente novamente."); }
    public void nenhumPassageiro() { saida.println("Nenhum passageiro nesta posição."); }
    public void naveCheia() { saida.println("Nave cheia! Não há espaço para mais passageiros."); }
    public void comandoInvalido() { saida.println("Comando inválido."); }
    public void retornoPouso() { saida.println("Todos resgatados! Retorne à plataforma L em (0,0)."); }
    public void novoRecorde() { saida.println("Novo recorde absoluto do sistema!"); }
    public void entradaTop5() { saida.println("Parabéns! Você entrou para o Top 5 de pilotos!"); }
    public void resetConcluido() { saida.println("Ranking resetado com sucesso!"); }
    public void resetCancelado() { saida.println("Operação cancelada."); }

    public void prepararDecolagem(String nome, Dificuldade dificuldade, int min, int max) {
        saida.printf("\nIniciando missão de %s na dificuldade %s; mapa de %d a %d.%n", nome, dificuldade, min, max);
        saida.println("Pressione Enter para decolar!");
    }

    public void estadoPartida(Missao missao, int pontos) {
        Nave nave = missao.getNave();
        saida.printf("Nave em (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes: %d%n",
                nave.getX(), nave.getY(), pontos, nave.getVidas(), nave.getPassageiros().size(),
                nave.getCapacidade(), missao.getPassageiros().size());
        saida.print("Comando (w/s/a/d/c/q): ");
    }

    public void embarque(String nome, int bonus) {
        saida.printf("Passageiro %s embarcado com sucesso! +%d pontos!%n", nome, bonus);
    }

    public void colisao(int vidas) {
        saida.printf("Alerta! Colisão detectada! Vidas restantes: %d%n", vidas);
    }

    public void encerramento(Encerramento motivo) {
        saida.println(switch (motivo) {
            case FIM_ENTRADA -> "Missão encerrada: fim da entrada.";
            case ABANDONO -> "Missão abortada pelo piloto.";
            case SEM_VIDAS -> "GAME OVER! A nave foi destruída.";
            case SEM_PONTOS -> "Combustível/Pontuação zerada! Missão perdida.";
            case VITORIA -> "Missão cumprida! Nave acoplada à plataforma em (0,0).";
        });
    }

    public void estatisticas(int pontos, int movimentos, long segundos, int passageiros) {
        saida.println("Estatísticas da Partida:");
        saida.printf(" - Pontuação Final: %d pontos%n", pontos);
        saida.printf(" - Movimentos Efetuados: %d%n", movimentos);
        saida.printf(" - Tempo de Jogo: %d segundos%n", segundos);
        saida.printf(" - Passageiros Resgatados: %d%n", passageiros);
    }

    public void recorde(RankingEntry entrada) {
        saida.printf(" - Recorde atual: %d pontos (Piloto: %s)%n", entrada.score(), entrada.name());
    }

    public void falhaRecorde() {
        saida.println("Aviso: Não foi possível verificar o recorde atual devido a uma falha de leitura.");
    }
    public void falhaSalvar(String motivo) {
        saida.println("Erro grave: Falha ao salvar a sua pontuação no arquivo de ranking (" + motivo + ").");
    }
    public void falhaLer(String motivo) { saida.println("Erro ao carregar o ranking (" + motivo + ")."); }
    public void falhaLimpar(String motivo) { saida.println("Erro Crítico: Não foi possível limpar o ranking (" + motivo + ")."); }

    public void ranking(List<RankingEntry> entradas) {
        saida.println("\n====== RANKING TOP 5 PILOTOS ======");
        if (entradas.isEmpty()) {
            saida.println("Nenhum registro encontrado. Seja o primeiro a jogar!");
        } else {
            int posicao = 1;
            for (RankingEntry entrada : entradas) {
                saida.printf("%d. %s - %d pts | Dificuldade: %s | Coletados: %d | Tempo: %ds | %s%n",
                        posicao++, entrada.name(), entrada.score(), entrada.dificuldade(),
                        entrada.passageirosColetados(), entrada.tempoJogo(), entrada.dataHora());
            }
        }
        saida.println("===================================");
    }

    public void confirmarReset() {
        saida.print("Você realmente deseja limpar o histórico de ranking? (s/n): ");
    }
}
