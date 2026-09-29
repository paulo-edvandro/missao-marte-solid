import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.presentation.JogoConsole;
import solidexercicio10.service.FabricaMissao;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingRepository;
import solidexercicio10.service.JogoService;

public class JogoServiceTest {
    private static final class MemoryRanking implements RankingRepository {
        final List<RankingEntry> entries = new ArrayList<>();
        boolean failRead;
        boolean failWrite;
        boolean failClear;
        int saves;
        public void salvar(RankingEntry e) throws IOException {
            if (failWrite) throw new IOException("Falha de gravação simulada");
            entries.add(e);
            entries.sort(java.util.Comparator.comparingInt(RankingEntry::score).reversed());
            if (entries.size() > 5) entries.remove(5);
            saves++;
        }
        public List<RankingEntry> listar() throws IOException {
            if (failRead) throw new IOException("Falha de leitura simulada");
            return List.copyOf(entries);
        }
        public void limpar() throws IOException {
            if (failClear) throw new IOException("Falha de limpeza simulada");
            entries.clear();
        }
    }

    private static final class FixedRandom extends Random {
        // Cinco passageiros, dois asteroides, dois inimigos no mapa -2..2.
        final int[] positions = {3,2, 1,2, 2,3, 2,1, 3,3, 4,4, 0,0, 4,0, 0,4};
        int index;
        @Override public int nextInt(int bound) {
            if (index < positions.length) return positions[index++];
            return 0;
        }
    }

    private static String run(String input, MemoryRanking ranking) {
        var stdout = System.out;
        var output = new ByteArrayOutputStream();
        try (var ps = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            new JogoService(ranking, new MapaRenderer(), new FixedRandom(),
                    new JogoConsole(ps), FabricaMissao.padrao())
                    .executarLoop(new Scanner(input));
        } finally {
            System.setOut(stdout);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        var ranking = new MemoryRanking();
        String route = String.join("\n", "d", "c", "s", "c", "a", "c", "a", "w", "c", "d", "w", "c", "s") + "\n";
        String win = run("1\nPaulo\nmedio\n2\n\n" + route + "4\n", ranking);
        check(win.contains("Missão cumprida!"), "deve vencer só na plataforma");
        check(ranking.entries.size() == 1, "deve salvar uma pontuação");
        check(ranking.entries.get(0).passageirosColetados() == 5, "deve salvar cinco passageiros");
        check(ranking.entries.get(0).score() == 82, "pontuação incorreta");
        var semPouso = new MemoryRanking();
        String retorno = String.join("\n", "d", "c", "s", "c", "a", "c", "a", "w", "c", "d", "w", "c", "q") + "\n";
        String semVitoria = run("1\nPaulo\nmedio\n2\n\n" + retorno + "4\n", semPouso);
        check(semVitoria.contains("Retorne à plataforma L"), "deve exigir pouso");
        check(semPouso.entries.isEmpty(), "sem pouso não salva ranking");
        var empate = new MemoryRanking();
        for (int i = 0; i < 5; i++) empate.entries.add(ranking.entries.get(0));
        run("1\nPaulo\nmedio\n2\n\n" + route + "4\n", empate);
        check(empate.entries.size() == 5 && empate.saves == 0, "empate com quinto lugar não deve entrar");
        empate.entries.set(4, new RankingEntry("Quinto", 81, ranking.entries.get(0).dificuldade(), 5, "2026-09-26", 0));
        run("1\nPaulo\nmedio\n2\n\n" + route + "4\n", empate);
        check(empate.saves == 1, "pontuação maior que a quinta deve ser enviada ao repositório");
        check(run("2\n3\nn\n4\n", ranking).contains("Operação cancelada."), "cancelar reset");
        check(ranking.entries.size() == 1, "reset cancelado não pode apagar");
        run("3\ns\n4\n", ranking);
        check(ranking.entries.isEmpty(), "reset confirmado deve limpar");
        String abort = run("1\n\nfacil\n1\n\nq\n4\n", ranking);
        check(abort.contains("Tamanho inválido. Usando o padrão (5)."), "tamanho mínimo");
        check(ranking.entries.isEmpty(), "abortada não pontua");
        check(abort.contains("Estatísticas da Partida:"), "aborto precisa mostrar estatísticas");
        check(abort.contains("A bordo: 0/5"), "capacidade original deve continuar sendo cinco");
        ranking.entries.add(new RankingEntry("Recordista", 55, solidexercicio10.model.Dificuldade.FACIL, 4, "2026-09-26", 4));
        String abortComRecorde = run("1\nPaulo\nfacil\n2\n\nq\n4\n", ranking);
        check(abortComRecorde.contains("Recorde atual: 55 pontos (Piloto: Recordista)"),
                "estatísticas de aborto devem mostrar o recorde anterior");
        ranking.entries.clear();
        String perdaVidas = run("1\nPaulo\nmedio\n2\n\nd\nd\ns\ns\nc\nc\n4\n", ranking);
        check(perdaVidas.contains("GAME OVER!"), "três colisões devem encerrar a missão");
        check(perdaVidas.contains("Estatísticas da Partida:"), "derrota precisa mostrar estatísticas");
        String perdaPontos = run("1\nPaulo\nmedio\n2\n\n" + "w\ns\n".repeat(10) + "4\n", ranking);
        check(perdaPontos.contains("Pontuação zerada!"), "pontuação zero deve encerrar a missão");
        check(ranking.entries.isEmpty(), "derrotas não podem registrar pontuação");
        for (String entrada : List.of("", "1\n", "1\nPaulo\n", "1\nPaulo\nmedio\n", "1\nPaulo\nmedio\n2\n", "1\nPaulo\nmedio\n2\n\n")) {
            run(entrada, ranking);
        }
        var falha = new MemoryRanking();
        falha.failRead = true;
        check(run("2\n4\n", falha).contains("Erro ao carregar o ranking"), "erro de leitura deve ser informado");
        falha.failRead = false;
        falha.failWrite = true;
        falha.entries.add(new RankingEntry("Recordista", 1, solidexercicio10.model.Dificuldade.MEDIO, 5, "2026-09-29", 0));
        String escrita = run("1\nPaulo\nmedio\n2\n\n" + route + "4\n", falha);
        check(escrita.contains("Falha ao salvar a sua pontuação") && !escrita.contains("Parabéns!") && !escrita.contains("Novo recorde absoluto"), "gravação malsucedida não pode informar sucesso");
        falha.failClear = true;
        String limpeza = run("3\ns\n4\n", falha);
        check(limpeza.contains("Não foi possível limpar o ranking") && !limpeza.contains("Ranking resetado com sucesso!"), "limpeza malsucedida não pode informar sucesso");
        System.out.println("OK: vitória, pouso, pontuação, Top 5, ranking, reset, dimensões, aborto, derrotas, fim da entrada e erros de I/O");
    }
}
