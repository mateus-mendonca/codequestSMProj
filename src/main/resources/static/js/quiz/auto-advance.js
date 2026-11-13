/** Owns the countdown and cancels both timers together. */
export function createAutoAdvance(onAdvance, onTick, seconds) {
    let timeout = null;
    let interval = null;
    function cancel() {
        clearTimeout(timeout);
        clearInterval(interval);
        timeout = null;
        interval = null;
    }
    function start() {
        cancel();
        let remaining = seconds;
        onTick(remaining);
        interval = setInterval(() => onTick(remaining = Math.max(0, remaining - 1)), 1000);
        timeout = setTimeout(() => { cancel(); onAdvance(); }, seconds * 1000);
    }
    return { start, cancel };
}
