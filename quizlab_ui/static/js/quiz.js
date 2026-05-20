// QuizLab — countdown timer for quiz taking page
(function () {
  const el = document.getElementById('quiz-timer');
  const form = document.getElementById('quiz-form');
  if (!el || !form) return;

  let remaining = parseInt(el.dataset.seconds, 10) || 0;
  const display = el.querySelector('.timer-text');

  function fmt(s) {
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
  }
  function render() {
    if (display) display.textContent = fmt(Math.max(0, remaining));
    el.classList.toggle('warn', remaining <= 60 && remaining > 20);
    el.classList.toggle('danger', remaining <= 20);
  }
  render();

  const intv = setInterval(() => {
    remaining -= 1;
    render();
    if (remaining <= 0) {
      clearInterval(intv);
      // Auto-submit when time runs out
      form.submit();
    }
  }, 1000);

  // Warn on accidental navigation
  window.addEventListener('beforeunload', function (e) {
    if (remaining > 0 && !form.dataset.submitting) {
      e.preventDefault();
      e.returnValue = '';
    }
  });
  form.addEventListener('submit', () => { form.dataset.submitting = '1'; });
})();
