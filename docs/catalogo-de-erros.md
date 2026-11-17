# Catálogo de erros e avisos — Totem SnackSys

Este documento lista as situações de erro, os avisos e as confirmações do totem de autoatendimento. Ele serve de base para o protótipo no Figma, para a implementação e para os casos de teste.

- **Status `Prototipado`**: a tela já existe no Figma (seção "Estados de erro").
- **Status `Implementar no código`**: a regra está definida, mas a tela fica para a implementação.
- **`A confirmar`**: sugestão ainda não aprovada pela equipe.

Contexto: o SnackSys é um projeto acadêmico, então pagamento e falhas de sistema são **simulados** para demonstração.

## Tipos de aviso

| Tipo | Quando usar | Exemplo |
|---|---|---|
| Inline | Corrigir algo no próprio campo ou botão, sem interromper | "Digite seu nome para continuar" |
| Toast | Informação rápida que some sozinha | "Sem conexão. Tentando reconectar…" |
| Pop-up | Decisão importante ou que apaga dados | "Recomeçar o pedido?" |
| Tela cheia | O fluxo não pode continuar | "Pagamento não aprovado" |

Princípio: **prevenir é melhor que avisar**. Sempre que possível, o botão fica desabilitado antes de o erro acontecer.

## Resumo

| ID | Situação | Tipo | Status |
|---|---|---|---|
| E01 | Cliente inativo | Pop-up com contagem | Prototipado |
| E02 | Recomeçar pedido | Pop-up de confirmação | Prototipado |
| E03 | Produto esgotado | Tarja no card | Prototipado |
| E04 | Sacola vazia | Estado vazio | Prototipado |
| E05 | Item da sacola esgotou durante a compra | Aviso no item | Implementar no código |
| E06 | Quantidade máxima de um item | Inline | Implementar no código |
| E07 | Nome vazio | Inline | Prototipado |
| E08 | Nome curto ou longo demais | Inline | Prototipado |
| E09 | Pagamento recusado | Tela cheia | Prototipado |
| E10 | Tempo do Pix esgotado | Tela cheia | Implementar no código |
| E11 | Cancelar durante o pagamento | Pop-up | Implementar no código |
| E12 | Falha ao registrar o pedido | Tela cheia | Implementar no código |
| E13 | Pagamento aprovado, pedido não gravado | Tela cheia | Implementar no código |
| E14 | Sem conexão | Faixa no topo | Implementar no código |
| E15 | Totem indisponível | Tela cheia | Implementar no código |
| E16 | Erro inesperado | Tela cheia | Implementar no código |
| E17 | Pedido de ajuda | Pop-up | Implementar no código |

## Detalhamento

### E01 — Cliente inativo

- **Onde:** todas as telas do pedido, menos "Processando" e "Pedido confirmado".
- **Gatilho:** 30 segundos sem nenhum toque na tela.
- **Comportamento:** abre um pop-up com contagem regressiva de 15 segundos.
- **Mensagem:** "Você ainda está aí? Seu pedido será apagado em 15 s."
- **Ações:**
  - [Continuar pedido] fecha o pop-up e reinicia a contagem de inatividade.
  - [Recomeçar] apaga o pedido e volta à tela de espera.
  - Se a contagem chegar a zero, o pedido é apagado e o totem volta à tela de espera.
- **Demonstração:** botão discreto "Simular inatividade (demo)" abre o pop-up na hora.
- **Critério de aceitação:** Dado que o cliente tem itens na sacola, quando ficar 30 s sem tocar, então o pop-up aparece; se ficar mais 15 s sem tocar, então o pedido é apagado e a tela de espera é exibida.

### E02 — Recomeçar pedido

- **Onde:** cardápio e sacola, no botão "Recomeçar pedido".
- **Comportamento:** pop-up de confirmação.
- **Mensagem:** "Recomeçar o pedido? Os itens da sacola serão apagados."
- **Ações:**
  - [Cancelar] fecha o pop-up e mantém o pedido.
  - [Recomeçar] apaga o pedido e volta à tela de espera.
- **Critério de aceitação:** Dado que há itens na sacola, quando o cliente tocar em "Recomeçar pedido" e confirmar, então a sacola fica vazia e a tela de espera é exibida; se cancelar, então nada muda.

### E03 — Produto esgotado

- **Onde:** cards do cardápio.
- **Comportamento:** o card mostra a tarja "Esgotado", com foto e textos esmaecidos (componente "Card de produto · Estado=Esgotado"). Tocar no card não faz nada e não mostra aviso.
- **Exemplo no protótipo:** Bolo de cenoura.
- **Critério de aceitação:** Dado que um produto está com estoque zerado, quando o cardápio for exibido, então o card aparece com a tarja "Esgotado" e não pode ser adicionado à sacola.

