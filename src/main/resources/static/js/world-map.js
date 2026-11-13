import { read, save } from './storage.js';
export function initializeWorldMap(profile) {
        const track = document.querySelector('[data-world]');
        if (track) {
            const node = track.querySelector('.phase-node.current') || track.querySelector('.boss-node.unlocked');
            if (node) {
                const mascot = document.createElement('img');
                mascot.src = '/img/Correndo.png';
                mascot.alt = 'Você está aqui: continue sua jornada';
                mascot.className = 'cq-trail-mascot';
                node.prepend(mascot);
                const key = `cq-position:${profile}:${track.dataset.world}`;
                if (Number(read(key)) < Number(track.dataset.position)) mascot.classList.add('cq-advance');
                save(key, track.dataset.position);
            }
        }
}
