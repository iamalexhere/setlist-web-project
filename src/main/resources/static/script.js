document.addEventListener('DOMContentLoaded', () => {
    // Intersection Observer for Animations
    const elements = document.querySelectorAll('main > section');
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.style.animationPlayState = 'running';
          observer.unobserve(entry.target)
        }
      });
    }, { threshold: 0.3 });

    elements.forEach(el => {
      el.style.animationPlayState = 'paused';
      observer.observe(el);
    })

    // Search Functionality
    document.getElementById('search-button').addEventListener('click', () => {
        const searchTerm = document.getElementById('search-input').value;
        if (searchTerm) {
            alert(`You searched for: ${searchTerm}`);
        } else {
            alert("Please input a value");
        }
    });


   // Artist Carousel
    const carousel = document.querySelector('.artist-carousel');
    const prevButton = document.querySelector('.carousel-button.prev');
    const nextButton = document.querySelector('.carousel-button.next');
    const cardWidth = document.querySelector('.artist-card').offsetWidth + 20; // Card width + margin
    let currentPosition = 0;


    prevButton.addEventListener('click', () => {
        currentPosition = Math.max(currentPosition - cardWidth, 0);
        carousel.style.transform = `translateX(-${currentPosition}px)`;
    });

    nextButton.addEventListener('click', () => {
        const maxPosition = (carousel.children.length * cardWidth) - carousel.offsetWidth;
        currentPosition = Math.min(currentPosition + cardWidth, maxPosition);
        carousel.style.transform = `translateX(-${currentPosition}px)`;
    });
});