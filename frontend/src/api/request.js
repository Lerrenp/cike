import axios from 'axios'
import { toast } from '@/utils/toast'

// 统一 axios 实例
const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动附加 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('cike_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务码
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && res.code !== undefined && res.code !== 200) {
      if (res.code === 401 && !response.config.skipAuthRedirect) {
        handleUnauthorized()
      }
      if (!response.config.skipAuthRedirect) {
        toast.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      handleUnauthorized()
    } else if (status === 403) {
      toast.error('没有权限执行该操作')
    } else if (status === 404) {
      toast.error('请求的资源不存在')
    } else if (status >= 500) {
      toast.error('服务器开小差了，请稍后重试')
    } else if (error.code === 'ECONNABORTED') {
      toast.error('请求超时，请稍后重试')
    } else if (!error.response) {
      toast.error('网络连接失败，请检查后端服务是否启动')
    }
    return Promise.reject(error)
  }
)
function handleUnauthorized() {
  localStorage.removeItem('cike_token')
  localStorage.removeItem('cike_user')
  if (window.location.pathname === '/login') return

  toast.warning('登录已失效，请重新登录')
  const redirect = `${window.location.pathname}${window.location.search}${window.location.hash}`
  window.location.replace(`/login?redirect=${encodeURIComponent(redirect)}`)
}

export default request
