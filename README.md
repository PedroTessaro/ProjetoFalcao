# Cyber Bullet System — Central de Controle de Frotas de Drones

Projeto **Securus Dynamics** · equipe **Cyber Bullet System**.

Aplicação **Spring Boot (Java 21)** com interface web, que implementa a
**Central de Controle** de uma frota de drones. Usa **dois bancos de dados**:

- **Relacional (H2)** — dados estruturados/transacionais: operadores, frota e missões.
- **Não relacional (MongoDB embarcado)** — dados de tempo real e logs imutáveis:
  telemetria, eventos de ameaça e trilha de auditoria (hash-chain).

> Os **drones, sensores (LIDAR/câmera/GPS) e o SO embarcado NÃO são implementados** —
> eles são **simulados**. Procure no código os comentários `[SIMULACAO ...]`, que
> marcam exatamente onde entraria o hardware/firmware real, sempre acompanhados de
> `prints`/logs nos entrypoints.

---

## Como executar

Pré-requisitos: **JDK 21+** e **Maven** (já instalados neste ambiente). Não é
preciso instalar banco de dados — o H2 é embarcado e o MongoDB sobe sozinho
(Flapdoodle baixa o binário na primeira execução; precisa de internet só uma vez).

```bash
mvn spring-boot:run
```

Acesse: **http://localhost:8080** → redireciona para o login.

### Credenciais (criadas automaticamente)

| Usuário    | Senha         | Perfil   |
|------------|---------------|----------|
| `admin`    | `admin123`    | ADMIN    |
| `operador` | `operador123` | OPERATOR |
| `viewer`   | `viewer123`   | VIEWER   |

Todos exigem **MFA/TOTP**. Na tela de MFA há um QR Code (para Google
Authenticator/Authy) e, em **modo demonstração**, o código válido é exibido na
própria tela e impresso no log do servidor (`[MFA-DEMO]`) — assim dá para entrar
sem celular. Desligue isso em produção via `cyberbullet.security.demo-print-totp=false`.

Console do H2 (inspeção do banco relacional): **http://localhost:8080/h2-console**
(JDBC URL `jdbc:h2:file:./data/cyberbullet`, usuário `sa`, sem senha).

---

## Interface web (telas)

- **Dashboard** (`/dashboard`) — telemetria em tempo real (polling a cada 2,5s via
  `/api/dashboard`), KPIs da frota, tabela de processos do SO embarcado e ameaças.
- **Frota** (`/fleet`) — lista de drones e **controle remoto** (Decolar/Recolher/Pousar).
- **Missões** (`/missions`) — planejamento/execução; a prioridade alimenta o
  escalonamento do SO embarcado.
- **Auditoria** (`/audit`) — trilha imutável + verificação de integridade da cadeia.

---

## Mapa: requisitos do enunciado → onde está no código

### 2.1 Requisitos e Funcionalidades

| Requisito | Implementação |
|-----------|---------------|
| **1. Central de Controle** — gerência de frota, controle remoto/autônomo, dashboard em tempo real | `FleetController`, `DroneService`, `DashboardController` + `dashboard.js`/`DashboardApiController` (polling). Retorno autônomo à base por bateria crítica em `TelemetrySimulator`. |
| **2. Navegação Inteligente** — LIDAR/câmera/GPS, detecção/evasão de ameaças, rede neural | `NavigationService.senseAndNavigate()` — blocos `[SIMULACAO LIDAR/CAMERA/GPS/REDE NEURAL]`; gera `ThreatEvent` (MongoDB). |
| **3. Gerenciamento de Comunicação** — protocolo seguro em tempo real, fallback | `CommunicationService.sendSecureCommand()` — canal primário (mTLS/5G) com **fallback** para satélite; `[SIMULACAO CRIPTOGRAFIA/RADIO]`. |
| **4. Banco de Dados e Auditoria** — logs de missões/eventos críticos, criptografia + assinatura, NoSQL distribuído | `AuditService` (hash-chain imutável) + `CryptoService` (SHA-256 + assinatura HMAC); documentos no **MongoDB**. |
| **5. Embarcados e Segurança** — biometria + MFA, monitoramento de processos do SO | `TotpService` (TOTP real) + `MfaSuccessHandler` (biometria simulada) + `MfaFilter`; `EmbeddedOsMonitorService` (tabela de processos/watchdog). |

