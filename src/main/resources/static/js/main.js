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

    // Category Slider
    const categoryGrid = document.querySelector('.category-grid');
    if (!categoryGrid) return;

    const prevButton = categoryGrid.parentElement.querySelector('.nav-button:first-child');
    const nextButton = categoryGrid.parentElement.querySelector('.nav-button:last-child');

    let isDown = false;
    let startX;
    let scrollLeft;
    let momentumID;
    let velocity = 0;
    const friction = 0.95;

    // Mouse Events
    categoryGrid.addEventListener('mousedown', e => {
        isDown = true;
        categoryGrid.classList.add('dragging');
        startX = e.pageX - categoryGrid.offsetLeft;
        scrollLeft = categoryGrid.scrollLeft;
        cancelMomentumTracking();
    });

    categoryGrid.addEventListener('mouseleave', () => {
        isDown = false;
        categoryGrid.classList.remove('dragging');
    });

    categoryGrid.addEventListener('mouseup', () => {
        isDown = false;
        categoryGrid.classList.remove('dragging');
        beginMomentumTracking();
    });

    categoryGrid.addEventListener('mousemove', e => {
        if (!isDown) return;
        e.preventDefault();
        const x = e.pageX - categoryGrid.offsetLeft;
        const walk = (x - startX) * 2;
        const prevScrollLeft = categoryGrid.scrollLeft;
        categoryGrid.scrollLeft = scrollLeft - walk;
        velocity = categoryGrid.scrollLeft - prevScrollLeft;
    });

    // Touch Events
    categoryGrid.addEventListener('touchstart', e => {
        isDown = true;
        categoryGrid.classList.add('dragging');
        startX = e.touches[0].pageX - categoryGrid.offsetLeft;
        scrollLeft = categoryGrid.scrollLeft;
        cancelMomentumTracking();
    });

    categoryGrid.addEventListener('touchend', () => {
        isDown = false;
        categoryGrid.classList.remove('dragging');
        beginMomentumTracking();
    });

    categoryGrid.addEventListener('touchmove', e => {
        if (!isDown) return;
        e.preventDefault();
        const x = e.touches[0].pageX - categoryGrid.offsetLeft;
        const walk = (x - startX) * 2;
        const prevScrollLeft = categoryGrid.scrollLeft;
        categoryGrid.scrollLeft = scrollLeft - walk;
        velocity = categoryGrid.scrollLeft - prevScrollLeft;
    });

    // Navigation Buttons
    function updateButtonStates() {
        if (prevButton && nextButton) {
            prevButton.disabled = categoryGrid.scrollLeft <= 0;
            nextButton.disabled = categoryGrid.scrollLeft >= categoryGrid.scrollWidth - categoryGrid.clientWidth;
        }
    }

    if (prevButton) {
        prevButton.addEventListener('click', () => {
            cancelMomentumTracking();
            categoryGrid.scrollBy({
                left: -categoryGrid.offsetWidth,
                behavior: 'smooth'
            });
        });
    }

    if (nextButton) {
        nextButton.addEventListener('click', () => {
            cancelMomentumTracking();
            categoryGrid.scrollBy({
                left: categoryGrid.offsetWidth,
                behavior: 'smooth'
            });
        });
    }

    categoryGrid.addEventListener('scroll', () => {
        updateButtonStates();
    });

    // Momentum Scrolling
    function beginMomentumTracking() {
        cancelMomentumTracking();
        momentumID = requestAnimationFrame(momentumLoop);
    }

    function cancelMomentumTracking() {
        cancelAnimationFrame(momentumID);
    }

    function momentumLoop() {
        categoryGrid.scrollLeft += velocity;
        velocity *= friction;
        if (Math.abs(velocity) > 0.1) {
            momentumID = requestAnimationFrame(momentumLoop);
        }
    }

    // Initial button state
    updateButtonStates();
});
