import type { ApiErrorBody, LoginResponse } from '../types'

const DEFAULT_API_URL = 'http://localhost:8080'

export const API_BASE_URL: string = import.meta.env.VITE_API_URL?.trim() || DEFAULT_API_URL

/** Credentials used for the automatic sign-in of the demo. */
const DEMO_CREDENTIALS = { username: 'demo', password: 'demo123' }

export class ApiError extends Error {
  status: number
  code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST'
  body?: unknown
  headers?: Record<string, string>
  signal?: AbortSignal
}

/** Token lives in memory only: a page reload signs in again. */
let token: string | null = null
let loginInFlight: Promise<string> | null = null

function url(path: string): string {
  return `${API_BASE_URL}${path}`
}

async function rawFetch(path: string, options: RequestOptions, bearer: string | null): Promise<Response> {
  const headers: Record<string, string> = { ...options.headers }
  let body: string | undefined
  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
    body = JSON.stringify(options.body)
  }
  if (bearer) {
    headers['Authorization'] = `Bearer ${bearer}`
  }
  try {
    return await fetch(url(path), {
      method: options.method ?? 'GET',
      headers,
      body,
      signal: options.signal,
    })
  } catch (error) {
    if (options.signal?.aborted) {
      throw error
    }
    throw new ApiError(0, 'NETWORK_ERROR', `Cannot reach the backend at ${API_BASE_URL}`)
  }
}

async function readBody<T>(response: Response): Promise<T> {
  const text = await response.text()
  return (text.length > 0 ? JSON.parse(text) : undefined) as T
}

function defaultCode(status: number): string {
  switch (status) {
    case 400:
      return 'VALIDATION'
    case 401:
      return 'UNAUTHORIZED'
    case 409:
      return 'IDEMPOTENCY_CONFLICT'
    default:
      return `HTTP_${status}`
  }
}

async function toApiError(response: Response): Promise<ApiError> {
  const body = (await response.json().catch(() => null)) as Partial<ApiErrorBody> | null
  const message = body?.message ?? `${response.status} ${response.statusText || 'Request failed'}`
  return new ApiError(response.status, body?.error ?? defaultCode(response.status), message)
}

async function performLogin(): Promise<string> {
  const response = await rawFetch('/api/v1/auth/login', { method: 'POST', body: DEMO_CREDENTIALS }, null)
  if (!response.ok) {
    throw await toApiError(response)
  }
  const data = await readBody<LoginResponse>(response)
  token = data.token
  return data.token
}

/** Signs in with the demo credentials; concurrent callers share one request. */
export function login(): Promise<string> {
  loginInFlight ??= performLogin().finally(() => {
    loginInFlight = null
  })
  return loginInFlight
}

function ensureToken(): Promise<string> {
  return token ? Promise.resolve(token) : login()
}

/**
 * Sends an authenticated request. A 401 triggers a fresh sign-in and exactly one
 * replay of the same request, so an expired token never surfaces to the UI.
 */
async function send(path: string, options: RequestOptions = {}): Promise<Response> {
  const firstToken = await ensureToken()
  const response = await rawFetch(path, options, firstToken)
  if (response.status !== 401) {
    return response
  }
  token = null
  const refreshedToken = await login()
  return rawFetch(path, options, refreshedToken)
}

async function request(path: string, options: RequestOptions = {}): Promise<Response> {
  const response = await send(path, options)
  if (!response.ok) {
    throw await toApiError(response)
  }
  return response
}

export async function getJson<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await request(path, options)
  return readBody<T>(response)
}

export async function postJson<T>(
  path: string,
  body: unknown,
  options: RequestOptions = {},
): Promise<{ data: T; headers: Headers }> {
  const response = await request(path, { method: 'POST', body, ...options })
  return { data: await readBody<T>(response), headers: response.headers }
}