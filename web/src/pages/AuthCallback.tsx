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
    const accessToken = searchParams.get("accessToken")
    const refreshToken = searchParams.get("refreshToken")
    const userId = searchParams.get("userId")
    const email = searchParams.get("email")
    const role = searchParams.get("role") ?? "USER"

    if (!accessToken || !refreshToken || !userId || !email) {
      setStatus("error")
      setError("Invalid authentication response")
      return
    }

    saveAuth(accessToken, refreshToken, userId, email, role)

    apiFetch("/api/v1/auth/claim", { method: "POST" })
      .catch(() => {})
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
