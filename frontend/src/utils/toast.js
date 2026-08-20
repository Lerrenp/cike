// 轻量 MD3 风格 Toast 通知（不依赖 UI 框架，纯 DOM 实现）
// 用法: import { toast } from '@/utils/toast'; toast('保存成功') / toast.error('出错啦')
import './toast.css'

let seed = 0

function show(message, type = 'info', duration = 2400) {
  const id = 'toast-' + seed++
  const host = document.getElementById('toast-host') || createHost()
  const el = document.createElement('div')
  el.className = `m3-toast ${type}`
  el.id = id
  // MD3 语义色
  const iconMap = {
    success: 'mdi-check-circle',
    error: 'mdi-alert-circle',
    warning: 'mdi-alert',
    info: 'mdi-information'
  }
  el.innerHTML = `<i class="mdi ${iconMap[type] || iconMap.info}"></i><span></span>`
  el.querySelector('span').textContent = message
  host.appendChild(el)
  requestAnimationFrame(() => el.classList.add('show'))
  setTimeout(() => {
    el.classList.remove('show')
    setTimeout(() => el.remove(), 220)
  }, duration)
}

function createHost() {
  const host = document.createElement('div')
  host.id = 'toast-host'
  document.body.appendChild(host)
  return host
}

export const toast = Object.assign(show, {
  success: (m) => show(m, 'success', 2000),
  error: (m) => show(m, 'error', 3000),
  warning: (m) => show(m, 'warning', 3000),
  info: (m) => show(m, 'info', 2200)
})

export default toast
