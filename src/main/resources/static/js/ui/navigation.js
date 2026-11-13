export function initializeNavigation() {
const navToggle = document.getElementById('navToggle');
if (navToggle) navToggle.addEventListener('click', () => document.querySelector('.breadcrumb').classList.toggle('open'));
}
