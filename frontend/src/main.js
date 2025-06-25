import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.mount('#app')

// ===== 커피 배경 이미지 슬라이드 =====
const coffeeImages = [
  'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80',
  'https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=1200&q=80',
  'https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=1200&q=80',
  'https://images.unsplash.com/photo-1432888498266-38ffec3eaf0a?auto=format&fit=crop&w=1200&q=80',
  'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=80'
];

function createBgSlides() {
  const body = document.body;
  // 기존 bg-slide 제거
  document.querySelectorAll('.bg-slide').forEach(el => el.remove());
  // 이미지 엘리먼트 생성
  coffeeImages.forEach((img, idx) => {
    const div = document.createElement('div');
    div.className = 'bg-slide';
    div.style.backgroundImage = `url('${img}')`;
    if (idx === 0) div.classList.add('active');
    body.prepend(div);
  });
  // 오버레이 추가
  if (!document.querySelector('.bg-overlay')) {
    const overlay = document.createElement('div');
    overlay.className = 'bg-overlay';
    body.prepend(overlay);
  }
}

function startBgSlider() {
  const slides = document.querySelectorAll('.bg-slide');
  let idx = 0;
  setInterval(() => {
    slides[idx].classList.remove('active');
    idx = (idx + 1) % slides.length;
    slides[idx].classList.add('active');
  }, 10000);
}

window.addEventListener('DOMContentLoaded', () => {
  createBgSlides();
  startBgSlider();
});
