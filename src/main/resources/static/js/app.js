(function () {
    const timer = document.getElementById("timer");
    const form = document.getElementById("quizForm");
    if (timer && form) {
        let remaining = Number(timer.dataset.seconds || 0);
        const paint = () => {
            const minutes = String(Math.floor(remaining / 60)).padStart(2, "0");
            const seconds = String(remaining % 60).padStart(2, "0");
            timer.textContent = `${minutes}:${seconds}`;
            timer.classList.toggle("danger", remaining <= 30);
        };
        paint();
        const tick = window.setInterval(() => {
            remaining -= 1;
            paint();
            if (remaining <= 0) {
                window.clearInterval(tick);
                form.submit();
            }
        }, 1000);
    }

    const chart = document.getElementById("resultChart");
    if (chart) {
        const correct = Number(chart.dataset.correct || 0);
        const wrong = Number(chart.dataset.wrong || 0);
        const total = Math.max(1, correct + wrong);
        const ctx = chart.getContext("2d");
        const centerX = chart.width / 2;
        const centerY = 74;
        const radius = 56;
        let start = -Math.PI / 2;

        [
            {value: correct, color: "#1f9d74"},
            {value: wrong, color: "#d64545"}
        ].forEach(slice => {
            const angle = (slice.value / total) * Math.PI * 2;
            ctx.beginPath();
            ctx.moveTo(centerX, centerY);
            ctx.arc(centerX, centerY, radius, start, start + angle);
            ctx.closePath();
            ctx.fillStyle = slice.color;
            ctx.fill();
            start += angle;
        });

        ctx.fillStyle = "#ffffff";
        ctx.beginPath();
        ctx.arc(centerX, centerY, 28, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = "#14213d";
        ctx.font = "700 18px system-ui";
        ctx.textAlign = "center";
        ctx.fillText(`${Math.round((correct / total) * 100)}%`, centerX, centerY + 6);
    }
})();
