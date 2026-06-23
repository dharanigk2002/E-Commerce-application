# E-commerce Admin Frontend

React + Vite + TypeScript admin dashboard for the e-commerce backend.

## Local Setup

Create a local environment file:

```text
VITE_API_BASE_URL=http://localhost:8080
```

Install dependencies and run:

```bash
npm.cmd install
npm.cmd run dev
```

## Deployment

Deploy to Vercel with:

```text
Root directory: frontend
VITE_API_BASE_URL=https://<render-backend-url>
```

After Vercel deployment, add the Vercel URL to the backend Render
`APP_CORS_ALLOWED_ORIGINS` setting.
