import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'
import './styles/index.css'

const pinia = createPinia()
const app = createApp(App)
app.use(pinia)
app.use(vuetify)

async function bootstrap() {
  const { useUserStore } = await import('./stores/user')
  await useUserStore(pinia).restoreSession()
  app.use(router)
  app.mount('#app')
}

bootstrap()
