import { useState, useRef, useEffect } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { Send, ArrowLeft, Sparkles, FileText, User, Bot, Globe, Lightbulb, ChevronDown, ChevronUp, X } from "lucide-react"
import { apiFetch, getAccessToken, getAnonymousToken } from "../lib/auth.ts"
import AuthControls from "../components/AuthControls.tsx"

interface ChatMessage {
  role: "user" | "assistant"
  content: string
}

interface ChatProps {
  documentId?: string | null
}

function welcomeMessage(isGlobal: boolean, fileName: string): ChatMessage {
  return {
    role: "assistant",
    content: isGlobal
      ? "Hello! I can answer questions across all your documents. What would you like to know?"
      : `Hello! I've analyzed **${fileName}**. What would you like to know about this document?`,
  }
}

export default function Chat({ documentId: propDocumentId }: ChatProps) {
  const navigate = useNavigate()
  const location = useLocation()
  const checkedAuth = useRef(false)

  useEffect(() => {
    if (checkedAuth.current) return
    checkedAuth.current = true
    if (!getAccessToken() && !getAnonymousToken()) {
      navigate("/", { replace: true })
    }
  }, [navigate])

  const params = new URLSearchParams(location.search)
  const documentId = propDocumentId ?? params.get("documentId") ?? null
  const isGlobal = !documentId
  const locState = location.state as { fileName?: string; summary?: string } | null
  const fileName = params.get("fileName") ?? locState?.fileName ?? "document.pdf"
  const documentSummary = locState?.summary ?? null

  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [showSummary, setShowSummary] = useState(!!documentSummary)
  const [inputValue, setInputValue] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [streaming, setStreaming] = useState(false)
  const [suggestions, setSuggestions] = useState<Map<number, string[]>>(new Map())
  const [suggestionsLoading, setSuggestionsLoading] = useState(false)
  const streamingStarted = useRef(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)

  async function loadSuggestions(messageIndex: number) {
    setSuggestionsLoading(true)
    try {
      const res = await apiFetch(`/api/v1/ask/suggest`, {
        method: "POST",
        body: JSON.stringify({ document_id: documentId }),
      })
      if (!res.ok) return
      const data: { questions: string[] } = await res.json()
      if (data.questions.length > 0) {
        setSuggestions(new Map([[messageIndex, data.questions]]))
      }
    } catch {
      // silently ignore — suggestions are non-critical
    } finally {
      setSuggestionsLoading(false)
    }
  }

  function handleSuggestionClick(q: string) {
    handleSend(q)
  }

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages, suggestions])

  useEffect(() => {
    inputRef.current?.focus()
  }, [])

  function isRole(v: string): v is "user" | "assistant" {
    return v === "user" || v === "assistant"
  }

  useEffect(() => {
    async function loadHistory() {
      try {
        const params = new URLSearchParams()
        if (documentId) params.set("document_id", documentId)
        const res = await apiFetch(`/api/v1/ask/history?${params}`)
        if (!res.ok) return
        const data: { role: string; content: string }[] = await res.json()
        setMessages(data.length > 0
          ? data.map(m => ({ role: isRole(m.role) ? m.role : "assistant", content: m.content }))
          : [welcomeMessage(isGlobal, fileName)])
      } catch {
        setMessages([welcomeMessage(isGlobal, fileName)])
      }
    }
    loadHistory()
  }, [documentId, isGlobal, fileName])

  function autoResize() {
    const el = inputRef.current
    if (el) {
      el.style.height = "auto"
      el.style.height = `${Math.min(el.scrollHeight, 160)}px`
    }
  }

  async function handleSend(question?: string) {
    const text = (question ?? inputValue).trim()
    if (!text || isLoading) return

    const assistantMsgIndex = messages.length + 1

    setInputValue("")
    if (inputRef.current) {
      inputRef.current.style.height = "auto"
    }

    setMessages((prev) => [...prev, { role: "user", content: text }])
    setIsLoading(true)
    streamingStarted.current = false
    setStreaming(false)

    try {
      const response = await apiFetch("/api/v1/ask/stream", {
        method: "POST",
        body: JSON.stringify({
          question: text,
          document_id: documentId,
        }),
      })

      if (!response.ok) throw new Error("Failed to get answer")

      const reader = response.body!.getReader()
      const decoder = new TextDecoder()
      let buffer = ""

      outer: while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split("\n")
        buffer = lines.pop() || ""

        for (const line of lines) {
          if (line.startsWith("data:")) {
            try {
              const payload = JSON.parse(line.slice(5))
              if (payload.done) break outer
              if (payload.token) {
                if (!streamingStarted.current) {
                  streamingStarted.current = true
                  setStreaming(true)
                  setMessages((prev) => [...prev, { role: "assistant", content: payload.token }])
                } else {
                  setMessages((prev) => {
                    const updated = [...prev]
                    const last = updated[updated.length - 1]
                    updated[updated.length - 1] = { ...last, content: last.content + payload.token }
                    return updated
                  })
                }
              }
            } catch {
              // skip malformed events
            }
          }
        }
      }
    } catch {
      if (!streamingStarted.current) {
        setMessages((prev) => [
          ...prev,
          {
            role: "assistant",
            content: "Sorry, I couldn't process your question. Please try again.",
          },
        ])
      }
    } finally {
      setStreaming(false)
      setIsLoading(false)
      loadSuggestions(assistantMsgIndex)
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
              {documentSummary && (
                <button
                  onClick={() => setShowSummary(!showSummary)}
                  className="ml-2 inline-flex items-center gap-0.5 text-[11px] text-primary/70 hover:text-primary transition-colors"
                >
                  {showSummary ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />}
                  {showSummary ? "Hide summary" : "Show summary"}
                </button>
              )}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <AuthControls />
        </div>
      </header>

      {/* document summary */}
      {documentSummary && showSummary && (
        <div className="max-w-3xl mx-auto w-full px-4 pt-4">
          <div className="rounded-xl border border-primary/10 bg-primary/5 p-3.5">
            <div className="flex items-start gap-2.5">
              <FileText className="h-4 w-4 text-primary mt-0.5 shrink-0" />
              <div className="flex-1 min-w-0">
                <p className="text-xs font-medium text-primary mb-1">Document Summary</p>
                <p className="text-sm text-muted-foreground leading-relaxed">{documentSummary}</p>
              </div>
              <button
                onClick={() => setShowSummary(false)}
                className="p-0.5 rounded hover:bg-primary/10 text-muted-foreground/40 hover:text-muted-foreground transition-colors shrink-0"
                title="Dismiss summary"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </div>
          </div>
        </div>
      )}

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

            <div className="flex flex-col gap-2 max-w-[85%]">
              <div
                className={`rounded-2xl px-4 py-3 text-sm leading-relaxed whitespace-pre-wrap shadow-sm ${
                  msg.role === "user"
                    ? "bg-primary text-primary-foreground rounded-tr-sm"
                    : "bg-card text-card-foreground border border-border/50 rounded-tl-sm"
                }`}
              >
                {msg.content}
                {streaming && index === messages.length - 1 && msg.role === "assistant" && (
                  <span className="inline-block w-[1px] h-4 bg-foreground ml-0.5 animate-pulse" />
                )}
              </div>

              {msg.role === "assistant" && index === messages.length - 1 && !streaming && (
                <>
                  {suggestionsLoading && (
                    <div className="flex flex-wrap gap-2 mt-1 ml-1">
                      {[1, 2, 3].map((i) => (
                        <div
                          key={i}
                          className="h-7 w-32 rounded-full bg-muted/50 animate-pulse"
                        />
                      ))}
                    </div>
                  )}
                  {!suggestionsLoading && suggestions.get(index) && suggestions.get(index)!.length > 0 && (
                    <div className="flex flex-col gap-2 mt-1 ml-1">
                      <div className="flex items-center gap-1.5">
                        <Lightbulb className="h-3 w-3 text-amber-500" />
                        <span className="text-[11px] text-muted-foreground/60 font-medium uppercase tracking-wider">
                          Ask a follow-up
                        </span>
                        <button
                          onClick={() => {
                            setSuggestions(new Map())
                          }}
                          className="ml-auto text-muted-foreground/30 hover:text-muted-foreground/60 transition-colors"
                        >
                          <span className="text-[11px]">✕</span>
                        </button>
                      </div>
                      <div className="flex flex-wrap gap-1.5">
                        {suggestions.get(index)!.map((q, qi) => (
                          <button
                            key={qi}
                            onClick={() => handleSuggestionClick(q)}
                            style={{ animationDelay: `${qi * 80}ms` }}
                            className="animate-fade-in text-xs bg-muted/50 hover:bg-primary/10 hover:text-primary hover:border-primary/30 text-muted-foreground rounded-full px-3 py-1.5 border border-border/50 transition-all duration-200 active:scale-95"
                          >
                            {q}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>

            {msg.role === "user" && (
              <div className="h-8 w-8 rounded-xl bg-muted flex items-center justify-center shrink-0 mt-0.5 ring-1 ring-border/50">
                <User className="h-4 w-4 text-muted-foreground" />
              </div>
            )}
          </div>
        ))}

        {isLoading && !streaming && (
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
              onClick={() => handleSend()}
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

