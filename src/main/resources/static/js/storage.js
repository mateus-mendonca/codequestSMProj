export const read = key => { try { return localStorage.getItem(key); } catch (_) { return null; } };
export const save = (key, value) => { try { localStorage.setItem(key, value); } catch (_) { /* Storage is optional. */ } };
