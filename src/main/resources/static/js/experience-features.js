import { startGuide } from './tutorial/guide.js';
import { initializeWorldMap } from './world-map.js';
const profile = new URLSearchParams(location.search).get('usuario') || 'visitante';
startGuide(profile);
initializeWorldMap(profile);
