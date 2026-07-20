import { useState, useRef, useEffect } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { Send, ArrowLeft, Sparkles, FileText, User } from "lucide-react"

interface ChatMessage {
  role: "user" | "assistant"
  content: string
}

export default function Chat() {
  const navigate = useNavigate()
  const location = useLocation()

  // grab filename from upload page
  const fileName = (location.state as { fileName?: string })?.fileName ?? "document.pdf"

  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      role: "assistant",
      content: `Hello! I've analyzed **${fileName}**. What would you like to know about this document?`,
    },
  ])
  const [inputValue, setInputValue] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  // auto scroll to bottom
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages])

  function handleSend() {
    const text = inputValue.trim()
    if (!text || isLoading) return

    setInputValue("")

    // add user message
    setMessages((prev) => [...prev, { role: "user", content: text }])
    setIsLoading(true)

    // simulate AI response
    setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: `This is a simulated response for: "${text}". In production, this would query the PDF content via an AI backend.`,
        },
      ])
      setIsLoading(false)
    }, 1000)
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="h-screen flex flex-col bg-background">
      {/* header bar */}
      <header className="flex items-center gap-3 px-4 py-3 border-b border-border bg-secondary/30 backdrop-blur-sm">
        <button
          onClick={() => navigate("/upload")}
          className="p-2 rounded-xl hover:bg-secondary transition-colors"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>

        <div className="flex items-center gap-2 flex-1 min-w-0">
          <div className="h-8 w-8 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
            <FileText className="h-4 w-4 text-primary" />
          </div>
          <div className="min-w-0">
            <p className="font-medium text-sm truncate">{fileName}</p>
            <p className="text-xs text-muted-foreground">Ready to answer questions</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
            <Sparkles className="h-4 w-4 text-primary-foreground" />
          </div>
          <span className="font-semibold hidden sm:inline">DocPilot AI</span>
        </div>
      </header>

      {/* messages area */}
      <div className="flex-1 overflow-y-auto px-4 py-6 space-y-4 max-w-3xl mx-auto w-full">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`flex gap-3 ${msg.role === "user" ? "justify-end" : "justify-start"}`}
          >
            {msg.role === "assistant" && (
              <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center shrink-0 mt-0.5">
                <Sparkles className="h-4 w-4 text-primary-foreground" />
              </div>
            )}

            <div
              className={`max-w-[80%] rounded-2xl px-4 py-3 text-sm leading-relaxed ${
                msg.role === "user"
                  ? "bg-primary text-primary-foreground rounded-tr-sm"
                  : "bg-secondary text-foreground rounded-tl-sm"
              }`}
            >
              {msg.content}
            </div>

            {msg.role === "user" && (
              <div className="h-8 w-8 rounded-lg bg-muted flex items-center justify-center shrink-0 mt-0.5">
                <User className="h-4 w-4 text-muted-foreground" />
              </div>
            )}
          </div>
        ))}

        {/* typing indicator */}
        {isLoading && (
          <div className="flex gap-3 justify-start">
            <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center shrink-0">
              <Sparkles className="h-4 w-4 text-primary-foreground" />
            </div>
            <div className="bg-secondary rounded-2xl rounded-tl-sm px-4 py-3">
              <div className="flex gap-1">
                <span className="h-2 w-2 rounded-full bg-muted-foreground/40 animate-bounce [animation-delay:0ms]" />
                <span className="h-2 w-2 rounded-full bg-muted-foreground/40 animate-bounce [animation-delay:150ms]" />
                <span className="h-2 w-2 rounded-full bg-muted-foreground/40 animate-bounce [animation-delay:300ms]" />
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* input bar */}
      <div className="border-t border-border bg-secondary/30 backdrop-blur-sm">
        <div className="max-w-3xl mx-auto w-full px-4 py-3">
          <div className="flex items-end gap-2 bg-secondary rounded-2xl border border-border px-4 py-2 focus-within:border-primary/50 focus-within:ring-1 focus-within:ring-primary/30 transition-all">
            <textarea
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask a question about your document..."
              rows={1}
              className="flex-1 bg-transparent resize-none outline-none text-sm py-2 max-h-32 placeholder:text-muted-foreground/60"
            />
            <button
              onClick={handleSend}
              disabled={!inputValue.trim() || isLoading}
              className="p-2 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90 transition-colors disabled:opacity-40 disabled:cursor-not-allowed shrink-0"
            >
              <Send className="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
