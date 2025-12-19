const API_URL = 'http://localhost:8080/api/clicks';
let totalClicks = 0;

const girls = [
    { req: 0,   src: 'girl1.png', top: 15 },
    {req: 25, src: 'girl2.jpg', top: 0},
    { req: 50,  src: 'girl2.png', top: 5 },
    { req: 75,  src: 'girl3.png', top: 45 },
    { req: 100, src: 'girl4.jpg', top: 10 },
    { req: 125, src: 'girl5.png', top: 70 },
    { req: 150, src: 'girl6.jpg', top: 10 }
];

async function fetchClicks() {
    try {
        const response = await fetch(API_URL);
        totalClicks = await response.json();
        updateUI();
    } catch (e) { console.error("Ошибка загрузки", e); }
}

function updateUI() {
    document.getElementById('click-count').innerText = totalClicks;

    const goal = 25;
    const level = Math.floor(totalClicks / goal) + 1;
    const progress = totalClicks % goal;

    const isMaxLevel = level > girls.length;

    if (isMaxLevel) {
        document.getElementById('level-val').innerText = "MAX";
        document.getElementById('clicks-left').innerText = "0";
        document.getElementById('progress-bar').style.width = "100%";
        document.querySelector('.happiness-text').innerText = "ТЫ СУПЕР! ВСЕ ТЯНКИ СОБРАНЫ! ✨";
    } else {
        document.getElementById('level-val').innerText = level;
        document.getElementById('clicks-left').innerText = goal - progress;
        document.getElementById('progress-bar').style.width = (progress / goal * 100) + "%";
    }

    const girlIdx = Math.min(level - 1, girls.length - 1);
    const currentGirl = girls[girlIdx];
    const animeImgElement = document.getElementById('anime-img');

    if (animeImgElement) {
        animeImgElement.src = currentGirl.src;
        animeImgElement.style.objectPosition = `50% ${currentGirl.top}%`;
    }

    const unlocked = girls.filter(g => totalClicks >= g.req).length;
    document.getElementById('collection-count').innerText = `Разблокировано: ${unlocked}/${girls.length}`;
}

async function makeClick(event) {
    spawnHeart(event.clientX, event.clientY);
    try {
        const response = await fetch(`${API_URL}/increment`, { method: 'POST' });
        totalClicks = await response.json();
        updateUI();
    } catch (e) { console.error("Ошибка клика", e); }
}

function spawnHeart(x, y) {
    const heart = document.createElement('div');
    heart.className = 'click-heart';
    heart.innerText = '💖';
    heart.style.left = x + 'px';
    heart.style.top = y + 'px';
    document.body.appendChild(heart);
    setTimeout(() => heart.remove(), 800);
}

function openCollection() {
    const gallery = document.getElementById('gallery');
    gallery.innerHTML = '';
    girls.forEach(g => {
        const img = document.createElement('img');
        img.src = g.src;
        img.style.objectPosition = `50% ${g.top}%`;
        img.className = 'gallery-item' + (totalClicks >= g.req ? ' unlocked' : '');
        gallery.appendChild(img);
    });
    document.getElementById('collection-modal').style.display = 'block';
}

function closeCollection() {
    document.getElementById('collection-modal').style.display = 'none';
}

fetchClicks();