import { useState } from "react"
import { useNavigate, Link } from "react-router-dom"
import { Sparkles, Upload, LogOut, Loader2, Globe, FileText } from "lucide-react"
import { isAuthenticated, getUser, clearAuth, startAnonymousSession } from "../lib/auth"

export default function Landing() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const loggedIn = isAuthenticated()
  const user = getUser()

  async function handleTryNow() {
    setLoading(true)
    try {
      await startAnonymousSession()
      navigate("/upload")
    } catch {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-br from-background via-background to-primary/5 relative overflow-hidden">
      {/* bg glows */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-primary/10 via-transparent to-transparent pointer-events-none" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_left,_var(--tw-gradient-stops))] from-primary/5 via-transparent to-transparent pointer-events-none" />

      {/* nav */}
      <header className="relative z-10 flex items-center justify-between px-6 py-4">
        <div className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
            <Sparkles className="h-4 w-4 text-primary-foreground" />
          </div>
          <span className="font-semibold">DocPilot AI</span>
        </div>
        <div className="flex items-center gap-3">
          {loggedIn ? (
            <>
              <span className="text-sm text-muted-foreground hidden sm:inline">{user?.email}</span>
              <button
                onClick={() => { clearAuth(); navigate("/") }}
                className="inline-flex items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-sm font-medium hover:bg-secondary transition-colors"
              >
                <LogOut className="h-4 w-4" />
                Logout
              </button>
            </>
          ) : (
            <>
              <button
                onClick={() => navigate("/login")}
                className="inline-flex items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-sm font-medium hover:bg-secondary transition-colors"
              >
                Sign in
              </button>
              <button
                onClick={() => navigate("/register")}
                className="inline-flex items-center gap-1.5 rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-primary-foreground hover:bg-primary/90 transition-colors"
              >
                Sign up
              </button>
            </>
          )}
        </div>
      </header>

      <main className="relative z-10 flex flex-col items-center justify-center flex-1 gap-8 px-4 text-center max-w-3xl mx-auto w-full pb-24">
        <div className="flex items-center gap-3 mb-2">
          <div className="h-14 w-14 rounded-2xl bg-primary flex items-center justify-center shadow-lg shadow-primary/25">
            <Sparkles className="h-7 w-7 text-primary-foreground" />
          </div>
        </div>

        <h1 className="text-6xl sm:text-7xl lg:text-8xl font-bold tracking-tight bg-gradient-to-r from-foreground via-foreground to-primary/70 bg-clip-text text-transparent">
          DocPilot AI
        </h1>

        <p className="text-lg sm:text-xl text-muted-foreground max-w-xl leading-relaxed">
          Upload your PDF documents and ask questions effortlessly. Your intelligent document companion powered by AI.
        </p>

        {loggedIn ? (
          <div className="flex flex-col sm:flex-row items-center gap-4 mt-4">
            <button
              onClick={() => navigate("/chat")}
              className="inline-flex items-center gap-2.5 rounded-full bg-primary px-8 py-4 text-base font-medium text-primary-foreground shadow-lg shadow-primary/30 transition-all hover:shadow-xl hover:shadow-primary/40 hover:scale-105 active:scale-95"
            >
              <Globe className="h-5 w-5" />
              Global Chat
            </button>
            <button
              onClick={() => navigate("/documents")}
              className="inline-flex items-center gap-2.5 rounded-full bg-secondary px-8 py-4 text-base font-medium text-foreground border border-border/50 transition-all hover:bg-secondary/80 hover:scale-105 active:scale-95"
            >
              <FileText className="h-5 w-5" />
              My Documents
            </button>
          </div>
        ) : (
          <>
            <button
              onClick={handleTryNow}
              disabled={loading}
              className="inline-flex items-center gap-2.5 rounded-full bg-primary px-10 py-5 text-lg font-medium text-primary-foreground shadow-lg shadow-primary/30 transition-all hover:shadow-xl hover:shadow-primary/40 hover:scale-105 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed mt-4"
            >
              {loading ? <Loader2 className="h-5 w-5 animate-spin" /> : <Upload className="h-5 w-5" />}
              Try DocPilot AI
            </button>
            <p className="text-sm text-muted-foreground mt-8">
              Upload a PDF and ask questions — no account needed.{" "}
              <Link to="/register" className="text-primary hover:underline font-medium">Sign up</Link> or{" "}
              <Link to="/login" className="text-primary hover:underline font-medium">Sign in</Link> to keep your documents.
            </p>
          </>
        )}
      </main>
    </div>
  )
}
