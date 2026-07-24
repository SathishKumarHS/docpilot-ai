import { useEffect, useState } from "react"
import { useNavigate, useSearchParams } from "react-router-dom"
import { saveAuth, apiFetch } from "../lib/auth"
import { Sparkles, Loader2 } from "lucide-react"

export default function AuthCallback() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [status, setStatus] = useState<"processing" | "error">("processing")
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const code = searchParams.get("code")

    if (!code) {
      setStatus("error")
      setError("Invalid authentication response")
      return
    }

    fetch("/api/v1/auth/exchange", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code }),
    })
      .then(async (res) => {
        if (!res.ok) throw new Error("Authentication failed")
        const data = await res.json()
        saveAuth(data.accessToken, data.refreshToken, data.userId, data.email, data.role)
        return apiFetch("/api/v1/auth/claim", { method: "POST" })
      })
      .catch(() => setError("Authentication failed"))
      .finally(() => navigate("/", { replace: true }))
  }, [searchParams, navigate])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-background via-background to-primary/5">
      <div className="flex flex-col items-center gap-4">
        {status === "error" ? (
          <>
            <div className="h-14 w-14 rounded-2xl bg-destructive/10 flex items-center justify-center">
              <Sparkles className="h-7 w-7 text-destructive" />
            </div>
            <p className="text-destructive font-medium">{error}</p>
            <button
              onClick={() => navigate("/login")}
              className="text-sm text-primary hover:underline"
            >
              Back to login
            </button>
          </>
        ) : (
          <>
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
            <p className="text-muted-foreground">Signing you in...</p>
          </>
        )}
      </div>
    </div>
  )
}
