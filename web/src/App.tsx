import { BrowserRouter, Routes, Route } from "react-router-dom"
import Landing from "./pages/Landing"
import UploadPage from "./pages/Upload"
import Chat from "./pages/Chat"

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/upload" element={<UploadPage />} />
        <Route path="/chat" element={<Chat />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
