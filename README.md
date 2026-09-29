# Missão Marte Unifor — refatoração SOLID

Base: [solid-tutorial](https://github.com/marcelobezerra-dotcom/solid-tutorial).
Repositório compartilhado: https://github.com/paulo-edvandro/missao-marte-solid.

## Integrantes e contribuições

- Paulo Edvandro Rocha Filho (`paulo-edvandro`): fluxo da partida, ponto de entrada,
  integração, testes do fluxo e atualização da documentação.
- Lucas Alencar (`luken6406`): entidades e regras do modelo, diagrama de classes.
- Emerson (`EmsRibeiro`): ranking em JSON, renderização do mapa e diagrama de pacotes.

As correções de integração complementam a persistência,
seus testes e a renderização, sem alterar os arquivos do modelo ou o diagrama
sob responsabilidade de Lucas. O histórico de commits registra essas etapas.

O original em `src/exercicio10/` permanece preservado. A versão refatorada
está em `src/solidexercicio10/`. Os contratos estão em
[docs/CONTRATOS.md](docs/CONTRATOS.md).

## Compilar e executar

É necessário **JDK 17 ou superior**. Execute os comandos na raiz do projeto.
Use os comandos correspondentes ao seu terminal.

### Git Bash, Linux ou macOS

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/solidexercicio10 -name '*.java')
java -cp out solidexercicio10.Main
```

### PowerShell

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src/solidexercicio10 | ForEach-Object FullName)
java -cp out solidexercicio10.Main
```

Para compilar e executar o original, em qualquer um desses terminais:

```text
javac -encoding UTF-8 -d out src/exercicio10/*.java
java -cp out exercicio10.Main
```

O original grava `ranking.json`; o refatorado grava
`ranking-solid-exercicio10.json` no diretório em que o programa é executado.

## Como jogar

O menu permite iniciar uma missão, consultar o Top 5, resetar o ranking com
confirmação e sair. Escolha nome, dificuldade e tamanho do mapa.

- `w`, `a`, `s`, `d`: movimentar a nave; cada comando de movimento custa um ponto.
- `c`: embarcar um passageiro na posição da nave.
- `q`: abandonar a missão e mostrar suas estatísticas.
- Vitória: embarcar todos os passageiros e retornar à plataforma `L` em `(0,0)`.
- Derrota: perder as três vidas ou atingir pontuação zero.

O tamanho informado é o alcance de cada eixo: 5 gera coordenadas de -5 a +5,
isto é, uma grade de 11 × 11. São aceitos valores de 2 a 50; entradas inválidas
usam 5. O mínimo garante espaço para todas as dificuldades.

A nave tem cinco lugares. O modo fácil possui quatro passageiros e inicia com
30 pontos; médio e difícil possuem cinco passageiros e iniciam com 20 e 15
pontos. Professor vale 10, engenheiro 15 e astronauta 20. Há um, dois ou três
asteroides e a mesma quantidade de inimigos, conforme a dificuldade.

O mapa usa `N` para nave, `P` para passageiro, `A` para asteroide, `I` para
inimigo, `L` para plataforma e `.` para espaço vazio. A lista abaixo do mapa
identifica o nome, tipo e posição dos passageiros. A nave se sobrepõe à
plataforma quando está na origem; ao sair, a plataforma reaparece se estiver livre.

## Organização e decisões de projeto

- `Main` cria e conecta ranking, renderizador, console, fábrica de missão e gerador aleatório.
- `model` mantém entidades, movimento, embarque e colisões sem imprimir ou
  persistir dados. As subclasses definem os pontos por `getPontuacao()`.
- `service/JogoService` coordena as opções do menu, partida e seleção de pontuação
  para o ranking, sem formatar ou imprimir mensagens. Recebe suas dependências
  pelo construtor.
- `service/FabricaMissao` monta a nave e distribui as entidades. Seu catálogo
  recebe funções que criam passageiros. A configuração padrão preserva nomes,
  ordem e quantidades do original; um catálogo diferente pode inserir novos tipos
  sem editar o serviço. Um catálogo ampliado ajusta a capacidade da nave.
- `presentation/JogoConsole` apresenta menu, solicitações, mensagens, ranking e
  estatísticas usando um `PrintStream` recebido. Não movimenta entidades, decide
  vitória ou acessa arquivos.
- `presentation/MapaRenderer` desenha o mapa sem alterar a missão. A orientação
  das linhas acompanha o original: `w` diminui `y` e move a nave para cima.
