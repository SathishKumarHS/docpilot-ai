const ACCESS_TOKEN_KEY = "docpilot-access-token"
const REFRESH_TOKEN_KEY = "docpilot-refresh-token"
const USER_KEY = "docpilot-user"
const ANON_TOKEN_KEY = "docpilot-anon-token"

let refreshPromise: Promise<boolean> | null = null

interface StoredUser {
  userId: string
  email: string
  role: string
}

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function getUser(): StoredUser | null {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function isAuthenticated(): boolean {
  return !!getAccessToken()
}

export function getAnonymousToken(): string | null {
  return sessionStorage.getItem(ANON_TOKEN_KEY)
}

export function saveAuth(accessToken: string, refreshToken: string, userId: string, email: string, role: string) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  localStorage.setItem(USER_KEY, JSON.stringify({ userId, email, role }))
}

export function clearAuth() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(ANON_TOKEN_KEY)
}

export async function startAnonymousSession(): Promise<string> {
  const res = await fetch("/api/v1/auth/anonymous-session", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  })
  if (!res.ok) throw new Error("Failed to create anonymous session")
  const data = await res.json()
  sessionStorage.setItem(ANON_TOKEN_KEY, data.token)
  return data.token
}

async function tryRefresh(): Promise<boolean> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return false
  try {
    const res = await fetch("/api/v1/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    })
    if (!res.ok) return false
    const data = await res.json()
    saveAuth(data.accessToken, data.refreshToken, data.userId, data.email, data.role)
    return true
  } catch {
    return false
  }
}

export async function apiFetch(path: string, options: RequestInit = {}): Promise<Response> {
  const headers: Record<string, string> = {
    ...(options.headers as Record<string, string>),
  }

  const token = getAccessToken()
  if (token) {
    headers["Authorization"] = `Bearer ${token}`
  }

  const anonToken = getAnonymousToken()
  if (anonToken) {
    headers["X-Anonymous-Token"] = anonToken
  }

  if (options.body && !(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json"
  }

  const res = await fetch(path, { ...options, headers })

  if (res.status === 401 && token) {
    if (!refreshPromise) {
      refreshPromise = tryRefresh().finally(() => { refreshPromise = null })
    }
    const refreshed = await refreshPromise
    if (refreshed) {
      headers["Authorization"] = `Bearer ${getAccessToken()}`
      return fetch(path, { ...options, headers })
    }
    clearAuth()
  }

  return res
}
