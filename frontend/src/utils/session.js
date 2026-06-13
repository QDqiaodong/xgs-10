export function getSessionId() {
  let sessionId = localStorage.getItem('nostalgia_session')
  if (!sessionId) {
    sessionId = 'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    localStorage.setItem('nostalgia_session', sessionId)
  }
  return sessionId
}
