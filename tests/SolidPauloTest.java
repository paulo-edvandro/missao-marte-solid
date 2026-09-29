import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.BiFunction;
import solidexercicio10.model.*;
import solidexercicio10.presentation.JogoConsole;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingRepository;
import solidexercicio10.service.FabricaMissao;
import solidexercicio10.service.JogoService;

/** Testa extensão de passageiros e preservação das configurações e regras. */
public class SolidPauloTest {
    private static final class Cientista extends Passageiro {
        Cientista(int x, int y) { super("Dra. Nova", "Cientista", x, y); }
        @Override public int getPontuacao() { return 25; }
    }

    private static final class Memoria implements RankingRepository {
        private final List<RankingEntry> entradas = new ArrayList<>();
        public void salvar(RankingEntry entrada) { entradas.add(entrada); }
        public List<RankingEntry> listar() { return List.copyOf(entradas); }
        public void limpar() { entradas.clear(); }
    }

    private static final class PosicoesFixas extends Random {
        private final int[] posicoes = {3,2, 1,2, 2,3, 2,1, 3,3, 4,4, 0,0, 4,0, 0,4};
        private int indice;
        @Override public int nextInt(int limite) {
            return indice < posicoes.length ? posicoes[indice++] : 0;
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    public static void main(String[] args) {
        for (Dificuldade dificuldade : Dificuldade.values()) {
            Missao missao = FabricaMissao.padrao().criar(dificuldade, new Random(42), -2, 2, -2, 2);
            int obstaculos = dificuldade == Dificuldade.FACIL ? 1 : dificuldade == Dificuldade.MEDIO ? 2 : 3;
            verificar(missao.getPassageiros().size() == (dificuldade == Dificuldade.FACIL ? 4 : 5), "quantidade original");
            verificar(missao.getAsteroides().size() == obstaculos && missao.getInimigos().size() == obstaculos,
                    "dificuldade precisa preservar obstáculos");
            verificar(missao.getNave().getCapacidade() == 5, "capacidade original");
            var ocupadas = new HashSet<String>();
            ocupadas.add("0,0");
            var entidades = new ArrayList<Posicionavel>();
            entidades.addAll(missao.getPassageiros());
            entidades.addAll(missao.getAsteroides());
            entidades.addAll(missao.getInimigos());
            for (Posicionavel entidade : entidades) {
                verificar(ocupadas.add(entidade.getX() + "," + entidade.getY()), "geração sem sobreposição");
            }
        }
        // Random constante obriga a busca alternativa; criação ainda deve terminar.
        FabricaMissao.padrao().criar(Dificuldade.DIFICIL, new Random() {
            @Override public int nextInt(int limite) { return 0; }
        }, -2, 2, -2, 2);

        List<BiFunction<Integer, Integer, Passageiro>> catalogo = new ArrayList<>(List.of(
                Cientista::new,
                (x,y) -> new Engenheiro("Eng. Rosa", x,y),
                (x,y) -> new Professor("Dr. Lima", x,y),
                (x,y) -> new Engenheiro("Eng. Carlos", x,y),
                (x,y) -> new Astronauta("Ast. Maria", x,y)));
        var fabrica = new FabricaMissao(catalogo);
        var ranking = new Memoria();
        var bytes = new ByteArrayOutputStream();
        try (var saida = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            // O mapa é omitido aqui para verificar a saída do console injetado isoladamente.
            var mapa = new MapaRenderer() {
                @Override public void desenhar(Missao missao, int minX, int maxX, int minY, int maxY,
                                               int pontos, String nome) { }
            };
            var jogo = new JogoService(ranking, mapa, new PosicoesFixas(), new JogoConsole(saida), fabrica);
            String rota = String.join("\n", "d","c","s","c","a","c","a","w","c","d","w","c","s") + "\n";
            jogo.executarLoop(new Scanner("1\nPaulo\nmedio\n2\n\n" + rota + "4\n"));
        }
        String texto = bytes.toString(StandardCharsets.UTF_8);
        verificar(texto.contains("Dra. Nova embarcado com sucesso! +25 pontos!"), "novo tipo deve pontuar pelo contrato");
        verificar(texto.contains("MENU PRINCIPAL") && texto.contains("Estatísticas da Partida:"), "apresentação injetada");
        verificar(ranking.entradas.size() == 1 && ranking.entradas.get(0).score() == 97, "vitória com novo tipo e regras preservadas");
        catalogo.add(Cientista::new);
        Missao ampliada = new FabricaMissao(catalogo).criar(Dificuldade.MEDIO, new Random(42), -2,2,-2,2);
        verificar(ampliada.getPassageiros().size() == 6 && ampliada.getNave().getCapacidade() == 6,
                "catálogo ampliado deve permitir resgate de todos");
        try {
            FabricaMissao.padrao().criar(Dificuldade.DIFICIL, new Random(), 0,0,0,0);
            throw new AssertionError("mapa insuficiente precisa ser rejeitado antes de sortear");
        } catch (IllegalArgumentException esperado) { }
        System.out.println("OK: SRP, novo passageiro via catálogo, pontuação polimórfica, dificuldades e geração sem sobreposição");
    }
}
