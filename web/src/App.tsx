import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom"
import Landing from "./pages/Landing"
import UploadPage from "./pages/Upload"
import Chat from "./pages/Chat"
import Documents from "./pages/Documents"

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/upload" element={<UploadPage />} />
        <Route path="/chat" element={<ChatWrapper />} />
        <Route path="/documents" element={<Documents />} />
      </Routes>
    </BrowserRouter>
  )
}

function ChatWrapper() {
  const location = useLocation()
  const state = location.state as { documentId?: string } | null
  return <Chat key={state?.documentId ?? "global"} />
}

export default App
