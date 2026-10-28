# BrandGuard AI

**Every Content. One Consistent Brand.**

BrandGuard AI is a hackathon MVP that helps teams review marketing copy against approved brand information before it is published. It combines deterministic checks, optional AI-assisted analysis, evidence-linked findings, suggested edits, and a human review workflow.

| | |
|---|---|
| Hackathon | Codex 48 |
| Team | LONE WOLF |
| Team member | Nirmal Ravidas |
| Category | Artificial Intelligence / Content Governance |

## The problem

Marketing content is created by many people and increasingly with AI tools. That can lead to incorrect product details, inconsistent terminology, unsupported claims, and avoidable review work. Generic writing tools do not necessarily know which product facts or brand rules are approved.

BrandGuard AI provides a review step between content creation and publication: it checks copy against a source of approved brand information, explains potential issues, and keeps people in control of the final decision.

## What this MVP does

- Maintains a fictional brand profile, approved product facts, and editable review guidelines.
- Checks submitted text for configured brand-rule violations, product-name and price mismatches, and unsupported promotional claims.
- Optionally uses a hosted LLM for contextual analysis and suggested corrections. Without a configured key, deterministic checks still work in rules-only mode.
- Shows findings, available evidence, and suggested corrections for creator review.
- Saves content revisions and supports submission, approval, and request-changes steps.
- Shows review workflow information and recent submission metrics in a dashboard.

The demo is seeded with **NovaFit**, a fictional brand and synthetic product information. Findings and corrections are assistive; they are not legal or compliance advice, and human review is still required.

## Demo walkthrough

1. Open **Brand knowledge** to inspect NovaFit's profile, product details, and guidelines.
2. Select **New content review** and try: `Get guaranteed fitness results with our new Nova Smart Watch for just ₹1,499!`
3. Analyze the copy to see the product, price, and promotional-claim findings.
4. Review or edit the suggested correction, then analyze edited content again.
5. Submit the analyzed content for review. In **Approval queue**, approve it or request changes with a comment.
6. Open the submission history to see saved revisions and review decisions.

## Technology

- **Frontend:** React, TypeScript, Vite, React Router
- **Backend:** Java 21, Spring Boot, Spring Web, Spring Data JPA
- **Database:** PostgreSQL
- **AI:** Optional OpenAI-compatible Chat Completions API; analysis falls back to deterministic rules if unavailable
- **Local deployment:** Docker Compose; Nginx serves the frontend and proxies `/api/` to the backend

## Run locally with Docker Compose

Requirements: Docker Engine and Docker Compose.

```bash
cp -n .env.example .env
```

Edit `.env` and set a local-only `DATABASE_PASSWORD`. Then start the app:

```bash
docker compose up --build -d
docker compose ps
```

Open [http://localhost:8081](http://localhost:8081). Check the API through the frontend proxy:

```bash
curl http://localhost:8081/api/health
docker compose logs -f db backend frontend
```

The database is stored in the `brandguard-db` named volume. Stop the app without deleting local database data:

```bash
docker compose down
```

Do not use `docker compose down -v` unless you intend to delete that volume.

## Environment variables

| Variable | Used by | Purpose |
|---|---|---|
| `DATABASE_USERNAME` | Compose / backend | PostgreSQL username; required by Compose |
| `DATABASE_PASSWORD` | Compose / backend | PostgreSQL password; required by Compose |
| `DATABASE_URL` | Backend | JDBC connection URL; Compose sets `jdbc:postgresql://db:5432/brandguard` |
| `OPENAI_API_KEY` | Backend | Optional secret enabling hosted AI analysis |
| `OPENAI_MODEL` | Backend | Optional model name; defaults to `gpt-4o-mini` |
| `OPENAI_CHAT_COMPLETIONS_URL` | Backend | Optional provider endpoint |
| `FRONTEND_ORIGIN` | Backend | Exact browser origin allowed by CORS |
| `FRONTEND_PORT` | Compose | Local published frontend port; defaults to `8081` |
| `VITE_API_URL` | Vercel frontend build | Backend API base URL, e.g. `https://<backend>.onrender.com/api` |
| `BACKEND_URL` | Compose frontend / Nginx | Backend upstream URL; defaults to `http://backend:8080` |

Keep `.env` and API keys out of Git. `.env.example` contains placeholders, not production credentials.

## Deploy the hackathon MVP

The app can be deployed with the backend and managed PostgreSQL on Render and the frontend on Vercel. These are separate deployments; Render does not run this Compose file as one multi-container service.

### Render: PostgreSQL and backend

1. Create a Render PostgreSQL database. Prefer its internal hostname when the database and backend are in the same region.
2. Create a Render Web Service from this repository using Docker. Set the backend root directory to `backend` (or configure the Dockerfile and build context to `backend/Dockerfile` and `backend`).
3. Add these backend environment variables in Render:
   - `DATABASE_URL`: `jdbc:postgresql://<internal-host>:5432/<database-name>`
   - `DATABASE_USERNAME` and `DATABASE_PASSWORD`: values from the Render database
   - `FRONTEND_ORIGIN`: your exact Vercel production origin, e.g. `https://<project>.vercel.app`
   - `OPENAI_API_KEY`: optional secret; omit it for rules-only analysis
4. After deployment, verify `https://<backend>.onrender.com/api/health` returns `{"status":"UP"}`.

Spring Boot reads Render's assigned `PORT`. Use the Render database's internal connection details and JDBC URL format; do not put credentials in the repository. Review the selected database plan, persistence, and backup options.

### Vercel: frontend

1. Import the repository into Vercel and set the project root directory to `frontend`.
2. Use the Vite framework preset, build command `npm run build`, and output directory `dist`.
3. Set `VITE_API_URL` for the production environment to `https://<backend>.onrender.com/api`.
4. Deploy, then set the backend's `FRONTEND_ORIGIN` to the exact Vercel production origin.

The `frontend/vercel.json` rewrite supports direct navigation and refreshes on frontend routes. Vercel's `VITE_API_URL` is used in the browser, so it must be the public Render backend URL, not a Docker service name. The backend currently allows one configured CORS origin; Vercel preview URLs may need a corresponding backend CORS configuration to work.

## Local development

Run PostgreSQL with `docker compose up -d db`. In the terminal where you start the backend, load the same credentials from `.env` before running Spring Boot:

```bash
set -a
. ./.env
set +a
cd backend
mvn spring-boot:run
```

In a second terminal, start the frontend:

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server is at [http://localhost:5173](http://localhost:5173) and uses `http://localhost:8080/api` by default. Set `VITE_API_URL` if the backend runs at a different address.

## Tests

```bash
cd backend && mvn test
cd frontend && npm run build
```

Backend tests cover deterministic rule checks, AI response handling, and key submission/review workflow transitions. The frontend build runs TypeScript checks and creates the Vite production bundle.

## MVP scope and limitations

- NovaFit and all included brand facts are fictional demonstration data.
- This MVP has no authentication or authorization. Anyone who can access a public deployment may view and change its demo data; do not use it for confidential content.
- It reviews text content only; document/image analysis and automatic publishing are not implemented.
- AI analysis is optional, can be subjective, and may be unavailable. A missing source is not proof that a claim is false.
- The project demonstrates a hackathon workflow and is not a production compliance or legal decision system.

## API

The REST API is under `/api`. Useful routes include `GET /api/health`, `GET /api/dashboard`, brand and guideline endpoints, and submission endpoints for creation, analysis, editing, review, approval, and history. See `backend/src/main/java/com/brandguard/api/BrandGuardController.java` for the implemented routes.
