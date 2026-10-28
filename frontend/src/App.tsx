import { useEffect, useState } from 'react'
import { Link, NavLink, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import {
  Activity, ArrowDownLeft, ArrowLeft, ArrowRight, BadgeCheck, BookOpen, Check, ChevronRight,
  CircleAlert, ClipboardCheck, FileClock, FilePlus2, LayoutDashboard, LoaderCircle, Menu,
  Package, SearchCheck, ShieldCheck, Sparkles, X,
} from 'lucide-react'
import { api, type Analysis, type AppStatus, type Brand, type Dashboard, type Finding, type Product, type Rule, type Submission } from './api'

const brandId = 'novafit'
const date = (value: string) => new Date(value).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
const titleCase = (value: string) => value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase())

function App() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [aiConfigured, setAiConfigured] = useState<boolean>()
  useEffect(() => { api<AppStatus>('/status').then((status) => setAiConfigured(status.aiConfigured)).catch(() => setAiConfigured(undefined)) }, [])
  const nav = [
    { to: '/', label: 'Overview', icon: LayoutDashboard, end: true },
    { to: '/brand', label: 'Brand knowledge', icon: BookOpen },
    { to: '/new', label: 'New content review', icon: FilePlus2 },
    { to: '/submissions', label: 'Submission history', icon: FileClock },
    { to: '/approvals', label: 'Approval queue', icon: ClipboardCheck },
  ]
  return <div className="app-shell">
    <aside className={`sidebar ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <div className="brand-lockup"><div className="brand-mark"><ShieldCheck size={21} /></div><div><strong>BrandGuard</strong><small>AI CONTENT GOVERNANCE</small></div></div>
      <div className="workspace-label">WORKSPACE</div>
      <div className="workspace-switch"><span className="workspace-dot">N</span><span><b>NovaFit</b><small>Demo workspace</small></span><ChevronRight size={16} className="muted-icon" /></div>
      <nav className="main-nav" aria-label="Main navigation">
        {nav.map(({ to, label, icon: Icon, end }) => <NavLink key={to} to={to} end={end} onClick={() => setSidebarOpen(false)} className={({ isActive }) => `nav-link ${isActive ? 'nav-active' : ''}`}><Icon size={18} /><span>{label}</span>{label === 'Approval queue' && <span className="nav-indicator" />}</NavLink>)}
      </nav>
      <div className="sidebar-bottom"><div className="ai-status"><span className={`status-dot ${aiConfigured ? 'status-dot-ai' : ''}`} /><span><b>{aiConfigured === undefined ? 'Analysis mode unknown' : aiConfigured ? 'AI provider configured' : 'Rules-only mode'}</b><small>{aiConfigured === undefined ? 'Provider status unavailable' : aiConfigured ? 'AI is backend-only' : 'AI key not configured'}</small></span></div><div className="profile"><div className="avatar">DR</div><div><b>Demo Reviewer</b><small>Workspace reviewer</small></div><Menu size={17} className="muted-icon" /></div></div>
    </aside>
    {sidebarOpen && <button className="mobile-backdrop" aria-label="Close navigation" onClick={() => setSidebarOpen(false)} />}
    <main className="main-panel">
      <header className="topbar"><button className="mobile-menu icon-button" onClick={() => setSidebarOpen(true)} aria-label="Open navigation"><Menu size={19} /></button><div className="breadcrumb">Brand governance <ChevronRight size={14} /> <span>Workspace</span></div><div className="topbar-right"><span className="environment-pill"><span /> DEMO ENVIRONMENT</span><div className="avatar">DR</div></div></header>
      <div className="page-wrap"><Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/brand" element={<BrandPage />} />
        <Route path="/new" element={<NewSubmissionPage />} />
        <Route path="/submissions" element={<SubmissionsPage />} />
        <Route path="/submissions/:id" element={<SubmissionPage />} />
        <Route path="/approvals" element={<ApprovalsPage />} />
        <Route path="*" element={<NotFound />} />
      </Routes></div>
    </main>
  </div>
}

function PageTitle({ eyebrow, title, description, action }: { eyebrow?: string; title: string; description?: string; action?: React.ReactNode }) {
  return <div className="page-heading"><div>{eyebrow && <div className="eyebrow">{eyebrow}</div>}<h1>{title}</h1>{description && <p>{description}</p>}</div>{action}</div>
}

function Notice({ error, success }: { error?: string; success?: string }) {
  if (!error && !success) return null
  return <div className={`notice ${error ? 'notice-error' : 'notice-success'}`} role={error ? 'alert' : 'status'}>{error ? <CircleAlert size={18} /> : <Check size={18} />}{error || success}</div>
}

function Loading() { return <div className="loading"><LoaderCircle className="spin" size={22} /> Loading workspace data…</div> }

function DashboardPage() {
  const [data, setData] = useState<Dashboard>()
  const [error, setError] = useState('')
  useEffect(() => { api<Dashboard>('/dashboard').then(setData).catch((e: Error) => setError(e.message)) }, [])
  if (error) return <><PageTitle eyebrow="WORKSPACE OVERVIEW" title="Good to see you" description="Your brand review activity at a glance." /><Notice error={error} /></>
  if (!data) return <Loading />
  const metrics = [
    { title: 'Total submissions', value: data.submissions, icon: FileClock, color: 'blue' },
    { title: 'Pending reviews', value: data.pendingReviews, icon: Activity, color: 'amber' },
    { title: 'Approved content', value: data.approved, icon: BadgeCheck, color: 'green' },
  ]
  return <>
    <PageTitle eyebrow={new Date().toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' }).toUpperCase()} title="Good to see you, reviewer" description="A clear view of your brand content and review activity." action={<Link className="button button-primary" to="/new"><FilePlus2 size={17} /> New content review</Link>} />
    <div className="fictional-banner"><Sparkles size={17} /><span><b>Demo workspace</b> — NovaFit is fictional data created for this product demonstration.</span><Link to="/brand">View brand profile <ArrowRight size={14} /></Link></div>
    <div className="metric-grid">{metrics.map(({ title, value, icon: Icon, color }) => <article className="metric-card" key={title}><div className={`metric-icon ${color}`}><Icon size={19} /></div><span className="metric-label">{title}</span><strong>{value}</strong><small>Live workspace data</small></article>)}</div>
    <section className="content-card">
      <div className="section-head"><div><h2>Recent submissions</h2><p>Latest content reviewed in this workspace</p></div><Link className="text-link" to="/submissions">View all <ArrowRight size={15} /></Link></div>
      {data.recent.length ? <SubmissionTable rows={data.recent} /> : <EmptyState icon={<SearchCheck />} title="No submissions yet" text="Submit your first marketing asset to check it against the NovaFit brand." action={<Link className="button button-primary button-small" to="/new">Start a review <ArrowRight size={15} /></Link>} />}
    </section>
    <div className="dashboard-footnote"><ShieldCheck size={16} /> Analysis supports human review; it does not guarantee legal or brand compliance.</div>
  </>
}

function SubmissionTable({ rows }: { rows: Submission[] }) {
  return <div className="table-scroll"><table><thead><tr><th>CONTENT</th><th>TYPE</th><th>STATUS</th><th>LAST UPDATED</th><th /></tr></thead><tbody>{rows.map((row) => <tr key={row.id}><td><Link to={`/submissions/${row.id}`} className="submission-title">{row.title}</Link><small className="table-subtitle">{row.findings.length} findings · {row.id.slice(0, 8)}</small></td><td>{titleCase(row.contentType)}</td><td><StatusBadge status={row.status} /></td><td>{date(row.updatedAt)}</td><td><Link to={`/submissions/${row.id}`} className="row-arrow" aria-label={`Open ${row.title}`}><ArrowRight size={17} /></Link></td></tr>)}</tbody></table></div>
}

function StatusBadge({ status }: { status: string }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}><span />{titleCase(status)}</span>
}

function EmptyState({ icon, title, text, action }: { icon: React.ReactNode; title: string; text: string; action?: React.ReactNode }) {
  return <div className="empty-state"><div className="empty-icon">{icon}</div><h3>{title}</h3><p>{text}</p>{action}</div>
}

function BrandPage() {
  const [brand, setBrand] = useState<Brand>()
  const [product, setProduct] = useState<Product>()
  const [rules, setRules] = useState<Rule[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [saving, setSaving] = useState(false)
  const load = () => Promise.all([
    api<Brand>(`/brands/${brandId}`), api<Product[]>(`/brands/${brandId}/products`), api<Rule[]>(`/brands/${brandId}/guidelines`),
  ]).then(([b, p, r]) => { setBrand(b); setProduct(p[0]); setRules(r) }).catch((e: Error) => setError(e.message)).finally(() => setLoading(false))
  useEffect(() => { void load() }, [])
  const save = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!brand || !product) return
    const form = new FormData(event.currentTarget)
    setSaving(true); setError(''); setSuccess('')
    try {
      await Promise.all([
        api(`/brands/${brand.id}`, { method: 'PUT', body: JSON.stringify({ name: form.get('name'), description: form.get('description'), preferredTone: form.get('tone') }) }),
        api(`/products/${product.id}`, { method: 'PUT', body: JSON.stringify({ name: form.get('productName'), description: form.get('productDescription'), price: form.get('price') }) }),
      ])
      setSuccess('Brand profile and product facts saved.')
      await load()
    } catch (e) { setError((e as Error).message) } finally { setSaving(false) }
  }
  const saveRule = async (event: React.FormEvent<HTMLFormElement>, rule: Rule) => {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setSaving(true); setError(''); setSuccess('')
    try {
      await api(`/guidelines/${rule.id}`, { method: 'PUT', body: JSON.stringify({ value: form.get('value'), explanation: form.get('explanation'), severity: form.get('severity'), enabled: form.get('enabled') === 'on' }) })
      setSuccess('Guideline updated.')
      await load()
    } catch (e) { setError((e as Error).message) } finally { setSaving(false) }
  }
  if (loading) return <Loading />
  if (!brand || !product) return <><PageTitle eyebrow="BRAND KNOWLEDGE HUB" title="Brand profile" /><Notice error={error || 'Brand information is unavailable.'} /></>
  return <>
    <PageTitle eyebrow="BRAND KNOWLEDGE HUB" title="Approved brand knowledge" description="The source of truth used by deterministic checks and AI analysis." />
    <Notice error={error} success={success} />
    <div className="brand-summary"><div className="brand-summary-mark">N</div><div><div className="brand-name-line"><h2>{brand.name}</h2><span className="fictional-tag">FICTIONAL DEMO</span></div><p>{brand.description}</p></div><div className="brand-summary-meta"><small>PRODUCTS</small><b>1 approved</b></div></div>
    <form className="content-card form-card" onSubmit={save}>
      <div className="section-head"><div><h2>Company profile</h2><p>Core identity details and product facts.</p></div><button className="button button-primary button-small" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save changes'}</button></div>
      <div className="form-grid"><Field label="Company name"><input name="name" defaultValue={brand.name} required /></Field><Field label="Preferred tone"><input name="tone" defaultValue={brand.preferredTone} required /></Field><Field label="Company description" className="full-field"><textarea name="description" defaultValue={brand.description} rows={2} required /></Field></div>
      <div className="subsection-title"><Package size={17} /><div><b>Approved product</b><small>Product names and prices are checked against these facts.</small></div></div>
      <div className="form-grid"><Field label="Exact product name"><input name="productName" defaultValue={product.name} required /></Field><Field label="Approved price (INR)"><input name="price" defaultValue={product.price.replace('₹', '').replaceAll(',', '')} inputMode="decimal" required /></Field><Field label="Product description" className="full-field"><textarea name="productDescription" defaultValue={product.description} rows={2} required /></Field></div>
    </form>
    <section className="content-card form-card">
      <div className="section-head"><div><h2>Guidelines and rules</h2><p>Enable or update guidance used during content analysis.</p></div></div>
      <div className="guideline-list">{rules.map((rule) => <form className="guideline-row" key={rule.id} onSubmit={(event) => void saveRule(event, rule)}><div className="guideline-info"><span className={`rule-kind kind-${rule.kind.toLowerCase()}`}>{titleCase(rule.kind)}</span><label className="switch"><input name="enabled" type="checkbox" defaultChecked={rule.enabled} /><span /></label></div><div className="form-grid"><Field label="Rule value"><input name="value" defaultValue={rule.value} required /></Field><Field label="Severity"><select name="severity" defaultValue={rule.severity}>{['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map((severity) => <option key={severity}>{severity}</option>)}</select></Field><Field label="Explanation" className="full-field"><input name="explanation" defaultValue={rule.explanation} required /></Field></div><div className="guideline-actions"><button type="submit" className="button button-secondary button-small" disabled={saving}>Save guideline</button></div></form>)}</div>
    </section>
    <div className="dashboard-footnote"><ShieldCheck size={16} /> Brand facts are reviewed and stored by your workspace. AI does not set approved prices.</div>
  </>
}

function Field({ label, children, className = '' }: { label: string; children: React.ReactNode; className?: string }) {
  return <label className={`field ${className}`}><span>{label}</span>{children}</label>
}

function NewSubmissionPage() {
  const [error, setError] = useState('')
  const [sending, setSending] = useState(false)
  const navigate = useNavigate()
  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setSending(true); setError('')
    try {
      const created = await api<Submission>('/submissions', { method: 'POST', body: JSON.stringify({ title: form.get('title'), contentType: form.get('contentType'), content: form.get('content') }) })
      navigate(`/submissions/${created.id}`)
    } catch (e) { setError((e as Error).message) } finally { setSending(false) }
  }
  return <>
    <PageTitle eyebrow="NEW REVIEW" title="Review marketing content" description="Create a submission to check it against the approved NovaFit brand profile." />
    <Notice error={error} />
    <div className="two-column-layout">
      <form className="content-card form-card" onSubmit={(event) => void submit(event)}>
        <div className="section-head"><div><h2>Content details</h2><p>Paste a draft or start with the demo scenario.</p></div><span className="required-label">* Required</span></div>
        <div className="form-grid"><Field label="Content title"><input name="title" placeholder="e.g. Summer campaign social post" maxLength={160} required /></Field><Field label="Content type"><select name="contentType" defaultValue="SOCIAL_POST"><option value="ADVERTISEMENT">Advertisement copy</option><option value="SOCIAL_POST">Social media post</option><option value="PROMOTIONAL_EMAIL">Promotional email</option></select></Field><Field label="Marketing content" className="full-field"><textarea name="content" rows={8} placeholder="Write or paste the content you want to review…" maxLength={12000} required /></Field></div>
        <div className="form-footer"><span>Up to 12,000 characters · Your original is preserved</span><button type="submit" className="button button-primary" disabled={sending}>{sending ? 'Creating submission…' : 'Continue to analysis'}<ArrowRight size={16} /></button></div>
      </form>
      <aside className="demo-card"><div className="demo-icon"><Sparkles size={19} /></div><span className="eyebrow">TRY THE DEMO</span><h3>Spot a brand mismatch</h3><p>Use this example to see product, price, and promotional claim checks in action.</p><blockquote>“Get guaranteed fitness results with our new Nova Smart Watch for just ₹1,499!”</blockquote><div className="demo-check-list"><span><CircleAlert size={15} /> Product name differs</span><span><CircleAlert size={15} /> Price is outdated</span><span><CircleAlert size={15} /> Guarantee needs evidence</span></div></aside>
    </div>
  </>
}

function SubmissionsPage() {
  const [rows, setRows] = useState<Submission[]>()
  const [error, setError] = useState('')
  useEffect(() => { api<Submission[]>('/submissions').then(setRows).catch((e: Error) => setError(e.message)) }, [])
  return <><PageTitle eyebrow="CONTENT LIBRARY" title="Submission history" description="Every draft, analysis, and review decision in one place." action={<Link className="button button-primary" to="/new"><FilePlus2 size={17} /> New submission</Link>} />{error && <Notice error={error} />}{!rows ? <Loading /> : rows.length ? <section className="content-card"><SubmissionTable rows={rows} /></section> : <section className="content-card"><EmptyState icon={<FileClock />} title="Nothing here yet" text="New submissions will appear here with their review status and analysis findings." action={<Link className="button button-primary button-small" to="/new">Create a submission</Link>} /></section>}</>
}

function ApprovalsPage() {
  const [rows, setRows] = useState<Submission[]>()
  const [error, setError] = useState('')
  useEffect(() => { api<Submission[]>('/submissions').then((items) => setRows(items.filter((item) => item.status === 'PENDING_APPROVAL'))).catch((e: Error) => setError(e.message)) }, [])
  return <><PageTitle eyebrow="HUMAN REVIEW" title="Approval queue" description="Review analyzed content and record an approval or request specific changes." />{error && <Notice error={error} />}{!rows ? <Loading /> : rows.length ? <section className="content-card"><SubmissionTable rows={rows} /></section> : <section className="content-card"><EmptyState icon={<ClipboardCheck />} title="Queue is clear" text="Content submitted for review will show up here." /></section>}</>
}

function SubmissionPage() {
  const { id = '' } = useParams()
  const [item, setItem] = useState<Submission>()
  const [analysis, setAnalysis] = useState<Analysis>()
  const [edited, setEdited] = useState('')
  const [comment, setComment] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [busy, setBusy] = useState(false)
  const [showHistory, setShowHistory] = useState(false)
  const load = () => api<Submission>(`/submissions/${id}`).then((submission) => {
    setItem(submission)
    setEdited(submission.status === 'PENDING_APPROVAL' || submission.status === 'APPROVED'
      ? submission.currentContent : submission.correctedContent ?? submission.currentContent)
  }).catch((e: Error) => setError(e.message))
  useEffect(() => { void load() }, [id])
  const act = async (fn: () => Promise<unknown>, successText: string) => {
    setBusy(true); setError(''); setSuccess('')
    try { await fn(); await load(); setSuccess(successText) } catch (e) { setError((e as Error).message) } finally { setBusy(false) }
  }
  const analyze = async () => {
    setBusy(true); setError(''); setSuccess('')
    try {
      const result = await api<Analysis>(`/submissions/${id}/analyze`, { method: 'POST' })
      setAnalysis(result); setEdited(result.correctedContent); await load()
      setSuccess(result.mode === 'AI_ASSISTED' ? 'Analysis complete with AI and deterministic checks.' : 'Rules-only analysis complete. No AI provider is configured.')
    } catch (e) { setError((e as Error).message) } finally { setBusy(false) }
  }
  const reanalyzeCorrection = async (content = edited) => {
    setBusy(true); setError(''); setSuccess('')
    try {
      await api(`/submissions/${id}/content`, { method: 'PUT', body: JSON.stringify({ content }) })
      const result = await api<Analysis>(`/submissions/${id}/analyze`, { method: 'POST' })
      setAnalysis(result); setEdited(result.correctedContent); await load()
      setSuccess('Your edited version was saved as a revision and analyzed again.')
    } catch (e) { setError((e as Error).message) } finally { setBusy(false) }
  }
  const submitReview = () => act(() => api(`/submissions/${id}/submit-for-review`, { method: 'POST' }), 'Submitted to the human approval queue.')
  const review = (decision: 'approve' | 'request-changes') => act(() => api(`/submissions/${id}/${decision}`, { method: 'POST', body: JSON.stringify({ reviewer: 'Demo reviewer', comment }) }), decision === 'approve' ? 'Content approved.' : 'Changes requested and recorded.')
  if (!item) return error ? <><Link to="/submissions" className="back-link"><ArrowLeft size={16} /> All submissions</Link><Notice error={error} /></> : <Loading />
  const findings = analysis?.findings ?? item.findings
  const activeText = analysis?.summary || item.analysisSummary
  return <>
    <Link to="/submissions" className="back-link"><ArrowLeft size={16} /> All submissions</Link>
    <PageTitle eyebrow={`${titleCase(item.contentType)} · CREATED ${date(item.createdAt)}`} title={item.title} description={`Submission ${item.id.slice(0, 8)} · Last updated ${date(item.updatedAt)}`} action={<StatusBadge status={item.status} />} />
    <Notice error={error} success={success} />
    <div className="submission-layout">
      <div className="submission-main">
        <section className="content-card">
          <div className="section-head"><div><h2>Analysis results</h2><p>Evidence-linked checks support—not replace—reviewer judgment.</p></div>{item.status !== 'PENDING_APPROVAL' && item.status !== 'APPROVED' && <button className="button button-primary button-small" onClick={() => void analyze()} disabled={busy}><SearchCheck size={16} />{busy ? 'Analyzing…' : item.status === 'ANALYZED' ? 'Run again' : 'Analyze content'}</button>}</div>
          {!activeText ? <div className="analysis-empty"><div className="empty-icon"><SearchCheck /></div><h3>Ready when you are</h3><p>Run deterministic brand checks and optional AI contextual analysis on this version.</p></div> : <><div className={`analysis-mode ${analysis?.mode === 'AI_ASSISTED' || item.analysisMode === 'AI_ASSISTED' ? 'mode-ai' : ''}`}><div className="mode-icon">{analysis?.mode === 'AI_ASSISTED' || item.analysisMode === 'AI_ASSISTED' ? <Sparkles size={17} /> : <ShieldCheck size={17} />}</div><div><b>{analysis?.mode === 'AI_ASSISTED' || item.analysisMode === 'AI_ASSISTED' ? 'AI-assisted analysis' : 'Rules-only analysis'}</b><small>{activeText}</small></div><span>HUMAN REVIEW REQUIRED</span></div>
            <div className="finding-summary"><b>{findings.length} {findings.length === 1 ? 'finding' : 'findings'}</b><div>{['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map((severity) => { const count = findings.filter((f) => f.severity === severity).length; return count ? <span key={severity} className={`severity-count severity-${severity.toLowerCase()}`}>{count} {severity.toLowerCase()}</span> : null })}{findings.length === 0 && <span className="clean-result"><Check size={14} /> No configured issues found</span>}</div></div>
            {findings.length > 0 && <div className="finding-list">{findings.map((finding) => <FindingCard key={finding.id || `${finding.category}-${finding.excerpt}`} finding={finding} />)}</div>}
          </>}
        </section>
        {activeText && <section className="content-card correction-card">
          <div className="section-head"><div><h2>Correction assistant</h2><p>Review and edit suggestions. Nothing is changed until you save and re-analyze.</p></div><span className="suggestion-label"><Sparkles size={14} /> SUGGESTED DRAFT</span></div>
          <div className="original-preview"><small>ORIGINAL SUBMISSION</small><p>{item.originalContent}</p></div>
          <label className="field"><span>Proposed content · edit before using</span><textarea rows={5} value={edited} onChange={(event) => setEdited(event.target.value)} disabled={item.status === 'PENDING_APPROVAL' || item.status === 'APPROVED'} /></label>
          {item.status !== 'PENDING_APPROVAL' && item.status !== 'APPROVED' && <div className="form-footer correction-actions"><span>Original content remains preserved in history.</span><div><button className="button button-secondary button-small" onClick={() => setEdited(item.originalContent)} disabled={busy}>Restore original</button><button className="button button-primary button-small" onClick={() => void reanalyzeCorrection()} disabled={busy}>{busy ? 'Saving…' : 'Save edits & re-analyze'}<ArrowRight size={15} /></button></div></div>}
          {item.status === 'ANALYZED' && <div className="review-submit"><div><b>Ready for a human decision?</b><small>Submit the analyzed version to the approval queue.</small></div><button className="button button-primary" onClick={() => void submitReview()} disabled={busy}>Submit for review <ArrowRight size={15} /></button></div>}
        </section>}
        <section className="content-card history-card"><button className="history-toggle" onClick={() => setShowHistory(!showHistory)}><span><FileClock size={17} /><b>Revision & review history</b><small>{item.revisions.length} revisions · {item.reviews.length} decisions</small></span>{showHistory ? <X size={17} /> : <ChevronRight size={17} />}</button>{showHistory && <div className="timeline">{[...item.revisions.map((entry) => ({ ...entry, kind: 'revision' })), ...item.reviews.map((entry) => ({ ...entry, content: entry.comment, kind: 'review' }))].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()).map((entry) => <div className="timeline-item" key={entry.id}><span className={`timeline-dot ${entry.kind}`} /><div><b>{titleCase(entry.action.replaceAll('_', ' '))}</b><small>{date(entry.createdAt)} · {entry.kind === 'review' && 'reviewer' in entry ? entry.reviewer : 'Content revision'}</small>{entry.content && <p>{entry.kind === 'revision' ? entry.content : entry.content}</p>}</div></div>)}</div>}</section>
      </div>
      <aside className="submission-aside">
        <div className="content-card context-card"><div className="aside-heading"><BookOpen size={17} /><b>Review context</b></div><div className="context-row"><small>BRAND</small><b>NovaFit <span className="fictional-tag">FICTIONAL</span></b></div><div className="context-row"><small>APPROVED PRODUCT</small><b>NovaFit Smart Band</b><span>₹1,999</span></div><div className="context-row"><small>PREFERRED TONE</small><span>Friendly, informative, realistic, and trustworthy</span></div><Link className="text-link" to="/brand">View all guidelines <ArrowRight size={14} /></Link></div>
        {item.status === 'PENDING_APPROVAL' && <div className="content-card decision-card"><div className="aside-heading"><ClipboardCheck size={17} /><b>Reviewer decision</b></div><p>Review the content and analysis before recording a decision.</p><Field label="Decision comment"><textarea rows={3} value={comment} onChange={(event) => setComment(event.target.value)} placeholder="Add context for the author…" /></Field><button className="button button-approve" onClick={() => void review('approve')} disabled={busy}><Check size={16} /> Approve content</button><button className="button button-secondary full-button" onClick={() => void review('request-changes')} disabled={busy || !comment.trim()}><ArrowDownLeft size={16} /> Request changes</button>{!comment.trim() && <small className="help-text">A comment is required to request changes.</small>}</div>}
        {item.status === 'APPROVED' && <div className="approved-banner"><BadgeCheck size={19} /><div><b>Approved</b><small>Decision saved in review history.</small></div></div>}
        <div className="aside-note"><CircleAlert size={16} /><span>Approval is recorded by the backend and cannot be changed by editing the page state.</span></div>
      </aside>
    </div>
  </>
}

function FindingCard({ finding }: { finding: Finding }) {
  return <details className="finding-card" open><summary><span className={`severity-dot severity-${finding.severity.toLowerCase()}`} /><div className="finding-headline"><b>{titleCase(finding.category)}</b><small>{finding.source === 'AI' ? 'AI contextual analysis' : 'Deterministic Java rule'}</small></div><span className={`severity-badge severity-${finding.severity.toLowerCase()}`}>{finding.severity}</span><ChevronRight size={16} className="finding-chevron" /></summary><div className="finding-body"><div className="excerpt-box"><small>FLAGGED EXCERPT</small><p>“{finding.excerpt}”</p></div><p className="finding-explanation">{finding.explanation}</p>{finding.expectedValue && <div className="evidence-line"><span>APPROVED VALUE</span><b>{finding.expectedValue}</b></div>}{finding.evidenceReference && <div className="evidence-line"><span><BookOpen size={13} /> BRAND EVIDENCE</span><b>{finding.evidenceReference}</b></div>}{finding.needsVerification && <div className="verification-note"><CircleAlert size={15} /> Requires human verification; this finding is not a determination that the claim is false.</div>}{finding.suggestion && <div className="suggestion-line"><Sparkles size={15} /><span><b>Suggestion</b>{finding.suggestion}</span></div>}</div></details>
}

function NotFound() { return <div className="not-found"><CircleAlert size={24} /><h1>Page not found</h1><Link to="/" className="button button-primary">Back to overview</Link></div> }

export default App
