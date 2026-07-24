import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom"
import Landing from "./pages/Landing"
import UploadPage from "./pages/Upload"
import Chat from "./pages/Chat"
import Documents from "./pages/Documents"
import Login from "./pages/Login"
import Register from "./pages/Register"
import AuthCallback from "./pages/AuthCallback"

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/upload" element={<UploadPage />} />
        <Route path="/chat" element={<ChatWrapper />} />
        <Route path="/documents" element={<Documents />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/auth/callback" element={<AuthCallback />} />
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
