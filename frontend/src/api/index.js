import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000
})

export const categoriesAPI = {
  getAll: () => api.get('/categories'),
  getShowcase: () => api.get('/categories/showcase')
}

export const erasAPI = {
  getAll: () => api.get('/eras'),
  getTimeline: () => api.get('/eras/timeline')
}

export const postsAPI = {
  getList: (params) => api.get('/posts', { params }),
  getHot: () => api.get('/posts/hot'),
  getDetail: (id) => api.get(`/posts/${id}`),
  create: (formData) => api.post('/posts', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const commentsAPI = {
  getByPost: (postId) => api.get(`/comments/post/${postId}`),
  create: (data) => api.post('/comments', data)
}

export const favoritesAPI = {
  check: (postId, userSession) => api.get(`/favorites/check/${postId}`, { params: { userSession } }),
  toggle: (postId, userSession) => api.post(`/favorites/toggle/${postId}`, { userSession }),
  getUserFavorites: (userSession) => api.get('/favorites/user', { params: { userSession } })
}

export const timelineAPI = {
  getByPost: (postId) => api.get(`/timeline/post/${postId}`),
  create: (data) => api.post('/timeline', data)
}

export const archivesAPI = {
  getList: (params) => api.get('/archives', { params }),
  getGrouped: (params) => api.get('/archives/grouped', { params }),
  getStats: () => api.get('/archives/stats')
}

export default api
