import { useNavigate } from "react-router-dom"
import { LogOut } from "lucide-react"
import { isAuthenticated, getUser, clearAuth } from "../lib/auth.ts"

export default function AuthControls() {
  const navigate = useNavigate()
  const loggedIn = isAuthenticated()
  const user = getUser()

  if (!loggedIn) return null

  return (
    <div className="flex items-center gap-2 shrink-0">
      <span className="text-xs text-muted-foreground hidden sm:inline max-w-[120px] truncate">
        {user?.email}
      </span>
      <button
        onClick={() => { clearAuth(); navigate("/") }}
        className="p-2 rounded-lg hover:bg-secondary transition-colors text-muted-foreground hover:text-foreground"
        title="Sign out"
      >
        <LogOut className="h-4 w-4" />
      </button>
    </div>
  )
}
