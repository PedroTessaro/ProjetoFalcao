# 🎬 Roteiro de Apresentação em Vídeo — Cyber Bullet System

**Duração alvo:** até 10 min (ideal ~8–9 min). Foco: **como o projeto funciona,
o fluxo do usuário e a navegação** — com pitadas de explicação técnica.

---

## ✅ Antes de gravar (checklist de preparação)

1. **Deixe a aplicação rodando**: `mvn spring-boot:run` (espere o log
   `Started CyberBulletApplication`).
2. **Organize a tela em 2 partes** (recomendado): à esquerda o **navegador**
   (`http://localhost:8080`), à direita o **terminal** com os logs rolando.
   Assim a plateia vê os `[SIMULACAO]`, `[NAV]`, `[COMM]`, `[OS-MONITOR]`
   acontecendo enquanto você navega.
3. **Tenha a tela de Frota com pelo menos 1 drone IDLE** (ex.: FALCAO-03) para
   demonstrar a missão crítica ao vivo.
4. **Aumente a fonte do navegador** (Ctrl/Cmd +) e do terminal — vídeo fica mais legível.
5. Deixe o **README.md aberto** numa aba, caso queira mostrar o mapa de requisitos.
6. Faça **logout** antes de começar, para gravar o login do zero.

> 💡 Dica de gravação: fale com calma, e quando o roteiro disser *[mostrar]*,
> faça a ação devagar para a câmera acompanhar.

---

## ⏱️ Linha do tempo (resumo)

| Bloco | Tempo | Assunto |
|-------|-------|---------|
| 1 | 0:00–0:45 | Abertura e contexto |
| 2 | 0:45–2:00 | Visão geral + arquitetura (técnico leve) |
| 3 | 2:00–2:50 | Como o projeto foi criado |
| 4 | 2:50–4:00 | Fluxo de login + MFA (segurança) |
| 5 | 4:00–5:30 | Dashboard em tempo real |
| 6 | 5:30–6:45 | Frota + controle remoto + comunicação/fallback |
| 7 | 6:45–7:55 | Missões + prioridade do SO embarcado |
| 8 | 7:55–8:40 | Auditoria imutável |
| 9 | 8:40–9:10 | Encerramento |

---

## 🎙️ Roteiro detalhado (fala + ação na tela)

### BLOCO 1 — Abertura e contexto · `0:00–0:45`

**[Tela: slide simples ou a tela de login do sistema]**

> "Olá! Esse é o **Cyber Bullet System**, a central de controle de frotas de
> drones que desenvolvi para a **Securus Dynamics**. O objetivo é resolver os
> problemas que eles tinham: latência e quedas de comunicação em missões
> críticas, falhas de segurança e a necessidade de auditoria confiável.
>
> Importante: eu **não construí os drones nem o hardware** — câmera, LIDAR, GPS.
> Eu construí a **central** que comanda e monitora a frota, e os sistemas de
> bordo são **simulados** no código, com logs mostrando exatamente onde entraria
> o hardware real. Vou mostrar isso ao longo do vídeo."

---

### BLOCO 2 — Visão geral + arquitetura · `0:45–2:00`

**[Tela: pode mostrar a estrutura de pastas no editor, ou o diagrama do README]**

> "Tecnicamente, é uma aplicação **Spring Boot em Java**, com interface web feita
> em **Thymeleaf** — HTML renderizado no servidor — mais CSS e um pouco de
> JavaScript para o tempo real.
>
> O que eu acho mais interessante é que o sistema usa **dois bancos de dados ao
> mesmo tempo**, cada um para o que faz melhor:
>
> - Um **banco relacional**, o **H2**, guarda os dados estruturados: operadores,
>   a frota de drones e o histórico de missões.
> - E um **banco não relacional**, o **MongoDB**, guarda os dados de alto volume
>   e tempo real: a **telemetria** dos drones, os **eventos de ameaça** e a
>   **trilha de auditoria**.
>
> A ideia é que o relacional dá consistência e relações, e o NoSQL dá escala e
> velocidade de escrita — e é naturalmente distribuível e replicável, o que
> atende o requisito de alta disponibilidade."

**[Tela: mostrar rapidamente os pacotes `domain`, `document`, `service`]**

> "No código, separei bem: `domain` são as entidades relacionais, `document` são
> os documentos do Mongo, e em `service` ficam as regras de negócio e as
> **simulações** dos sistemas do drone."

