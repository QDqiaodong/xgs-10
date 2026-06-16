import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'
import './icons/categories/category-theme.css'
import CategoryIcon from './components/CategoryIcon.vue'

const app = createApp(App)
const pinia = createPinia()

app.component('CategoryIcon', CategoryIcon)
app.use(pinia)
app.use(router)
app.mount('#app')
