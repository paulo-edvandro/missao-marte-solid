# Resultados dos testes do modelo

As regras abaixo foram verificadas durante a validação do modelo.

Regras verificadas:

- Professor vale 10, Engenheiro vale 15 e Astronauta vale 20;
- embarque retorna sucesso quando há capacidade;
- embarque retorna falha quando a nave está cheia;
- passageiro só é removido da missão após embarque bem-sucedido;
- nave respeita os limites do mapa e os comandos `w`, `a`, `s` e `d`;
- inimigo se movimenta em uma das quatro direções sem sair dos limites;
- missão detecta colisões com asteroides;
- missão identifica passageiros ainda não embarcados.

Também foi verificado que o código original em `src/exercicio10/` continua compilando
sem alterações.
