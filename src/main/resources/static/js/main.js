document.addEventListener('DOMContentLoaded', function() {
    // Apply staggered animation to stat cards
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach((card, index) => {
        card.style.setProperty('--delay', `${index * 0.2}s`);
    });

    // Apply staggered animation to event cards
    const eventCards = document.querySelectorAll('.event-card');
    eventCards.forEach((card, index) => {
        card.style.setProperty('--delay', `${index * 0.2}s`);
    });

    // Search functionality
    const searchBox = document.querySelector('.search-box');
    if (searchBox) {
        searchBox.addEventListener('input', function(e) {
            // Add search logic here
            console.log('Searching for:', e.target.value);
        });
    }

    // Animate numbers in stats
    function animateValue(element, start, end, duration) {
        let startTimestamp = null;
        const step = (timestamp) => {
            if (!startTimestamp) startTimestamp = timestamp;
            const progress = Math.min((timestamp - startTimestamp) / duration, 1);
            const current = Math.floor(progress * (end - start) + start);
            element.textContent = current.toLocaleString();
            if (progress < 1) {
                window.requestAnimationFrame(step);
            }
        };
        window.requestAnimationFrame(step);
    }

    // Animate stats when they come into view
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const statValue = entry.target.querySelector('.stat-value');
                if (statValue && !statValue.dataset.animated) {
                    const endValue = parseInt(statValue.dataset.value);
                    animateValue(statValue, 0, endValue, 2000);
                    statValue.dataset.animated = 'true';
                }
            }
        });
    });

    document.querySelectorAll('.stat-card').forEach(stat => {
        observer.observe(stat);
    });
});
