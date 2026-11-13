# Organização do CodeQuest

O projeto mantém Java 17, Javalin, JDBC e Thymeleaf. O navegador usa módulos JavaScript nativos, sem etapa de build de front-end.

## Back-end

- `App`: inicia o banco e o servidor e registra as rotas.
- `config/ApplicationServices`: cria as dependências compartilhadas pelos controladores.
- `config/TemplateConfig`: configura a resolução dos templates.
- `config/Database`: configura o pool e fornece conexões.
- `database/SchemaInitializer`: executa os comandos existentes de criação e atualização do esquema.
- `database/seed`: contém a carga inicial de cada mundo. Preserva as condições e os dados anteriores, incluindo a atualização do mundo 2 durante a inicialização.
- `routes/Routes`: agrupa rotas públicas, aprendizado e administração. Mantém as URLs existentes e permite injetar serviços para testes.
- `controller`: adapta requisições e respostas HTTP.
- `service`: contém validações e regras de progressão, seleção de questões e recompensas.
- `repository`: executa SQL e mapeia resultados JDBC.
- `model`: representa módulos, etapas, questões e progresso.
- `dto`: representa os dados destinados aos templates e à API. Os nomes dos campos JSON permanecem iguais.

## Front-end

Os templates contêm estrutura e expressões Thymeleaf. O identificador do módulo é passado ao quiz em `data-modulo-id`; arquivos estáticos não contêm expressões Thymeleaf.

`static/css/style.css` é o ponto de entrada dos estilos compartilhados. A ordem dos imports preserva a cascata anterior. `pages` contém os estilos das telas e `components` os componentes comuns. O quiz carrega também `pages/quiz.css`. `template-layouts.css`, carregado por último, contém as variantes de apresentação extraídas dos atributos `style`.

`static/js/experience.js` aplica o tema antes da renderização e registra seu controle. Esse pequeno bootstrap deve continuar no cabeçalho, sem `defer`, para evitar um clarão na troca de página.

`static/js/main.js` inicializa os módulos compartilhados:

- `ui`: navegação, efeitos de clique, formulários, confirmações e notificações;
- `storage.js`: acesso tolerante à indisponibilidade de armazenamento local;
- `tutorial/steps.js`: configuração do percurso;
- `tutorial/guide.js`: ciclo de vida e posicionamento do destaque e do mascote;
- `world-map.js`: posição do mascote na trilha.

`static/js/quiz/quiz.js` controla o estado da sessão. `api.js` acessa o servidor, `view.js` apresenta questões e feedback, `results.js` apresenta o resultado, `audio.js` reproduz sons e `auto-advance.js` controla os temporizadores. O tempo de avanço é configurado por `AUTO_ADVANCE_SECONDS` em `quiz.js`.

Scripts com imports são carregados com `type="module"`; seus símbolos ficam restritos ao módulo. Eventos de navegação e do quiz são registrados pelo JavaScript.

## Validação

```bash
mvn test
mvn package
gjs src/test/js/experience.gjs.js
gjs src/test/js/quiz-auto-advance.gjs.js
python3 src/test/resources/test_asset_links.py
```

Os testes JDBC de carga inicial usam conexões simuladas. Os testes no navegador usam dados em memória e não inicializam `Database`.

Para iniciar o servidor isolado, em um terminal:

```bash
mvn test-compile exec:java -Dexec.mainClass=com.codequest.browser.BrowserFixtureServer -Dexec.classpathScope=test
```

Em outro terminal:

```bash
python3 src/test/browser/experience.py
```

O servidor de testes usa a porta 7001, e o Firefox usa a porta Marionette 2828 com um perfil temporário. `CODEQUEST_TEST_URL` permite escolher outro endereço. Os testes do quiz gravam progresso apenas nas implementações de teste; utilize esse servidor isolado.

## Limites desta reorganização

Login e cadastro continuam demonstrativos. A identidade padrão, as regras de desbloqueio, o cálculo de XP, o conteúdo e o esquema do banco mantêm o comportamento anterior. Alterações nessas regras devem ser tratadas como funcionalidades e acompanhadas de testes específicos.
