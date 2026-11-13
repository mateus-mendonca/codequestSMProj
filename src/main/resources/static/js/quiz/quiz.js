import { createAutoAdvance } from './auto-advance.js';
import { renderQuestionView, renderAnswerFeedback } from './view.js';
import { fetchQuestions, submitQuiz } from './api.js';
import { showResults } from './results.js';
const AUTO_ADVANCE_SECONDS = 5;

const moduloId = Number(document.querySelector('[data-modulo-id]').dataset.moduloId);
let questions = [];
let currentIndex = 0;
let lives = 3;
let userAnswers = {}; // qId -> key (A, B, C ou D)
let isAnswered = false;
let isFinishing = false;
const autoAdvance = createAutoAdvance(nextQuestion, secondsRemaining => {
    const action = currentIndex === questions.length - 1 ? 'Ver resultado' : 'Próxima questão';
    document.getElementById('btnNext').innerText = `${action} em ${secondsRemaining} ${secondsRemaining === 1 ? 'segundo' : 'segundos'}… (clique para antecipar)`;
}, AUTO_ADVANCE_SECONDS);
const cancelAutoAdvance = autoAdvance.cancel;
const scheduleAutoAdvance = autoAdvance.start;

window.addEventListener('pagehide', () => {
    cancelAutoAdvance();
    document.getElementById('btnNext').innerText = 'Continuar →';
});

async function loadQuestions() {
    try {
        questions = await fetchQuestions(moduloId);
        if (questions && questions.length > 0) {
            renderQuestion();
        } else {
            document.getElementById('questionTitle').innerText = "Nenhuma questão encontrada para este módulo.";
        }
    } catch (e) {
        console.error("Erro ao carregar questões:", e);
        document.getElementById('questionTitle').innerText = "Erro ao carregar as questões. Por favor, recarregue a página.";
    }
}

function renderQuestion() {
    cancelAutoAdvance();
    isAnswered = false;
    const q = questions[currentIndex];

    renderQuestionView(q, currentIndex, questions.length, selectOption);
}

function selectOption(qId, selectedKey, btnElement) {
    if (isAnswered) return;
    isAnswered = true;

    userAnswers[qId] = selectedKey;

    const q = questions[currentIndex];
    const isCorrect = (selectedKey === q.correta);

    lives = renderAnswerFeedback(q, isCorrect, btnElement, lives);
    if (isCorrect) scheduleAutoAdvance();
}

function nextQuestion() {
    if (!isAnswered || isFinishing) return;
    cancelAutoAdvance();
    isAnswered = false;
    currentIndex++;
    if (currentIndex < questions.length && lives > 0) {
        renderQuestion();
    } else {
        finishQuiz();
    }
}

async function finishQuiz() {
    if (isFinishing) return;
    isFinishing = true;
    cancelAutoAdvance();
    document.getElementById('questionCard').style.display = 'none';
    document.getElementById('hudBar').style.display = 'none';

    try {
        const result = await submitQuiz(moduloId, userAnswers, lives);
        showResults(result);
    } catch (e) {
        console.error("Erro ao finalizar quiz:", e);
    }
}

document.getElementById('btnNext').addEventListener('click', nextQuestion);
document.getElementById('btnRestart').addEventListener('click', () => window.location.reload());
loadQuestions();
