import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.presentation.JogoConsole;
import solidexercicio10.service.FabricaMissao;
import solidexercicio10.repository.ArquivoRankingRepository;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.service.JogoService;

/** Testa arquivo real e integração; usa apenas um diretório temporário. */
public class ArquivoRankingRepositoryTest {
    private interface Operacao { void executar() throws IOException; }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    private static void esperarIOException(Operacao operacao) throws IOException {
        try {
            operacao.executar();
            throw new AssertionError("Era esperado IOException");
        } catch (IOException esperado) {
            verificar(!esperado.getMessage().isEmpty(), "erro precisa ter mensagem");
        }
    }

    private static RankingEntry entrada(String nome, int pontos) {
        return new RankingEntry(nome, pontos, Dificuldade.MEDIO, 5, "2026-09-29 17:00:00", 8);
    }

    private static final class PosicoesFixas extends Random {
        private final int[] posicoes = {3,2, 1,2, 2,3, 2,1, 3,3, 4,4, 0,0, 4,0, 0,4};
        private int indice;
        @Override public int nextInt(int limite) {
            return indice < posicoes.length ? posicoes[indice++] : 0;
        }
    }

    private static String executar(String comandos, Path arquivo) {
        PrintStream anterior = System.out;
        var bytes = new ByteArrayOutputStream();
        try (var saida = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(saida);
            new JogoService(new ArquivoRankingRepository(arquivo.toString()),
                    new MapaRenderer(), new PosicoesFixas(),
                    new JogoConsole(saida), FabricaMissao.padrao()).executarLoop(new Scanner(comandos));
        } finally {
            System.setOut(anterior);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        Path diretorio = Files.createTempDirectory("missao-ranking-test-");
        Path arquivo = diretorio.resolve("ranking.json");
        var repo = new ArquivoRankingRepository(arquivo.toString());
        try {
            verificar(repo.listar().isEmpty(), "arquivo ausente deve retornar lista vazia");
            repo.limpar();
            for (String nome : List.of("Ana \"Nasa\"", "Paulo\\", "Ana}Silva",
                    "A{B}, C: D", "C:\\novo\\teste", "Linha\nTab\tFim\r\b\f" + (char) 1,
                    "João 🚀", "score: \"name\"")) {
                repo.limpar();
                RankingEntry esperado = entrada(nome, 82);
                repo.salvar(esperado);
                verificar(repo.listar().equals(List.of(esperado)), "nome não preservado: " + nome);
            }
            repo.limpar();
            for (int pontos : new int[] {10, 50, 20, 80, 40, 30, 60}) repo.salvar(entrada("P" + pontos, pontos));
            verificar(repo.listar().stream().map(RankingEntry::score).toList().equals(List.of(80,60,50,40,30)),
                    "arquivo deve manter Top 5 ordenado");
            var antesDoEmpate = repo.listar();
            repo.salvar(entrada("Empatado", 30));
            verificar(repo.listar().equals(antesDoEmpate), "empate deve preservar os anteriores");
            Files.writeString(arquivo, "[{\"score\":10,\"name\":\"B\"},{\"name\":\"A\",\"score\":90}]", StandardCharsets.UTF_8);
            verificar(repo.listar().get(0).score() == 90, "leitura deve ordenar arquivo existente");
            verificar(repo.listar().get(0).passageirosColetados() == 0, "campos opcionais antigos devem ter padrão");
            Files.writeString(arquivo, "[{\"name\":\"Jo\\u00e3o\",\"score\":1}]", StandardCharsets.UTF_8);
            verificar(repo.listar().get(0).name().equals("João"), "escape Unicode precisa ser lido");
            for (String invalido : List.of("", "texto", "[", "[{}]", "[{\"name\":\"A\",\"score\":\"abc\"}]",
                    "[{\"name\":\"A\",\"score\":999999999999999999999}]", "[] lixo",
                    "[{\"name\":\"A\\q\",\"score\":1}]", "[{\"name\":\"A\",\"score\":01}]",
                    "[{\"name\":\"A\",\"score\":1,}]")) {
                Files.writeString(arquivo, invalido, StandardCharsets.UTF_8);
                esperarIOException(() -> repo.listar());
                esperarIOException(() -> repo.salvar(entrada("Novo", 99)));
                verificar(Files.readString(arquivo).equals(invalido), "salvar não pode apagar ranking corrompido");
            }
            verificar(executar("2\n4\n", arquivo).contains("Erro ao carregar o ranking"),
                    "arquivo inválido deve avisar e retornar ao menu");
            repo.limpar();
            String rota = String.join("\n", "d","c","s","c","a","c","a","w","c","d","w","c","s") + "\n";
            String vitoria = executar("1\nPaulo\nmedio\n2\n\n" + rota + "4\n", arquivo);
            verificar(vitoria.contains("Missão cumprida!"), "missão integrada deve vencer com pouso");
            verificar(repo.listar().get(0).score() == 82 && repo.listar().get(0).passageirosColetados() == 5,
                    "partida deve salvar pontos e passageiros reais");
            verificar(executar("2\n4\n", arquivo).contains("Paulo - 82 pts"), "nova execução precisa ler o ranking");
            verificar(executar("3\ns\n4\n", arquivo).contains("Ranking resetado com sucesso!"), "reset integrado");
            verificar(!Files.exists(arquivo), "reset deve apagar arquivo real");
            Files.createDirectory(arquivo);
            Files.writeString(arquivo.resolve("filho"), "bloqueia remoção");
            esperarIOException(() -> repo.listar());
            esperarIOException(() -> repo.salvar(entrada("Novo", 99)));
            esperarIOException(() -> repo.limpar());
            verificar(!executar("3\ns\n4\n", arquivo).contains("Ranking resetado com sucesso!"),
                    "falha real de remoção não pode anunciar sucesso");
            var semPasta = new ArquivoRankingRepository(diretorio.resolve("ausente/ranking.json").toString());
            esperarIOException(() -> semPasta.salvar(entrada("Novo", 99)));
            System.out.println("OK: JSON, caracteres especiais, Top 5, compatibilidade, corrupção, falhas reais de I/O e integração");
        } finally {
            if (Files.isDirectory(arquivo)) Files.deleteIfExists(arquivo.resolve("filho"));
            Files.deleteIfExists(arquivo);
            Files.deleteIfExists(diretorio);
        }
    }
}