---

### BLOCO 3 — Como o projeto foi criado · `2:00–2:50`

**[Tela: mostrar um service com os comentários `[SIMULACAO ...]`, ex.: `NavigationService.java`]**

> "Sobre o processo de criação: comecei modelando o domínio — quem é um operador,
> um drone, uma missão — e escolhendo os dois bancos. Depois construí as camadas:
> entidades, repositórios, serviços, controllers e as telas.
>
> A parte mais característica são essas **simulações**. Olha aqui no
> `NavigationService`: onde estaria a leitura do LIDAR, da câmera, do GPS e a
> rede neural de navegação, eu deixei marcado com `[SIMULACAO]` e gerei logs.
> Então o sistema **se comporta** como se os drones existissem — gera telemetria,
> detecta ameaças, decide manobras — só que os dados são sintéticos."

**[Opcional, se sobrar tempo: mostrar o `pom.xml` rapidamente]**

> "Como o foco era facilitar a execução, o H2 é embarcado e o **MongoDB também
> sobe sozinho** junto com a aplicação — não precisa instalar banco nenhum."

---

### BLOCO 4 — Fluxo de login + MFA (segurança) · `2:50–4:00`

**[Tela: navegador na tela de login]**

> "Agora o fluxo do usuário. Tudo começa no login. A segurança foi um requisito
> forte, porque o sistema antigo tinha sofrido tentativas de invasão.
>
> Eu uso **três fatores de autenticação**."

**[Ação: digitar usuário `admin` e senha `admin123` → clicar em Autenticar]**

> "O **primeiro fator** é a senha — aqui com Spring Security, e a senha é
> guardada com hash BCrypt, nunca em texto puro."

**[Tela: redirecionou para a tela de MFA, com QR Code]**

> "Aí entra o **segundo fator**: a autenticação multifator com **TOTP** —
> aquele código que muda a cada 30 segundos, compatível com Google Authenticator.
> Esse QR Code aqui é justamente para cadastrar no app autenticador.
>
> E o **terceiro fator** é a **biometria** — que é simulada no servidor; se eu
> olhar o terminal, aparece o log `[SIMULACAO BIOMETRIA] leitor de digital:
> APROVADO`."

**[Ação: apontar para o código demo na tela / digitar o código de 6 dígitos → Verificar]**

> "Para a demonstração, eu deixei o código válido aparecendo na própria tela.
> Digito aqui... e pronto, **só depois dos dois fatores** o sistema libera o
> acesso ao painel."

**[Tela: dashboard abriu]**

---

### BLOCO 5 — Dashboard em tempo real · `4:00–5:30`

**[Tela: dashboard, deixar alguns segundos atualizando sozinho]**

> "Essa é a tela principal: o **dashboard em tempo real**. Repara que os números
> e a telemetria **mudam sozinhos** — a página busca dados novos a cada 2,5
> segundos, sem eu recarregar.
>
> No topo, os indicadores da frota: total de drones, quantos estão **em voo**,
> missões ativas, e a **prioridade do sistema operacional embarcado** — já já
> volto nesse número."

**[Ação: apontar para a tabela de telemetria]**

> "Aqui é a **telemetria** que vem do MongoDB: bateria, altitude, velocidade, a
> leitura do LIDAR e a força do sinal de cada drone."

**[Ação: apontar para a tabela de processos do SO]**

> "Do lado, a **tabela de processos do SO embarcado**: fusão de sensores,
> navegação, IA, comunicação... cada um com uma **prioridade**. Isso simula o
> escalonamento de threads do drone."

**[Ação: apontar para a tabela de ameaças, e mostrar o terminal com logs `[NAV]`]**

> "E embaixo, as **ameaças detectadas**. Esses eventos são gerados pela navegação
> inteligente: quando o LIDAR simulado vê um obstáculo perto, a 'rede neural'
> decide uma **manobra de evasão** — subir, desviar, descer de emergência. No
> terminal dá pra ver os logs `[NAV]` acontecendo em tempo real."

---

### BLOCO 6 — Frota + controle remoto + comunicação/fallback · `5:30–6:45`

**[Ação: clicar em "Frota" no menu]**

> "Vamos navegar. No menu, a tela de **Frota**: a lista de todos os drones, com
> status, bateria e firmware. Daqui eu faço o **controle remoto** — Decolar,
> Recolher, Pousar."

