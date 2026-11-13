import { playSound } from './audio.js';
function showResults(res) {
    const container = document.getElementById('resultsContainer');
    container.style.display = 'block';

    const titleEl = document.getElementById('resultsTitle');
    const subtitleEl = document.getElementById('resultsSubtitle');
    const cardModuloCompleted = document.getElementById('cardModuloCompleted');
    const btnNextModule = document.getElementById('btnNextModule');
    const mascotImg = document.getElementById('resultsMascotImg');

    if (res.passou) {
        playSound('win');
        if (mascotImg) mascotImg.src = "/img/Pulando.png";
        titleEl.innerText = "Quiz Concluído!";
        subtitleEl.innerText = `Parabéns! Você acertou ${res.pontuacao} de ${res.totalQuestoes} questões e concluiu o módulo!`;

        if (res.estrelas >= 1) document.getElementById('star1').classList.add('active');
        if (res.estrelas >= 2) document.getElementById('star2').classList.add('active');
        if (res.estrelas >= 3) document.getElementById('star3').classList.add('active');

        if (res.notaMaxima) {
            document.getElementById('cardPerfect').style.display = 'block';
        }
        if (res.semPerderVidas) {
            document.getElementById('cardNoLives').style.display = 'block';
        }
        cardModuloCompleted.style.display = 'block';
        btnNextModule.style.display = 'inline-flex';
    } else {
        playSound('wrong');
        if (mascotImg) mascotImg.src = "/img/Sentado.png";
        titleEl.innerText = "Que Pena! Tente Novamente";
        subtitleEl.innerText = `Você acertou ${res.pontuacao} de ${res.totalQuestoes} questões. Suas vidas acabaram ou você não atingiu a nota mínima.`;

        // Ocultar bônus e navegação do próximo módulo
        cardModuloCompleted.style.display = 'none';
        document.getElementById('cardPerfect').style.display = 'none';
        document.getElementById('cardNoLives').style.display = 'none';
        btnNextModule.style.display = 'none';
    }

    document.getElementById('totalXpDisplay').innerText = `Total: +${res.xpGanha} XP`;
}


export { showResults };
