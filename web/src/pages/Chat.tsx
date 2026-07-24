import { useState, useRef, useEffect } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { Send, ArrowLeft, Sparkles, FileText, User, Bot, Globe } from "lucide-react"
import { apiFetch } from "../lib/auth"
import AuthControls from "../components/AuthControls"

interface ChatMessage {
  role: "user" | "assistant"
  content: string
}

export default function Chat() {
  const navigate = useNavigate()
  const location = useLocation()

  const state = location.state as { documentId?: string; fileName?: string } | null
  const isGlobal = !state?.documentId
  const fileName = state?.fileName ?? "document.pdf"
  const documentId = state?.documentId ?? null

  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      role: "assistant",
      content: isGlobal
        ? "Hello! I can answer questions across all your documents. What would you like to know?"
        : `Hello! I've analyzed **${fileName}**. What would you like to know about this document?`,
    },
  ])
  const [inputValue, setInputValue] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages])

  function autoResize() {
    const el = inputRef.current
    if (el) {
      el.style.height = "auto"
      el.style.height = `${Math.min(el.scrollHeight, 160)}px`
    }
  }

  async function handleSend() {
    const text = inputValue.trim()
    if (!text || isLoading) return

    setInputValue("")
    if (inputRef.current) {
      inputRef.current.style.height = "auto"
    }

    setMessages((prev) => [...prev, { role: "user", content: text }])
    setIsLoading(true)

    try {
      const response = await apiFetch("/api/v1/ask", {
        method: "POST",
        body: JSON.stringify({ question: text, document_id: documentId }),
      })

      if (!response.ok) {
        throw new Error("Failed to get answer")
      }

      const data = await response.json()

      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: data.answer },
      ])
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: "Sorry, I couldn't process your question. Please try again.",
        },
      ])
    } finally {
      setIsLoading(false)
    }
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="h-screen flex flex-col bg-gradient-to-br from-background via-background to-primary/5">
      {/* header */}
      <header className="flex items-center gap-3 px-4 py-3 border-b border-border bg-background/80 backdrop-blur-lg supports-[backdrop-filter]:bg-background/60">
        <button
          onClick={() => navigate(isGlobal ? "/" : "/documents")}
          className="p-2 rounded-xl hover:bg-secondary transition-colors"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>

        <div className="flex items-center gap-3 flex-1 min-w-0">
          <div className={`h-9 w-9 rounded-xl flex items-center justify-center shrink-0 ring-1 ${isGlobal ? "bg-indigo-500/10 ring-indigo-500/20" : "bg-primary/10 ring-primary/20"}`}>
            {isGlobal ? <Globe className="h-4.5 w-4.5 text-indigo-500" /> : <FileText className="h-4.5 w-4.5 text-primary" />}
          </div>
          <div className="min-w-0">
            <p className="font-semibold text-sm truncate">{isGlobal ? "All Documents" : fileName}</p>
            <p className="text-xs text-muted-foreground flex items-center gap-1.5">
              <span className={`inline-block h-1.5 w-1.5 rounded-full ${isGlobal ? "bg-indigo-500" : "bg-emerald-500"}`} />
              {isGlobal ? "Global chat" : "Document chat"}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <AuthControls />
          <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
            <Sparkles className="h-4 w-4 text-primary-foreground" />
          </div>
          <span className="font-semibold text-sm hidden sm:inline">DocPilot AI</span>
        </div>
      </header>

      {/* messages */}
      <div className="flex-1 overflow-y-auto px-4 py-6 space-y-5 max-w-3xl mx-auto w-full scrollbar-thin">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`flex gap-3 ${msg.role === "user" ? "justify-end" : "justify-start"}`}
          >
            {msg.role === "assistant" && (
              <div className="h-8 w-8 rounded-xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center shrink-0 mt-0.5 shadow-sm">
                <Bot className="h-4 w-4 text-primary-foreground" />
              </div>
            )}

            <div
              className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-relaxed whitespace-pre-wrap shadow-sm ${
                msg.role === "user"
                  ? "bg-primary text-primary-foreground rounded-tr-sm"
                  : "bg-card text-card-foreground border border-border/50 rounded-tl-sm"
              }`}
            >
              {msg.content}
            </div>

            {msg.role === "user" && (
              <div className="h-8 w-8 rounded-xl bg-muted flex items-center justify-center shrink-0 mt-0.5 ring-1 ring-border/50">
                <User className="h-4 w-4 text-muted-foreground" />
              </div>
            )}
          </div>
        ))}

        {isLoading && (
          <div className="flex gap-3 justify-start">
            <div className="h-8 w-8 rounded-xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center shrink-0 shadow-sm">
              <Bot className="h-4 w-4 text-primary-foreground" />
            </div>
            <div className="bg-card border border-border/50 rounded-2xl rounded-tl-sm px-4 py-3.5 shadow-sm">
              <div className="flex gap-1.5">
                <span className="h-2 w-2 rounded-full bg-muted-foreground/30 animate-bounce [animation-delay:0ms]" />
                <span className="h-2 w-2 rounded-full bg-muted-foreground/30 animate-bounce [animation-delay:150ms]" />
                <span className="h-2 w-2 rounded-full bg-muted-foreground/30 animate-bounce [animation-delay:300ms]" />
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* input */}
      <div className="border-t border-border bg-background/80 backdrop-blur-lg supports-[backdrop-filter]:bg-background/60">
        <div className="max-w-3xl mx-auto w-full px-4 py-3">
          <div className="flex items-end gap-2 bg-card rounded-2xl border border-border/60 px-4 py-2 focus-within:border-primary/50 focus-within:ring-2 focus-within:ring-primary/20 transition-all shadow-sm">
            <textarea
              ref={inputRef}
              value={inputValue}
              onChange={(e) => {
                setInputValue(e.target.value)
                autoResize()
              }}
              onKeyDown={handleKeyDown}
              placeholder={isGlobal ? "Ask anything about your documents..." : "Ask a question about this document..."}
              rows={1}
              className="flex-1 bg-transparent resize-none outline-none text-sm py-2 max-h-40 placeholder:text-muted-foreground/50"
            />
            <button
              onClick={handleSend}
              disabled={!inputValue.trim() || isLoading}
              className="p-2.5 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90 transition-all disabled:opacity-40 disabled:cursor-not-allowed shrink-0 shadow-sm active:scale-95"
            >
              <Send className="h-4 w-4" />
            </button>
          </div>
          <p className="text-xs text-muted-foreground/40 text-center mt-2">
            {isGlobal ? "Answers are based on the content of all your documents" : "Answers are based on the content of this document"}
          </p>
        </div>
      </div>
    </div>
  )
}

