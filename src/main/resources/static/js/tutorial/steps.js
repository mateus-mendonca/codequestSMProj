export const pages = [
    { match: /^\/$/, steps: [
        ['.hero-title', 'Bem-vindo ao CodeQuest', 'Eu vou acompanhar você! Aqui você aprende programação explorando mundos e resolvendo desafios.'],
        ['.cq-theme', 'Escolha seu tema', 'Este botão alterna entre o modo claro e o escuro. Sua escolha fica salva neste navegador.'],
        ['.hero-actions a[href="/modulos"]', 'Vamos explorar', 'Clique em Explorar Mundos para conhecer sua trilha.', true]
    ] },
    { match: /^\/(login|cadastro)$/, steps: [
        ['.auth-card', 'Acesso à plataforma', 'Esta é a área de acesso. Você também pode explorar a plataforma sem conta.'],
        ['.continue', 'Explore os mundos', 'Clique aqui para seguir para a trilha de aprendizado.', true]
    ] },
    { match: /^\/modulos$/, steps: [
        ['.modulos-header', 'Seus mundos', 'Cada mundo reúne fases de estudo e um desafio final. Conquiste o anterior para liberar o seguinte.'],
        ['.module-card:not(.locked)', 'Entre na aventura', 'Clique neste mundo disponível para abrir o mapa de fases.', true]
    ] },
    { match: /^\/modulos\/\d+$/, steps: [
        ['.phase-map-header', 'Seu mapa de fases', 'O mapa mostra as fases disponíveis, concluídas e bloqueadas. O mascote indica onde continuar.'],
        ['.phase-node.current, .boss-node.unlocked, .phase-node.completed', 'Siga a trilha', 'Clique na fase disponível para estudar. Se o desafio final já estiver liberado, você pode acessá-lo.', true]
    ] },
    { match: /^\/modulos\/\d+\/etapas\/\d+$/, steps: [
        ['.etapa-header', 'Uma fase de cada vez', 'Aqui você encontra o tema e a posição desta fase dentro do mundo.'],
        ['.explanation-text', 'Leia e pratique', 'Leia a explicação e consulte o exemplo abaixo para entender o conteúdo.'],
        ['.nav-buttons .btn-primary', 'Continue sua jornada', 'Quando terminar a leitura, clique aqui para seguir para a próxima fase ou o desafio final.', true]
    ] },
    { match: /^\/modulos\/\d+\/quiz$/, steps: [
        ['.hud-bar', 'Acompanhe o desafio', 'Aqui estão suas vidas e o andamento das perguntas.'],
        ['.question-card', 'Hora de praticar', 'Escolha uma alternativa, leia o feedback e continue. Ao final, confira seu resultado e as recompensas.']
    ] }
];
