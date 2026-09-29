# Contratos de integração — Missão Marte SOLID

Este documento fixa as **assinaturas públicas mínimas** para o trabalho em paralelo. O código original em `src/exercicio10/` permanece sem alterações. As implementações ficam em `src/solidexercicio10/`. Pode haver métodos adicionais; qualquer mudança nas assinaturas abaixo deve ser avisada aos outros antes de subir a branch.

## Organização e autoria

| Pessoa | Arquivos sob sua responsabilidade | Branch sugerida |
| --- | --- | --- |
| Paulo | `Main.java`, `service/JogoService.java`, `service/FabricaMissao.java`, `presentation/JogoConsole.java`, integração e README final | `refactor/fluxo` |
| Lucas | `model/*.java`, diagrama de classes | `refactor/modelo` |
| Emerson | `repository/*.java`, `presentation/MapaRenderer.java`, diagrama de pacotes | `refactor/ranking-apresentacao` |

Commits em `main` são a base comum. Cada pessoa cria a própria branch **a partir da `main` publicada**, sobe os commits da sua parte e abre um PR para `main`. Mudanças de integração podem complementar arquivos de outra parte quando combinadas; devem preservar seus contratos e registrar o motivo no commit. `docs/uml/diagrama-classes-model.*` pertence ao Lucas; `docs/uml/diagrama-pacotes.*` pertence ao Emerson.

## Modelo — Lucas implementa

Pacote `solidexercicio10.model`. Para a integração com o serviço e o renderizador, manter:

```java
public enum Dificuldade { FACIL, MEDIO, DIFICIL;
    public static Dificuldade deString(String valor);
}

public interface Posicionavel {
    int getX(); int getY();
}
public interface Movel { void mover(int dx, int dy); }

public abstract class EntidadeMapa implements Posicionavel {
    public int getX(); public int getY(); public abstract String getSimbolo();
}
public abstract class Passageiro extends EntidadeMapa {
    public String getNome(); public String getTipo(); public abstract int getPontuacao();
}
public final class Professor extends Passageiro {
    public Professor(String nome, int x, int y);
}
public final class Engenheiro extends Passageiro {
    public Engenheiro(String nome, int x, int y);
}
public final class Astronauta extends Passageiro {
    public Astronauta(String nome, int x, int y);
}
public class Asteroide extends EntidadeMapa {
    public Asteroide(int x, int y);
}
public class Inimigo extends EntidadeMapa implements Movel {
    public Inimigo(int x, int y);
    public void mover(Random random, int minX, int maxX, int minY, int maxY);
}
public class Nave extends EntidadeMapa implements Movel {
    public Nave(String id, int capacidade); // posição inicial (0,0), 3 vidas
    public int getCapacidade(); public int getVidas();
    public List<Passageiro> getPassageiros();
    public boolean embarcar(Passageiro passageiro);
    public void perderVida();
    public void moverComLimites(char comando, int minX, int maxX, int minY, int maxY);
}
public class Missao {
    public Missao(Nave nave);
    public Nave getNave();
    public List<Passageiro> getPassageiros();
    public List<Asteroide> getAsteroides();
    public List<Inimigo> getInimigos();
    public void adicionarPassageiro(Passageiro passageiro);
    public void adicionarAsteroide(Asteroide asteroide);
    public void adicionarInimigo(Inimigo inimigo);
    public Passageiro passagemNaPosicao();
    public boolean embarcarPassageiroNaPosicao();
    public void moverInimigos(Random random, int minX, int maxX, int minY, int maxY);
    public boolean verificaColisao();
    public boolean todosEmbarcados();
}
```

Essas são **assinaturas ilustrativas por classe**, não um único arquivo Java compilável. `Random` e `List` vêm de `java.util`.

Semântica: Professor +10, Engenheiro +15, Astronauta +20; `Nave.embarcar` retorna `false` quando cheia; `Missao.embarcarPassageiroNaPosicao` só remove passageiro do mapa após embarque bem-sucedido. `w` diminui `y`, `s` aumenta `y`, `a` diminui `x`, `d` aumenta `x`, conforme original. Inimigos dão um passo em uma das quatro direções por turno e ficam dentro dos limites. `Missao` não imprime nem salva arquivos. O `MapaRenderer` pode usar `getSimbolo()`; combinar os símbolos com Emerson.