### E04 — Sacola vazia

- **Onde:** sacola, depois que o cliente remove o último item.
- **Comportamento:** a sacola mostra "Sua sacola está vazia" e o botão [Voltar ao cardápio]. O botão "Pagar pedido" fica desabilitado. A barra do cardápio mostra o estado vazio ("Seu pedido está vazio", com "Ver pedido" desabilitado).
- **Critério de aceitação:** Dado que a sacola tem um item, quando o cliente removê-lo, então a sacola mostra o estado vazio e não é possível ir para o pagamento.

### E05 — Item da sacola esgotou durante a compra

- **Onde:** sacola e resumo.
- **Comportamento:** o item recebe o aviso "Este item acabou. Remova para continuar." e "Pagar pedido" fica desabilitado até o item ser removido.
- **Critério de aceitação:** Dado um item na sacola cujo estoque zerou, quando a sacola for aberta, então o aviso aparece no item e o pagamento fica bloqueado.

### E06 — Quantidade máxima de um item

- **Onde:** sacola, no botão "+".
- **Comportamento:** ao atingir o limite, o "+" fica desabilitado e aparece "Máximo de 10 unidades".
- **A confirmar:** o valor do limite (sugestão: 10).

### E07 — Nome vazio

- **Onde:** tela "Qual é o seu nome?".
- **Comportamento:** ao tocar em "Continuar" com o campo vazio, a borda do campo fica vermelha e aparece "Digite seu nome para continuar".
- **Critério de aceitação:** Dado o campo de nome vazio, quando o cliente tocar em "Continuar", então a mensagem aparece e o fluxo não avança.

### E08 — Nome curto ou longo demais

- **Onde:** tela "Qual é o seu nome?".
- **Regra:** o nome deve ter de 2 a 20 letras. O teclado na tela só tem letras e espaço, então números e símbolos são evitados por prevenção.
- **Mensagem:** "Use de 2 a 20 letras."
- **Critério de aceitação:** Dado um nome com 1 letra ou com mais de 20, quando o cliente tocar em "Continuar", então a mensagem aparece e o fluxo não avança.

### E09 — Pagamento recusado

- **Onde:** depois da tela "Processando".
- **Mensagem:** "Pagamento não aprovado. Nenhum valor foi cobrado."
- **Ações:**
  - [Tentar de novo] volta para a tela "Processando".
  - [Escolher outra forma] volta para a tela de forma de pagamento.
- **Demonstração:** botão discreto "Simular falha (demo)" no canto da tela "Processando".
- **Critério de aceitação:** Dado um pagamento recusado, quando a resposta chegar, então a tela de recusa aparece, o pedido continua salvo e nenhum valor é cobrado.

### E10 — Tempo do Pix esgotado

- **Mensagem:** "O tempo do Pix acabou."
- **Ações:** [Gerar novo código] e [Escolher outra forma].
- **A confirmar:** o tempo de validade do código.

### E11 — Cancelar durante o pagamento

- **Mensagem:** "Cancelar o pagamento? Seu pedido continua salvo."
- **Ações:** [Continuar pagando] e [Cancelar].

### E12 — Falha ao registrar o pedido

- **Mensagem:** "Não conseguimos registrar seu pedido. Nenhum valor foi cobrado."
- **Ações:** [Tentar de novo] e [Chamar atendente].

### E13 — Pagamento aprovado, pedido não gravado

- **Mensagem:** "Seu pagamento foi aprovado, mas houve um problema. Mostre este código ao atendente: #A1B2."
- **Observação de qualidade:** é o caso mais crítico de integridade de dados. O código precisa permitir que a equipe localize o pagamento e registre o pedido manualmente.

### E14 — Sem conexão

- **Comportamento:** faixa no topo: "Sem conexão. Tentando reconectar…". Some sozinha quando a conexão volta.

### E15 — Totem indisponível

- **Mensagem:** "Totem indisponível no momento. Faça seu pedido no caixa."

### E16 — Erro inesperado

- **Mensagem:** "Algo deu errado." com o código do erro.
- **Ação:** [Voltar ao início].

### E17 — Pedido de ajuda

- **Onde:** botão "Ajuda" do cabeçalho.
- **Mensagem:** "Precisa de ajuda? Um atendente virá até você."
- **Ações:** [Chamar atendente] e [Fechar].