**[Ação: clicar em "Decolar" em um drone IDLE, e olhar o terminal]**

> "Quando eu mando um comando, ele não vai 'cru'. Olha o terminal: o log `[COMM]`
> mostra que o comando é **cifrado** e enviado pelo **canal primário**, com
> confirmação. E aqui está um diferencial do projeto: se o canal primário falha,
> o sistema aciona automaticamente um **canal de fallback**, por satélite — que
> era exatamente o problema de quedas de comunicação que eles tinham."

**[Ação: se aparecer um log de FALLBACK_USED, aponte; senão mencione que acontece aleatoriamente]**

> "Esse fallback é simulado com uma chance de falha, então de vez em quando ele
> aparece nos logs garantindo que o comando chegou mesmo assim."

---

### BLOCO 7 — Missões + prioridade do SO embarcado · `6:45–7:55`

**[Ação: clicar em "Missões"]**

> "Agora as **Missões**. Aqui eu planejo e executo. Vou criar uma missão nova e,
> de propósito, com prioridade **CRÍTICA**."

**[Ação: preencher nome ("Interceptação Crítica"), prioridade CRITICAL, escolher um drone → Criar]**

> "Criei. Repara que a missão entrou no **histórico**, que fica no banco
> relacional — isso serve para auditoria e análise preditiva depois."

**[Ação: clicar em "Iniciar" na missão]**

> "Ao iniciar, o drone recebe o comando de decolagem pela comunicação segura. Mas
> tem uma coisa legal aqui: a **prioridade da missão muda o comportamento do SO
> embarcado**."

**[Ação: voltar ao Dashboard e apontar o número "Prioridade SO embarcado"]**

> "Olha o dashboard: aquela **prioridade de escalonamento** que estava em 5
> **subiu para 10**, porque agora tem uma missão crítica em andamento. E na
> tabela de processos, navegação e IA ganham prioridade mais alta — ou seja, em
> missão crítica, o drone dá mais CPU para os processos de segurança de voo.
> Isso resolve o requisito de **priorizar processos conforme a criticidade**."

---

### BLOCO 8 — Auditoria imutável · `7:55–8:40`

**[Ação: clicar em "Auditoria"]**

> "Por fim, a **Auditoria**. Todo evento crítico — login, comando, ameaça,
> missão — vira um registro aqui. E esses registros são **imutáveis**: cada um
> guarda o **hash do anterior**, formando uma corrente, no estilo blockchain.
>
> Se alguém tentar alterar um registro antigo, ele quebra essa corrente — e o
> sistema **detecta**. Olha aqui em cima: '**Cadeia íntegra**', com todos os
> registros verificados por hash e assinatura digital. Isso atende o requisito
> de logs de auditoria imutáveis e confiáveis."

---

### BLOCO 9 — Encerramento · `8:40–9:10`

**[Tela: pode voltar ao dashboard]**

> "Recapitulando: a central comanda a frota com **autenticação multifator**,
> monitora **telemetria em tempo real**, faz **navegação com evasão de ameaças**,
> garante a **comunicação com fallback**, **prioriza processos** conforme a
> missão e mantém uma **auditoria imutável** — usando um banco **relacional** e
> um **não relacional**, cada um no seu papel.
>
> E tudo isso com os sistemas do drone **simulados**, mas com os pontos de
> integração real claramente marcados no código. Obrigado!"

---

## 🎯 Se precisar cortar para caber em menos tempo

Corte nesta ordem (do menos essencial para o mais):
1. Bloco 3 (criação) — resuma em 2 frases.
2. Parte técnica do Bloco 2 — vá direto para a demo.
3. Detalhes da tabela de processos no Bloco 5.

**O que NUNCA cortar:** Login+MFA (Bloco 4), Dashboard (Bloco 5) e a missão
crítica mudando a prioridade (Bloco 7) — são os momentos mais fortes.

## 🗣️ Frases de efeito (use à vontade)

- "Dois bancos, cada um fazendo o que faz de melhor."
- "Os drones são simulados, mas o sistema se comporta como se fossem reais."
- "Se o canal cair, o sistema não perde o drone — ele troca de canal sozinho."
- "Missão crítica? O drone prioriza quem mantém ele no ar."
- "Mexeu num log antigo, a corrente quebra e a gente vê na hora."
