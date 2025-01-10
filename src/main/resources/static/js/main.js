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

    // Initialize sliders
    function initializeSlider(sliderSelector, prevButtonSelector, nextButtonSelector) {
        const slider = document.querySelector(sliderSelector);
        if (!slider) return;

        const prevButton = document.querySelector(prevButtonSelector);
        const nextButton = document.querySelector(nextButtonSelector);

        let isDown = false;
        let startX;
        let scrollLeft;
        let momentumID;
        let velocity = 0;
        const friction = 0.95;

        function updateButtonStates() {
            if (!slider || !prevButton || !nextButton) return;
            
            const scrollWidth = slider.scrollWidth;
            const clientWidth = slider.clientWidth;
            const scrollLeft = slider.scrollLeft;
            
            prevButton.disabled = scrollLeft <= 0;
            nextButton.disabled = scrollLeft + clientWidth >= scrollWidth;
        }

        // Mouse Events
        slider.addEventListener('mousedown', e => {
            isDown = true;
            slider.classList.add('dragging');
            startX = e.pageX - slider.offsetLeft;
            scrollLeft = slider.scrollLeft;
            cancelMomentumTracking();
        });

        slider.addEventListener('mouseleave', () => {
            isDown = false;
            slider.classList.remove('dragging');
        });

        slider.addEventListener('mouseup', () => {
            isDown = false;
            slider.classList.remove('dragging');
            beginMomentumTracking();
        });

        slider.addEventListener('mousemove', e => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - slider.offsetLeft;
            const walk = (x - startX) * 2;
            const prevScrollLeft = slider.scrollLeft;
            slider.scrollLeft = scrollLeft - walk;
            velocity = slider.scrollLeft - prevScrollLeft;
        });

        // Touch Events
        slider.addEventListener('touchstart', e => {
            isDown = true;
            slider.classList.add('dragging');
            startX = e.touches[0].pageX - slider.offsetLeft;
            scrollLeft = slider.scrollLeft;
            cancelMomentumTracking();
        });

        slider.addEventListener('touchend', () => {
            isDown = false;
            slider.classList.remove('dragging');
            beginMomentumTracking();
        });

        slider.addEventListener('touchmove', e => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.touches[0].pageX - slider.offsetLeft;
            const walk = (x - startX) * 2;
            const prevScrollLeft = slider.scrollLeft;
            slider.scrollLeft = scrollLeft - walk;
            velocity = slider.scrollLeft - prevScrollLeft;
        });

        // Navigation Buttons
        if (prevButton) {
            prevButton.addEventListener('click', () => {
                cancelMomentumTracking();
                const firstCard = slider.querySelector('.event-card, .category-card');
                const cardWidth = firstCard ? firstCard.offsetWidth : 0;
                const gap = 24; // 1.5rem gap
                slider.scrollBy({
                    left: -(cardWidth + gap),
                    behavior: 'smooth'
                });
                updateButtonStates();
            });
        }

        if (nextButton) {
            nextButton.addEventListener('click', () => {
                cancelMomentumTracking();
                const firstCard = slider.querySelector('.event-card, .category-card');
                const cardWidth = firstCard ? firstCard.offsetWidth : 0;
                const gap = 24; // 1.5rem gap
                slider.scrollBy({
                    left: cardWidth + gap,
                    behavior: 'smooth'
                });
                updateButtonStates();
            });
        }

        // Momentum Scrolling
        function beginMomentumTracking() {
            cancelMomentumTracking();
            momentumID = requestAnimationFrame(momentumLoop);
        }

        function cancelMomentumTracking() {
            cancelAnimationFrame(momentumID);
        }

        function momentumLoop() {
            slider.scrollLeft += velocity;
            velocity *= friction;
            if (Math.abs(velocity) > 0.1) {
                momentumID = requestAnimationFrame(momentumLoop);
            }
            updateButtonStates();
        }

        // Initial button state
        updateButtonStates();

        // Update button states on scroll
        slider.addEventListener('scroll', updateButtonStates);
        
        // Update button states on window resize
        window.addEventListener('resize', updateButtonStates);
    }

    // Initialize both sliders
    initializeSlider('.event-slider', '.event-prev', '.event-next');
    initializeSlider('.category-grid', '.category-prev', '.category-next');
});