### 2.2 Fluxo do Sistema (problemas a resolver)

| Problema | Como é tratado |
|----------|----------------|
| **1. Arquitetura deficiente** (latência, interrupções, sem failover) | Comunicação com **fallback** automático (`CommunicationService`); telemetria desacoplada em NoSQL para escala/baixa latência. *(failover de servidor: ver "Notas".)* |
| **2. Segurança** (invasões, controle não autorizado, auditoria imutável) | **MFA/TOTP + biometria simulada + senha BCrypt** (Spring Security); comandos cifrados/assinados; **trilha de auditoria imutável** com cadeia de hash + verificação. |
| **3. Banco de dados** (integridade/sincronização em tempo real, histórico, distribuído/replicado) | Integridade via **optimistic locking** (`@Version` em `Drone`) e transações; histórico de missões no relacional (auditoria/análise preditiva); tempo real e logs no MongoDB (replicável via replica set/sharding). |
| **4. SO e concorrência** (múltiplas threads, prioridade por criticidade) | `EmbeddedOsMonitorService`: tabela de processos (sensor_fusion, navigation, ai_inference, comms, telemetry, watchdog) com **prioridade derivada da criticidade da missão** (`MissionPriority`). |

---

## Arquitetura (pacotes)

```
com.securus.cyberbullet
├── domain          # Entidades relacionais (H2/JPA): Operator, Drone, Mission + enums
├── document        # Documentos NoSQL (MongoDB): TelemetryRecord, ThreatEvent, AuditLog
├── repository      # Repositórios JPA (relacional) e Mongo (NoSQL)
├── service         # Regras + SIMULAÇÕES (Navigation, Communication, EmbeddedOsMonitor,
│                   #   TelemetrySimulator, Audit, Crypto, Totp, Drone, Mission)
├── security        # Spring Security + fluxo MFA (SuccessHandler, Filter, Config)
├── controller      # Web (Thymeleaf) + API REST (/api/dashboard para o tempo real)
└── config          # DataSeeder (dados iniciais)
```

### Por que dois bancos?

- **Relacional (H2):** consistência forte e relações (operador → missão → drone),
  ideal para histórico/auditoria de negócio e análise preditiva.
- **NoSQL (MongoDB):** alto volume e escrita rápida de **telemetria em tempo real** e
  **logs imutáveis**, com caminho natural para distribuição/replicação (replica set,
  sharding) — atende continuidade de operação e alta disponibilidade.

---

## Trilha de auditoria imutável (como funciona)

Cada evento crítico vira um `AuditLog` cujo `hash = SHA-256(conteúdo + hashAnterior)`
e que é **assinado** (`CryptoService.sign`). Como cada registro inclui o hash do
anterior, **alterar qualquer linha antiga quebra a verificação de toda a cadeia** a
partir dela — detectado em `/audit` por `AuditService.verifyChain()`.

---

## Notas / o que é simulado vs. real

- **Real:** Spring Boot, Spring Security, JPA/H2, Spring Data MongoDB (embarcado),
  TOTP/MFA (RFC 6238, compatível com apps autenticadores), SHA-256/HMAC, cadeia de
  hash de auditoria, optimistic locking, dashboard com polling.
- **Simulado (com logs `[SIMULACAO ...]`):** sensores LIDAR/câmera/GPS, rede neural de
  navegação, rádio/transporte com os drones, leitor biométrico, RTOS/processos de bordo,
  e a própria geração de telemetria (`TelemetrySimulator` faz o papel dos drones reais
  transmitindo dados).
- **Failover de servidor / MongoDB distribuído:** aqui rodamos uma instância embarcada
  única. Em produção seria um **replica set** (alta disponibilidade) e os drones teriam
  brokers redundantes; o ponto de extensão é o `CommunicationService` + a config do Mongo.
- **Persistência:** o H2 é **em arquivo** (`./data/`, persiste entre execuções); o
  MongoDB embarcado é **efêmero** (reinicia limpo a cada start) — adequado para demo.
