# CodeQuest

> **Plataforma interativa e gamificada para o aprendizado de lógica e fundamentos de programação.**

O **CodeQuest** é uma aplicação web full-stack desenvolvida para transformar o estudo de conceitos essenciais de computação (como algoritmos, estruturas de controle, entradas/saídas e fluxogramas) em uma jornada dinâmica e envolvente através da gamificação.

---

## Proposta da Aplicação

- **Aprendizado Estruturado e Modular:** O conteúdo é organizado em módulos progressivos. Cada módulo apresenta conceitos teóricos divididos em etapas claras com exemplos práticos.
- **Quizzes Interativos Gamificados:** Ao final de cada módulo, o aluno testa seus conhecimentos em quizzes com sistema de pontuação (**XP**), avaliação por estrelas, controle de tentativas e feedback pedagógico imediato para cada alternativa.
- **Painel Administrativo Completo:** Área de gestão com operações de CRUD para gerenciamento de módulos, etapas de estudo e questões com categorização e níveis de dificuldade.
- **Design Moderno e Responsivo:** Interface com estética *Dark Mode*, tipografia moderna, componentes fluidos e micro-animações focadas na experiência do estudante.

---

## Principais Tecnologias

### **Backend**
- **Java 17:** Linguagem base focada em estabilidade e performance.
- **Javalin 6:** Microframework web moderno, leve e de alta velocidade para criação de rotas REST e renderização SSR.
- **JDBC Puro & HikariCP:** Gerenciamento otimizado de pool de conexões com o banco de dados.
- **Dotenv Java:** Leitura flexível de variáveis de ambiente (`.env` ou variáveis do sistema operacional).
- **Jackson:** Processamento e serialização de dados em formato JSON para as APIs do quiz.

### **Frontend & Apresentação**
- **Thymeleaf 3:** Motor de templates server-side integrado com o Javalin.
- **HTML5 & Vanilla CSS3:** Estrutura semântica e estilização moderna, com layout responsivo e tema escuro (*Dark/Cyberpunk*).
- **JavaScript (ES6+):** Lógica interativa do quiz, feedback visual dinâmico e consumo das APIs internas.

### **Banco de Dados & Infraestrutura**
- **PostgreSQL 16:** Banco de dados relacional para persistência de módulos, questões, etapas e progresso.
- **Docker & Docker Compose:** Provisionamento automatizado do ambiente de banco de dados.
- **Migrations Embutidas:** Criação de tabelas e carga inicial de dados (*seed*) executadas automaticamente na inicialização da aplicação.

### **Testes & Qualidade**
- **JUnit 5:** Bateria de testes unitários e de integração cobrindo regras de negócio, serviços e configurações.
- **Maven:** Gerenciamento de dependências e automação do ciclo de build.

---

## Como Rodar o Projeto

### Pré-requisitos
- [JDK 17+](https://adoptium.net/) instalado e configurado no `PATH`.
- [Maven 3.8+](https://maven.apache.org/) (ou Maven Wrapper).
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) em execução.

### Passo a Passo

1. **Configure as variáveis de ambiente:**
   - Crie o arquivo `.env` a partir do modelo `.env.example`:
   ```bash
   cp .env.example .env
   ```

2. **Inicie o banco de dados via Docker:**
   ```bash
   docker compose up -d
   # ou especificando o serviço definido no docker-compose.yml:
   docker compose up -d postgres
   ```
   *(Caso ocorra conflito de nome de contêiner antigo, execute `docker rm -f codequest-db` e repita o comando).*

3. **Execute a aplicação:**
   - Via linha de comando (Maven):
     ```bash
     mvn compile exec:java -Dexec.mainClass="com.codequest.App"
     ```
   - Ou executando diretamente a classe `com.codequest.App` pela sua IDE.

4. **Acesse as principais páginas no navegador:**
   - **Página Inicial:** [http://localhost:7000](http://localhost:7000)
   - **Trilha de Módulos:** [http://localhost:7000/modulos](http://localhost:7000/modulos)
   - **Quiz Interativo:** [http://localhost:7000/modulos/1/quiz](http://localhost:7000/modulos/1/quiz)
   - **Painel Administrativo:** [http://localhost:7000/admin/modulos](http://localhost:7000/admin/modulos)

---

## Variáveis de Ambiente (Opcional)

As configurações padrão já apontam para o banco provisionado pelo Docker:

```env
PORT=7000
DB_HOST=localhost
DB_PORT=5432
DB_NAME=codequest
DB_USER=postgres
DB_PASSWORD=postgres
```

---

## Executando os Testes

Para rodar toda a suíte de testes unitários:

```bash
mvn test
```

### Testes da experiência visual

- `gjs src/test/js/quiz-auto-advance.gjs.js`: testes do avanço automático após acertos, contagem regressiva, cancelamento e finalização única do quiz.
- `gjs src/test/js/experience.gjs.js`: testes de tema e sequência do tutorial usando o motor JavaScript do sistema.
- `python3 src/test/browser/experience.py`: testes no Firefox headless, com o servidor isolado em `http://localhost:7001`. Usa um perfil temporário e a porta Marionette 2828; requer essa porta livre.

O tutorial inicia na página inicial e acompanha o percurso entre telas. Use Tutorial para rever as orientações da tela atual. Tema e andamento ficam salvos neste navegador.

### Organização do código

Consulte [a documentação de arquitetura](docs/architecture.md) para localizar as responsabilidades, configurar o tempo de avanço do quiz e executar os testes com dados isolados.

- `python3 src/test/resources/test_asset_links.py`: verifica os caminhos de estilos, scripts, imagens e imports.