- `repository/RankingRepository` define operações de persistência e propaga
  `IOException`. `ArquivoRankingRepository` grava e lê JSON, ordenando e
  limitando o resultado ao Top 5. As mensagens de falha são responsabilidade
  do serviço. Sucesso e novo recorde só são anunciados após gravar.

O ranking usa um leitor específico para arrays de objetos com strings e
inteiros, sem dependências externas. Aspas, barras e caracteres de controle
são escapados. As dificuldades novas são gravadas pelo nome estável do enum;
valores antigos com acentos continuam sendo lidos. Campos opcionais ausentes
recebem valores padrão. Arquivos inválidos produzem `IOException`; salvar
não sobrescreve um ranking cuja leitura falhou. O usuário pode resetá-lo pelo menu.

## Diagramas

- [Classes do domínio — imagem](docs/uml/diagrama-classes-model.svg) e
  [fonte PlantUML](docs/uml/diagrama-classes-model.puml): entidades, herança,
  interfaces e relações do modelo. Lucas ainda deve completar `Dificuldade`
  e conferir a coerência da imagem, relações e multiplicidades com o código.
- [Pacotes — imagem](docs/uml/diagrama-pacotes.png) e
  [fonte Mermaid](docs/uml/diagrama-pacotes.mmd): dependências entre as camadas.
  O serviço importa `RankingRepository`, enquanto `Main` instancia
  `ArquivoRankingRepository`. A fonte atual mostra dependências por pacote;
  essa distinção deve ser explicitada no diagrama antes da entrega. Após a
  refatoração de Paulo, `presentation` também utiliza `RankingEntry` de
  `repository` para apresentar os dados; Emerson deve atualizar a fonte e a imagem.

## Testes

Após compilar a versão nova, estes comandos funcionam no Git Bash e no PowerShell:

```text
javac -encoding UTF-8 -cp out -d out tests/*.java
java -cp out JogoServiceTest
java -cp out ArquivoRankingRepositoryTest
java -cp out MapaRendererTest
java -cp out SolidPauloTest
```

Todos passaram na validação de 29/09/2026 com Java 17. Os testes de arquivo
usam um diretório temporário e não modificam o ranking do jogador.

`SolidPauloTest` verifica uma partida completa com um passageiro novo (`Cientista`)
fornecido pelo catálogo. A pontuação final passa de 82 para 97 pela diferença de
bônus, sem alterar `JogoService`. Também verifica dificuldades, capacidade,
posições livres e a busca alternativa quando o gerador repete a mesma posição.

Resultados e procedimentos: [docs/TESTES-FLUXO.md](docs/TESTES-FLUXO.md).
As verificações anteriores do modelo estão em
[docs/TESTES-MODELO.md](docs/TESTES-MODELO.md).

## Limitações e pendências da entrega

- O jogo permanece um aplicativo de console. A leitura com `Scanner` e a
  coordenação das opções ficam no serviço; textos e formatos ficam na apresentação.
- A fábrica exige pelo menos cinco entradas de catálogo para preservar a
  configuração padrão. `Main` pode fornecer um catálogo diferente pelo construtor
  da fábrica. Quantidades e regras de dificuldade continuam explícitas na fábrica.
- A apresentação usa classes concretas pequenas; não foi criada uma interface
  para cada saída de console. DIP é demonstrado principalmente por `RankingRepository`.
- O leitor de JSON aceita o formato específico do ranking, não estruturas
  JSON arbitrárias. Não há escrita atômica ou controle de gravação simultânea.
- Símbolos e limite do mapa são mudanças visuais e de validação deliberadas.
- Completar os diagramas conforme indicado acima, preservando a responsabilidade
  dos integrantes por seus arquivos.
- Criar `REVISAO-SOLID.md` somente após concluir o projeto, com observações sobre
  os cinco princípios, melhorias priorizadas, concordância e discordância
  justificadas e resultados dos testes.

## Entrega individual

O enunciado exige um repositório individual por aluno contendo o original,
a versão refatorada completa, diagramas com fontes, README e revisão SOLID.
O repositório compartilhado serve ao desenvolvimento e à integração.

Antes de enviar, cada aluno deve adaptar o README para identificar o autor
da entrega e seu repositório, conferir todos os arquivos e preservar o histórico
de contribuições. Envie no Moodle o link completo do repositório público ou
libere acesso para `marcelobezerra-dotcom`. A apresentação é em equipe e todos
precisam explicar o funcionamento, as decisões, os testes e as limitações.