## Ranking e apresentação — Emerson implementa

Pacotes `solidexercicio10.repository` e `solidexercicio10.presentation`. Assinaturas usadas por Paulo:

```java
public record RankingEntry(
    String name, int score, Dificuldade dificuldade,
    int passageirosColetados, String dataHora, long tempoJogo
) {}

public interface RankingRepository {
    void salvar(RankingEntry entrada) throws IOException;
    List<RankingEntry> listar() throws IOException;
    void limpar() throws IOException;
}

public class ArquivoRankingRepository implements RankingRepository {
    public ArquivoRankingRepository(String nomeArquivo);
}

public class MapaRenderer {
    public void desenhar(Missao missao, int minX, int maxX, int minY, int maxY,
                        int score, String pilotoNome);
}
```

Imports necessários: `solidexercicio10.model.Dificuldade`, `solidexercicio10.model.Missao`, `java.util.List`, `java.io.IOException`. Use **Java 17 ou superior** (o `record` exige Java 16+). O ranking em arquivo deve ser JSON válido, ordenado por pontuação decrescente e limitado aos 5 melhores, conforme original. Em caso de pontuação empatada com o quinto quando já há cinco, mantenha os cinco anteriores (a seleção é responsabilidade de Paulo; o repositório garante ordenação e limite como defesa). `listar()` retorna lista vazia se não houver arquivo; falhas reais de I/O e formato JSON inválido são propagados como `IOException` e tratados pelo serviço. `limpar()` só informa sucesso quando a limpeza realmente ocorrer. O desenho do mapa não modifica a missão.

## Fluxo — Paulo implementa

```java
public class JogoService {
    public JogoService(RankingRepository rankingRepository, MapaRenderer mapaRenderer,
                       Random random, JogoConsole console, FabricaMissao fabricaMissao);
    public void executarLoop(Scanner scanner);
}
```

`Main` cria `new ArquivoRankingRepository("ranking-solid-exercicio10.json")`, `new MapaRenderer()`, `new Random()`, `new JogoConsole(System.out)` e `FabricaMissao.padrao()`, passa essas dependências ao `JogoService` e inicia o loop com `Scanner`. O construtor de três argumentos foi substituído pelo de cinco; os testes e o ponto de entrada foram atualizados juntos. O serviço consulta e salva usando o contrato do repositório. Em vitória, só salva se a pontuação for positiva e entrar no Top 5; o menu pede confirmação antes de limpar o ranking. A vitória exige todos os passageiros embarcados **e** a nave em `(0,0)`.

## Refatoração da parte de Paulo — 29/09/2026

- `JogoConsole` recebe `PrintStream` e só apresenta dados e mensagens. Não lê
  arquivos nem decide regras. O serviço lê os comandos e decide qual saída solicitar.
- `FabricaMissao` recebe `List<BiFunction<Integer, Integer, Passageiro>>` pelo
  construtor, ou usa a configuração original via `padrao()`. `criar` recebe
  dificuldade, `Random` e limites. Não há seleção por tipo concreto no serviço.
- Modelo e diagramas de classes do Lucas permanecem sem alterações.
- Emerson deve complementar o diagrama de pacotes com a dependência
  `presentation --> repository` (`RankingEntry`) e explicitar o contrato do ranking.
- `REVISAO-SOLID.md` será elaborado após concluir a implementação integrada.

## Verificações antes de abrir PR

- Todos os `.java` novos começam com `package solidexercicio10...` correto.
- O original continua íntegro: `git diff main -- src/exercicio10` sem diferenças.
- `javac -encoding UTF-8 -d out src/exercicio10/*.java` compila o original.
- Após integração: compilar todos os `.java` novos e comparar menu, missão, ranking e reset com o original.
- Diagramas representam a implementação real; enviar fonte (`.puml` ou `.mmd`) **e** imagem (`.png` ou `.svg`).
