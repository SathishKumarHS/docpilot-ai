import { useNavigate } from "react-router-dom"
import { Sparkles, Globe, Upload } from "lucide-react"

export default function Landing() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-background via-background to-primary/5 relative overflow-hidden">
      {/* bg glows */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-primary/10 via-transparent to-transparent pointer-events-none" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_left,_var(--tw-gradient-stops))] from-primary/5 via-transparent to-transparent pointer-events-none" />

      <main className="relative z-10 flex flex-col items-center gap-8 px-4 text-center max-w-3xl">
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

        <div className="flex flex-col sm:flex-row items-center gap-4 mt-4">
          <button
            onClick={() => navigate("/chat")}
            className="inline-flex items-center gap-2.5 rounded-full bg-primary px-8 py-4 text-base font-medium text-primary-foreground shadow-lg shadow-primary/30 transition-all hover:shadow-xl hover:shadow-primary/40 hover:scale-105 active:scale-95"
          >
            <Globe className="h-5 w-5" />
            Global Chat
          </button>
          <button
            onClick={() => navigate("/upload")}
            className="inline-flex items-center gap-2.5 rounded-full bg-secondary px-8 py-4 text-base font-medium text-foreground border border-border/50 transition-all hover:bg-secondary/80 hover:scale-105 active:scale-95"
          >
            <Upload className="h-5 w-5" />
            Upload PDF
          </button>
        </div>
      </main>
    </div>
  )
}
