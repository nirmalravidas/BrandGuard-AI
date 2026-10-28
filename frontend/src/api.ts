const API_BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/$/, '')

export type Brand = { id: string; name: string; description: string; preferredTone: string; fictional: boolean }
export type Product = { id: string; brandId: string; name: string; description: string; price: string }
export type Rule = { id: string; kind: string; value: string; explanation: string; severity: string; enabled: boolean }
export type Finding = {
  id: string; category: string; severity: string; source: string; excerpt: string
  explanation: string; expectedValue?: string; evidenceSourceId?: string
  evidenceReference?: string; suggestion?: string; needsVerification: boolean
}
export type Revision = { id: string; content: string; action: string; createdAt: string }
export type Review = { id: string; action: string; reviewer: string; comment?: string; createdAt: string }
export type Submission = {
  id: string; brandId: string; title: string; contentType: string; originalContent: string
  currentContent: string; status: string; analysisSummary?: string; createdAt: string; updatedAt: string
  findings: Finding[]; revisions: Revision[]; reviews: Review[]; analysisMode: string; correctedContent?: string
}
export type Dashboard = { submissions: number; pendingReviews: number; approved: number; recent: Submission[] }
export type Analysis = { summary: string; mode: string; correctedContent: string; findings: Finding[] }
export type AppStatus = { aiConfigured: boolean; analysisMode: string }

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...init,
      headers: { 'Content-Type': 'application/json', ...init?.headers },
    })
  } catch {
    throw new Error('Could not connect to BrandGuard API. Check that the backend is running.')
  }
  if (!response.ok) {
    let message = `Request failed (${response.status})`
    try {
      const body = await response.json() as { message?: string }
      if (body.message) message = body.message
    } catch { /* Response may be empty. */ }
    throw new Error(message)
  }
  return response.json() as Promise<T>
}

export const json = (body: unknown): RequestInit => ({ method: 'POST', body: JSON.stringify(body) })
