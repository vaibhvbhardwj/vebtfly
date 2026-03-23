# Vently Frontend

React + Vite frontend for the Vently event volunteer platform.

## Requirements

- Node.js 18+
- npm 9+

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create a `.env.local` file for local development:
```env
VITE_API_URL=http://localhost:8080/api/v1
```

3. Run dev server:
```bash
npm run dev
```

App runs at `http://localhost:5173`

## Build for Production

```bash
npm run build
```

Output goes to `dist/`. The `VITE_API_URL` is read from `.env.production` at build time:
```env
VITE_API_URL=https://api.knomochat.online/api/v1
```

## Deploy to EC2

After building:
```bash
scp -i "path/to/key.pem" dist/index.html ubuntu@YOUR_EC2_IP:/var/www/vently/
scp -i "path/to/key.pem" -r dist/assets ubuntu@YOUR_EC2_IP:/var/www/vently/
```

## Tech Stack

- React 19
- Vite 7
- Tailwind CSS 4
- React Router 7
- Axios
- Zustand (state management)
- Recharts (analytics charts)

## Color Palette

| Token | Value | Usage |
|-------|-------|-------|
| Primary | `#807aeb` | Buttons, links, accents |
| Background | `#ebf2fa` | Page background |
| Text | `#111827` | Primary text |
| Secondary | `#6B7280` | Muted text, labels |
| Success | `#10B981` | Success states |
| Danger | `#EF4444` | Error states |

## Project Structure

```
src/
├── api/            # Axios config and interceptors
├── components/     # Shared UI components
├── pages/
│   ├── auth/       # Login, Register
│   ├── events/     # Event list, detail, create, edit
│   ├── organizer/  # Organizer dashboard
│   ├── volunteer/  # Volunteer dashboard
│   └── admin/      # Admin panel
└── store/          # Zustand stores (auth, etc.)
```
