# Verificação do projeto integrado

## Validação de 29/09/2026

Base: `main` após o merge dos PRs #4 e #5, commit
`76bc1c67165db5802954124f78c8c02eb50ec43a`. Correções de integração feitas
na `branch-correçao`. O código original e os arquivos do modelo de Lucas
não foram alterados nesta etapa.

A versão original e a refatorada compilam com Java 17. O original foi
comparado por conteúdo com os arquivos preservados no Git. Os quatro testes
executáveis abaixo passaram. Compilação não equivale a comprovar todo o
comportamento possível; os cenários cobertos estão descritos a seguir.

## Como repetir

Na raiz do projeto, compile a versão nova conforme o README. Depois execute,
no Git Bash ou no PowerShell:

```text
javac -encoding UTF-8 -cp out -d out tests/*.java
java -cp out JogoServiceTest
java -cp out ArquivoRankingRepositoryTest
java -cp out MapaRendererTest
java -cp out SolidPauloTest
```

Para verificar a compilação original:

```text
javac -encoding UTF-8 -d out src/exercicio10/*.java
```

## Fluxo — `JogoServiceTest`

Usa modelo e renderizador reais, posições aleatórias controladas e repositório
em memória. Cenários aprovados:

- vitória com cinco passageiros e retorno à plataforma;
- rota de oito movimentos no modo médio termina com 82 pontos;
- resgatar todos sem pousar não registra vitória nem ranking;
- Top 5: empate com o quinto não substitui registros; pontuação maior entra;
- consulta, cancelamento e confirmação do reset;
- dimensão inválida usa 5; nave mantém cinco lugares no modo fácil;
- abandono mostra estatísticas e recorde anterior, sem salvar pontuação;
- perda das três vidas e pontuação zero encerram com estatísticas;
- fim da entrada no menu, preparação e partida não causa loop infinito;
- erros simulados de leitura, escrita e limpeza usam o contrato `IOException`;
- gravação malsucedida não anuncia entrada no ranking nem novo recorde;
- limpeza malsucedida não anuncia reset bem-sucedido.

## Arquivo e integração — `ArquivoRankingRepositoryTest`

Usa arquivo real em diretório temporário, removido ao final. Cenários aprovados:

- arquivo inexistente produz lista vazia e pode ser resetado;
- nomes com aspas, barras, chaves, vírgulas, dois-pontos, acentos e emoji;
- preservação de escapes de controle e leitura de Unicode escapado;
- Top 5 ordenado, limite de cinco e preservação dos anteriores em empate;
- leitura ordena registros antigos e aceita ausência de campos opcionais;
- rejeição de JSON truncado, escapes inválidos, campos obrigatórios ausentes,
  números inválidos ou fora da faixa, vírgula final e conteúdo excedente;
- tentativa de salvar sobre arquivo inválido preserva o conteúdo original;
- falha de leitura de arquivo inválido avisa e permite retornar ao menu;
- partida completa grava 82 pontos e cinco passageiros em JSON;
- outra execução do serviço lê essa pontuação; reset confirmado remove o arquivo;
- falhas reais ao ler um diretório, remover diretório não vazio e gravar num
  diretório pai inexistente são propagadas como `IOException`.

## Apresentação — `MapaRendererTest`

Cenários aprovados:

- `w` coloca a nave visualmente acima da plataforma;
- a plataforma aparece quando a nave deixa a origem;
- a lista identifica nome, tipo e coordenadas dos passageiros;
- desenhar não modifica posição da nave nem a lista da missão.

## Ajustes desta etapa

1. Testes atualizados para `IOException`, conforme a interface integrada por Emerson.
2. Leitura e escrita de strings JSON corrigidas; chaves dentro do nome não
   encerram objetos. JSON inválido gera erro em vez de registros truncados.
3. Leitura garante ordenação e limite Top 5, inclusive em arquivos existentes.
4. O serviço anuncia novo recorde somente depois de salvar com sucesso.
5. O mapa acompanha a orientação original dos comandos e identifica coordenadas
   e tipos de passageiros, mantendo os símbolos do modelo.
6. README e contratos atualizados para refletir a implementação atual.

## O que permanece

A documentação de modelagem ainda deve ser concluída pelos responsáveis.
A revisão crítica SOLID será feita quando o projeto estiver pronto.
O serviço delega saída de console a `JogoConsole` e criação a `FabricaMissao`.
O construtor recebe todas as dependências; `Main` monta a configuração real.
O JSON é específico para este formato e não há proteção contra duas instâncias
escrevendo simultaneamente ou interrupção no meio da gravação.

## Refatoração SOLID da parte de Paulo

Base: `main` após o merge das correções de integração, commit
`7a4db579352501a1cb97adf3e0325e580cba73a0`. Branch de destino:
`branch-solid-paulo`. Nenhum arquivo do modelo, persistência, renderizador ou
UML foi alterado nesta etapa. A revisão crítica continua adiada.

- `JogoService` deixa de imprimir ou formatar mensagens e de escolher tipos
  concretos de passageiros.
- `JogoConsole` concentra a apresentação; seu `PrintStream` é injetado.
- `FabricaMissao` concentra criação e posicionamento, com catálogo configurável.
- Os testes existentes foram adaptados para o construtor com cinco dependências,
  preservando as verificações de vitória, pouso, pontuação e erros de arquivo.
- Os quatro testes passaram em Java 17, e o original continua compilando.

### `SolidPauloTest`

Um novo passageiro `Cientista`, definido somente no teste, entra pelo catálogo.
A mesma rota de vitória termina com 97 pontos, pois seu bônus é 25 em vez dos
10 do professor substituído. Isso demonstra extensão do fluxo sem editar o
serviço. O teste também verifica:

- configurações padrão fácil, médio e difícil, com capacidade cinco;
- geração de posições sem sobreposição e fora da origem;
- busca alternativa que termina mesmo com gerador aleatório constante;
- catálogo ampliado com seis passageiros e capacidade suficiente;
- rejeição de mapa pequeno demais antes de iniciar a geração;
- menu e estatísticas escritos no console injetado.
