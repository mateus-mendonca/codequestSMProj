export async function fetchQuestions(moduloId) {
    const response = await fetch(`/api/modulos/${moduloId}/quiz/questoes`);
    if (!response.ok) throw new Error(await response.text());
    return response.json();
}

export async function submitQuiz(moduloId, respostas, vidasRestantes) {
    const response = await fetch(`/api/modulos/${moduloId}/quiz/finalizar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ respostas, vidasRestantes, usuarioId: 'aluno_demo' })
    });
    return response.json();
}
