import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.mount('#app')

// ===== 세련된 그라데이션 배경 =====
function createBgGradient() {
  const body = document.body;
  // 기존 bg-slide 제거
  document.querySelectorAll('.bg-slide').forEach(el => el.remove());
  
  // 세련된 그라데이션 오버레이 추가
  if (!document.querySelector('.bg-overlay')) {
    const overlay = document.createElement('div');
    overlay.className = 'bg-overlay';
    body.prepend(overlay);
  }
}

window.addEventListener('DOMContentLoaded', () => {
  createBgGradient();
});
