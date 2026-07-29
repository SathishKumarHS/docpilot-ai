# DocPilot Web

React 19 SPA with TypeScript 6, Vite 8, and Tailwind CSS 3. The user-facing interface for document upload, management, and AI chat.

## Tech

- **UI:** React 19, TypeScript 6, Vite 8
- **Styling:** Tailwind CSS 3
- **Icons:** Lucide React
- **Routing:** React Router 7
- **Linting:** Oxlint
- **Dev proxy:** Vite proxies `/api` and `/oauth2` to `localhost:8080`

## Project structure

```
web/src/
├── components/    # Shared UI components (AuthControls, etc.)
├── pages/         # Route pages (Landing, Chat, Login, Register, Documents, Upload)
├── lib/           # Auth utils, API client
└── assets/        # Static assets
```

## Running

```bash
npm install
npm run dev          # http://localhost:5173
npm run build        # Production build
npm run lint         # Oxlint
```

## Pages

| Route | Description |
|-------|-------------|
| `/` | Landing page with sign-up/login and anonymous entry |
| `/login` | Email/password or Google OAuth2 login |
| `/register` | Account registration |
| `/upload` | PDF upload and chunking status |
| `/documents` | Document list with delete |
| `/chat` | Global or per-document AI chat with conversation history |

## Proxy

Vite proxies `/api/*` → `http://localhost:8080` and `/oauth2/*` → `http://localhost:8080` in dev mode. No CORS issues during local development.
