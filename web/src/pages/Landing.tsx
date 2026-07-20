import { useNavigate } from "react-router-dom"
import { Sparkles } from "lucide-react"

export default function Landing() {
  const navigate = useNavigate()

  function handleTryHere() {
    navigate("/upload")
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-background via-background to-primary/5 relative overflow-hidden">
      {/* bg glows */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-primary/10 via-transparent to-transparent pointer-events-none" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_left,_var(--tw-gradient-stops))] from-primary/5 via-transparent to-transparent pointer-events-none" />

      <main className="relative z-10 flex flex-col items-center gap-8 px-4 text-center max-w-3xl">
        {/* icon */}
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

        <button
          onClick={handleTryHere}
          className="mt-4 inline-flex items-center gap-2 rounded-full bg-primary px-8 py-4 text-base font-medium text-primary-foreground shadow-lg shadow-primary/30 transition-all hover:shadow-xl hover:shadow-primary/40 hover:scale-105 active:scale-95"
        >
          <Sparkles className="h-5 w-5" />
          Try Here
        </button>
      </main>
    </div>
  )
}
