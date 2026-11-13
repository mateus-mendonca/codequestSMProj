import { playSound } from './audio.js';
export function renderQuestionView(q, currentIndex, totalQuestions, onSelect) {
    // Atualizar HUD
    document.getElementById('questionCounter').innerText = `Questão ${currentIndex + 1}/${totalQuestions}`;
    const fillPercent = ((currentIndex) / totalQuestions) * 100;
    document.getElementById('progressFill').style.width = fillPercent + '%';

    // Meta e Título
    document.getElementById('badgeCategory').innerText = q.categoria;
    document.getElementById('badgeLevel').innerText = q.nivel;
    document.getElementById('questionTitle').innerText = q.enunciado;

    // Limpar explicação e botão próximo
    const expBox = document.getElementById('explanationBox');
    expBox.className = 'explanation-box';
    expBox.style.display = 'none';

    const btnNext = document.getElementById('btnNext');
    btnNext.style.display = 'none';
    btnNext.innerText = "Próxima Questão \u2192";

    // Renderizar Opções
    const optionsList = document.getElementById('optionsList');
    optionsList.innerHTML = '';

    q.alternativas.forEach(op => {
        const btn = document.createElement('button');
        btn.className = 'option-btn';
        btn.dataset.key = op.key;
        btn.innerHTML = `<span class="option-key">${op.key}</span> <span>${op.text}</span>`;
        btn.onclick = () => onSelect(q.id, op.key, btn);
        optionsList.appendChild(btn);
    });
}

export function renderAnswerFeedback(q, isCorrect, btnElement, lives) {
    const buttons = document.querySelectorAll('.option-btn');
    buttons.forEach(b => b.disabled = true);

    const expBox = document.getElementById('explanationBox');
    const btnNext = document.getElementById('btnNext');

    if (isCorrect) {
        btnElement.classList.add('correct');
        playSound('correct');
        expBox.className = 'explanation-box show-correct';
        expBox.innerHTML = `
            <div class="feedback-flex">
                <img src="/img/Pulando.png" alt="Mascote comemorando" class="feedback-mascot">
                <div class="feedback-content">
                    <strong>\u2714 Resposta Correta! Excelente raciocínio!</strong><br>${q.explicacao || ''}
                </div>
            </div>
        `;
    } else {
        btnElement.classList.add('wrong');

        // Destacar em verde a alternativa correta
        buttons.forEach(b => {
            if (b.dataset.key === q.correta) {
                b.classList.add('correct');
            }
        });

        playSound('wrong');

        // Perder vida
        if (lives === 3) {
            const h3 = document.getElementById('heart3');
            h3.classList.add('lost');
            h3.src = '/img/sprites/icon-heart-empty.png';
        } else if (lives === 2) {
            const h2 = document.getElementById('heart2');
            h2.classList.add('lost');
            h2.src = '/img/sprites/icon-heart-empty.png';
        } else if (lives === 1) {
            const h1 = document.getElementById('heart1');
            h1.classList.add('lost');
            h1.src = '/img/sprites/icon-heart-empty.png';
        }
        lives--;

        expBox.className = 'explanation-box show-wrong';
        expBox.innerHTML = `
            <div class="feedback-flex">
                <img src="/img/Sentado.png" alt="Mascote estudando" class="feedback-mascot">
                <div class="feedback-content">
                    <strong>\u2716 Não foi dessa vez, mas continue firme!</strong> A alternativa correta era <strong>(${q.correta})</strong>.<br>${q.explicacao || ''}
                </div>
            </div>
        `;

        if (lives === 0) {
            btnNext.innerText = "Ver Resultado (Vidas Esgotadas) \u2192";
        }
    }

    expBox.style.display = 'block';
    btnNext.style.display = 'block';
    return lives;
}
