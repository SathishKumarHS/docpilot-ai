import { useState, useRef, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { Upload as UploadIcon, FileText, ArrowLeft, Sparkles, Loader2, Globe } from "lucide-react"
import { apiFetch, getAccessToken, getAnonymousToken } from "../lib/auth.ts"
import AuthControls from "../components/AuthControls.tsx"

export default function UploadPage() {
  const navigate = useNavigate()
  const checkedAuth = useRef(false)

  useEffect(() => {
    if (checkedAuth.current) return
    checkedAuth.current = true
    if (!getAccessToken() && !getAnonymousToken()) {
      navigate("/", { replace: true })
    }
  }, [navigate])
  const [file, setFile] = useState<File | null>(null)
  const [isDragOver, setIsDragOver] = useState(false)
  const [isUploading, setIsUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  function handleFilePick(f: File) {
    setError(null)
    if (f.type === "application/pdf") {
      setFile(f)
    } else {
      setError("Please upload a PDF file.")
    }
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault()
    setIsDragOver(false)
    const droppedFile = e.dataTransfer.files[0]
    if (droppedFile) handleFilePick(droppedFile)
  }

  function handleDragOver(e: React.DragEvent) {
    e.preventDefault()
    setIsDragOver(true)
  }

  function handleDragLeave() {
    setIsDragOver(false)
  }

  function handleInputChange(e: React.ChangeEvent<HTMLInputElement>) {
    const selectedFile = e.target.files?.[0]
    if (selectedFile) handleFilePick(selectedFile)
  }

  function handleRemoveFile(e: React.MouseEvent) {
    e.stopPropagation()
    setFile(null)
  }

  async function handleAnalyze() {
    if (!file || isUploading) return

    setIsUploading(true)

    try {
      const formData = new FormData()
      formData.append("file", file)

      const response = await apiFetch("/api/v1/documents", {
        method: "POST",
        body: formData,
      })

      const data = await response.json()

      if (!response.ok) {
        setError(data.message || "Upload failed")
        return
      }

      navigate(`/chat?documentId=${encodeURIComponent(data.id)}&fileName=${encodeURIComponent(data.fileName)}`)
    } catch {
      setError("Failed to upload document. Please try again.")
    } finally {
      setIsUploading(false)
    }
  }

  const dropZoneClass = isDragOver
    ? "border-primary bg-primary/5 scale-[1.02]"
    : file
      ? "border-primary/50 bg-primary/5"
      : "border-border hover:border-primary/50 hover:bg-secondary/50"

  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-br from-background via-background to-primary/5">
      {/* header */}
      <header className="flex items-center gap-3 px-6 py-4 border-b border-border">
        <button
          onClick={() => navigate("/")}
          className="p-2 rounded-xl hover:bg-secondary transition-colors"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div className="flex items-center gap-2 flex-1">
          <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
            <Sparkles className="h-4 w-4 text-primary-foreground" />
          </div>
          <span className="font-semibold text-lg">DocPilot AI</span>
        </div>
        <button
          onClick={() => navigate("/documents")}
          className="inline-flex items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-sm font-medium hover:bg-secondary transition-colors"
        >
          <FileText className="h-4 w-4" />
          My Documents
        </button>
        <button
          onClick={() => navigate("/chat")}
          className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-500/10 px-3 py-1.5 text-sm font-medium text-indigo-500 hover:bg-indigo-500/20 transition-colors"
        >
          <Globe className="h-4 w-4" />
          Global Chat
        </button>
        <AuthControls />
      </header>

      <main className="flex-1 flex flex-col items-center justify-center p-6">
        <div className="w-full max-w-lg">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold tracking-tight mb-2">Upload your PDF</h1>
            <p className="text-muted-foreground">
              Drag and drop your document below, or click to browse
            </p>
          </div>

          {/* drop zone */}
          <div
            role="button"
            tabIndex={0}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => inputRef.current?.click()}
            className={`relative cursor-pointer rounded-2xl border-2 border-dashed p-12 text-center transition-all ${dropZoneClass}`}
          >
            <input
              ref={inputRef}
              type="file"
              accept="application/pdf"
              onChange={handleInputChange}
              className="hidden"
            />

            {file ? (
              <div className="flex flex-col items-center gap-3">
                <div className="h-16 w-16 rounded-2xl bg-primary/10 flex items-center justify-center">
                  <FileText className="h-8 w-8 text-primary" />
                </div>
                <div>
                  <p className="font-medium text-foreground">{file.name}</p>
                  <p className="text-sm text-muted-foreground">
                    {(file.size / 1024 / 1024).toFixed(2)} MB
                  </p>
                </div>
                <button
                  onClick={handleRemoveFile}
                  className="text-sm text-muted-foreground underline hover:text-foreground"
                >
                  Remove
                </button>
              </div>
            ) : (
              <div className="flex flex-col items-center gap-3">
                <div className="h-16 w-16 rounded-2xl bg-secondary flex items-center justify-center">
                  <UploadIcon className="h-8 w-8 text-muted-foreground" />
                </div>
                <div>
                  <p className="font-medium text-foreground">
                    <span className="text-primary">Click to upload</span> or drag and drop
                  </p>
                  <p className="text-sm text-muted-foreground mt-1">PDF files only</p>
                </div>
              </div>
            )}
          </div>

          <button
            onClick={handleAnalyze}
            disabled={!file || isUploading}
            className="mt-6 w-full inline-flex items-center justify-center gap-2 rounded-xl bg-primary px-8 py-3.5 text-base font-medium text-primary-foreground shadow-lg shadow-primary/25 transition-all hover:shadow-xl hover:shadow-primary/30 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:scale-100 disabled:hover:shadow-lg"
          >
            {isUploading ? (
              <Loader2 className="h-5 w-5 animate-spin" />
            ) : (
              <Sparkles className="h-5 w-5" />
            )}
            {isUploading ? "Uploading..." : "Analyze Document"}
          </button>

          {error && (
            <div className="mt-4 rounded-xl border border-destructive/30 bg-destructive/5 p-4">
              <p className="text-sm text-destructive">{error}</p>
              {error.toLowerCase().includes("limit") && (
                <button
                  onClick={() => navigate("/documents")}
                  className="mt-2 text-sm font-medium text-destructive underline underline-offset-2 hover:text-destructive/80"
                >
                  Manage your documents to free up space
                </button>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  )
}
