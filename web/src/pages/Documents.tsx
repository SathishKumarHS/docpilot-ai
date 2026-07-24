import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { ArrowLeft, FileText, MessageCircle, Trash2, Sparkles, Loader2, Globe, Upload } from "lucide-react"
import { apiFetch, getAccessToken, getAnonymousToken } from "../lib/auth"
import AuthControls from "../components/AuthControls"

interface Document {
  id: string
  fileName: string
  size: number
  uploadedAt: string
}

export default function Documents() {
  const navigate = useNavigate()
  const [documents, setDocuments] = useState<Document[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [deletingId, setDeletingId] = useState<string | null>(null)

  useEffect(() => {
    if (!getAccessToken() && !getAnonymousToken()) {
      navigate("/", { replace: true })
      return
    }
    fetchDocuments()
  }, [navigate])

  async function fetchDocuments() {
    try {
      const res = await apiFetch("/api/v1/documents")
      if (res.ok) {
        const data = await res.json()
        setDocuments(data.content ?? data)
      }
    } catch {
      // ignore
    } finally {
      setIsLoading(false)
    }
  }

  async function handleDelete(id: string) {
    setDeletingId(id)
    try {
      const res = await apiFetch(`/api/v1/documents/${id}`, {
        method: "DELETE",
      })
      if (res.ok) {
        setDocuments((prev) => prev.filter((d) => d.id !== id))
      }
    } catch {
      // ignore
    } finally {
      setDeletingId(null)
    }
  }

  function handleChat(doc: Document) {
    navigate("/chat", {
      state: { documentId: doc.id, fileName: doc.fileName },
    })
  }

  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-br from-background via-background to-primary/5">
      <header className="flex items-center gap-3 px-6 py-4 border-b border-border">
        <button
          onClick={() => navigate("/upload")}
          className="p-2 rounded-xl hover:bg-secondary transition-colors"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div className="flex items-center gap-2 flex-1">
          <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
            <Sparkles className="h-4 w-4 text-primary-foreground" />
          </div>
          <span className="font-semibold text-lg">My Documents</span>
        </div>
        <button
          onClick={() => navigate("/upload")}
          className="inline-flex items-center gap-1.5 rounded-lg bg-primary/10 px-3 py-1.5 text-sm font-medium text-primary hover:bg-primary/20 transition-colors"
        >
          <Upload className="h-4 w-4" />
          Upload
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

      <main className="flex-1 p-6 max-w-3xl mx-auto w-full">
        {isLoading ? (
          <div className="flex justify-center pt-20">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        ) : documents.length === 0 ? (
          <div className="text-center pt-20">
            <FileText className="h-12 w-12 mx-auto text-muted-foreground/40 mb-4" />
            <p className="text-lg font-medium text-muted-foreground">No documents yet</p>
            <p className="text-sm text-muted-foreground/60 mt-1">
              Upload a PDF to get started
            </p>
            <button
          onClick={() => navigate("/")}
              className="mt-6 inline-flex items-center gap-2 rounded-xl bg-primary px-6 py-2.5 text-sm font-medium text-primary-foreground hover:bg-primary/90"
            >
              Upload Document
            </button>
          </div>
        ) : (
          <div className="space-y-3">
            {documents.map((doc) => (
              <div
                key={doc.id}
                className="flex items-center gap-4 rounded-xl border border-border bg-card p-4 hover:shadow-md transition-shadow"
              >
                <div className="h-10 w-10 rounded-xl bg-primary/10 flex items-center justify-center shrink-0">
                  <FileText className="h-5 w-5 text-primary" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-medium truncate">{doc.fileName}</p>
                  <p className="text-sm text-muted-foreground">
                    {(doc.size / 1024 / 1024).toFixed(2)} MB
                  </p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <button
                    onClick={() => handleChat(doc)}
                    className="inline-flex items-center gap-1.5 rounded-lg bg-primary/10 px-3 py-2 text-sm font-medium text-primary hover:bg-primary/20 transition-colors"
                  >
                    <MessageCircle className="h-4 w-4" />
                    Chat
                  </button>
                  <button
                    onClick={() => handleDelete(doc.id)}
                    disabled={deletingId === doc.id}
                    className="inline-flex items-center gap-1.5 rounded-lg bg-destructive/10 px-3 py-2 text-sm font-medium text-destructive hover:bg-destructive/20 transition-colors disabled:opacity-40"
                  >
                    {deletingId === doc.id ? (
                      <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                      <Trash2 className="h-4 w-4" />
                    )}
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
