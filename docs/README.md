# Nutritionists Platform

## Descrizione del Progetto

Nutritionists Platform è un'applicazione web completa per la gestione di servizi nutrizionali. La piattaforma mette in relazione pazienti e nutrizionisti, permettendo la prenotazione di appuntamenti, la gestione di piani nutrizionali personalizzati e il calcolo di parametri biometrici.

## Tecnologie Utilizzate

### Backend

- Java 21 con Spring Boot
- Spring Security per autenticazione e autorizzazione
- JWT per la gestione dei token
- Spring Data JPA per l'accesso al database
- Hibernate come ORM
- PostgreSQL come database principale
- MongoDB per il logging di sistema

### Servizio Flask

- Python 3.11
- Flask per API di geolocalizzazione
- Calcolo distanze stradali con OSRM
- Calcolo BMI, BMR e fabbisogno calorico
- Generazione report PDF con ReportLab

### Frontend

- React 18 con TypeScript
- Vite come build tool
- Tailwind CSS per lo styling
- React Router per la navigazione
- React Leaflet per mappe interattive
- Axios per chiamate API
- shadcn/ui per componenti UI

### DevOps & Deployment

- Docker e Docker Compose per containerizzazione
- GitHub Actions per CI/CD
- Jenkins (opzionale) per pipeline alternative

## Struttura delle Cartelle


nutritionists-platform/
├── backend-spring/                 # Backend Spring Boot
│   ├── src/main/java/             # Codice sorgente Java
│   │   ├── controller/            # REST controller
│   │   ├── service/               # Logica di business
│   │   ├── repository/            # Accesso ai dati
│   │   ├── model/entity/          # Entita JPA
│   │   ├── model/dto/             # Data Transfer Object
│   │   ├── security/              # JWT e autenticazione
│   │   └── config/                # Configurazioni
│   ├── src/main/resources/        # File di configurazione
│   └── pom.xml                    # Dipendenze Maven
│
├── backend-flask/                  # Servizio Flask
│   ├── routes/                    # Blueprint Flask
│   ├── services/                  # Logica di business
│   ├── app.py                     # Entry point
│   └── requirements.txt           # Dipendenze Python
│
├── frontend-react/                 # Frontend React
│   ├── src/
│   │   ├── components/            # Componenti UI
│   │   ├── pages/                 # Pagine dell'applicazione
│   │   ├── context/               # Context API (Auth)
│   │   ├── api/                   # Configurazione Axios
│   │   └── types/                 # TypeScript interfacce
│   ├── package.json               # Dipendenze Node
│   └── vite.config.ts             # Configurazione Vite
│
├── .github/workflows/              # GitHub Actions
│   ├── ci.yml                     # Continuous Integration
│   └── cd-demo.yml                # Continuous Deployment demo
│
├── scripts/
│   └── local-deploy.sh            # Script deploy locale
│
├── docker-compose.yml             # Orchestrazione container
├── docker-compose.prod.yml        # Produzione
├── Jenkinsfile                    # Pipeline Jenkins
└── .env.example                   # Template variabili ambiente


## Funzionalità Principali

### Per tutti gli utenti

- Registrazione e login con JWT
- Recupero password tramite email
- Visualizzazione profilo

### Pazienti

- Ricerca nutrizionisti su mappa interattiva
- Prenotazione appuntamenti (specifico o automatico)
- Gestione appuntamenti
- Inserimento dati salute (peso, altezza, allergie, obiettivi)
- Calcolo BMI e fabbisogno calorico
- Visualizzazione piani nutrizionali

### Nutrizionisti

- Gestione orari di lavoro
- Ricezione e gestione richieste appuntamento
- Accettazione/rifiuto proposte con definizione prezzo
- Gestione appuntamenti confermati
- Creazione piani nutrizionali personalizzati
- Generazione report PDF

### Amministratori

- Approvazione/rifiuto registrazioni nutrizionisti
- Sospensione/riattivazione utenti
- Gestione richieste disabilitazione profili
- Visualizzazione log di sistema
- Gestione specializzazioni

## Ruoli e Permessi

| Funzionalita | Paziente | Nutrizionista | Admin |
|--------------|----------|---------------|-------|
| Registrazione | Si | Si (attesa approvazione) | No |
| Login | Si | Si | Si |
| Ricerca nutrizionisti | Si | No | No |
| Prenotazione appuntamenti | Si | No | No |
| Gestione orari | No | Si | No |
| Accettazione proposte | No | Si | No |
| Piani nutrizionali | Visualizza | Crea/Modifica | No |
| Approvazione utenti | No | No | Si |
| Log di sistema | No | No | Si |

## Installazione e Avvio

### Prerequisiti

- Docker e Docker Compose
- Java 21 (per sviluppo)
- Node.js 20 (per sviluppo frontend)
- Python 3.11 (per sviluppo Flask)
- PostgreSQL 16
- MongoDB 7

## Credenziali di Default

| Ruolo | Email | Password |
|-------|-------|----------|
| Admin | admin@nutritionists.it | admin123 |

## API Endpoints Principali

### Auth

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| POST | /api/auth/register | Registrazione utente |
| POST | /api/auth/login | Login utente |
| POST | /api/auth/forgot-password | Richiesta reset password |
| POST | /api/auth/reset-password | Reset password |

### Paziente

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | /api/patient/nutritionists/nearby | Ricerca nutrizionisti vicini |
| POST | /api/patient/appointments | Prenotazione appuntamento |
| GET | /api/patient/appointments | Lista appuntamenti |
| GET | /api/patient/health-data | Dati salute |
| PUT | /api/patient/health-data | Aggiorna dati salute |
| GET | /api/patient/bmi | Calcolo BMI |

### Nutrizionista

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | /api/nutritionist/proposals | Richieste in arrivo |
| PUT | /api/nutritionist/proposals/{id}/accept | Accetta proposta |
| PUT | /api/nutritionist/proposals/{id}/reject | Rifiuta proposta |
| POST | /api/nutritionist/schedule | Aggiungi orario |
| GET | /api/nutritionist/schedule | Orari di lavoro |
| POST | /api/nutritionist/nutritional-plans | Crea piano nutrizionale |

### Admin

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | /api/admin/pending-nutritionists | Nutrizionisti in attesa |
| PUT | /api/admin/approve-nutritionist/{id} | Approva nutrizionista |
| PUT | /api/admin/suspend-user/{id} | Sospendi utente |
| GET | /api/admin/logs | Log di sistema |
| GET | /api/admin/appointments | Tutti gli appuntamenti |

## CI/CD Pipeline

Il progetto implementa una pipeline CI/CD completa:

### Continuous Integration (GitHub Actions)

- Esecuzione test automatici su push e pull request
- Build delle immagini Docker
- Verifica della corretta compilazione

### Continuous Deployment

- Build e push delle immagini su container registry
- Deploy automatico su server di produzione
- Health check post-deploy

### Deploy Locale

Lo script scripts/local-deploy.sh automatizza:

- Build delle immagini Docker
- Avvio di tutti i container
- Health check dei servizi

## Database

### PostgreSQL (dati persistenti)

- users - utenti del sistema
- user_profiles - profili utente
- nutritionists - dati nutrizionisti
- appointments - appuntamenti
- nutritional_plans - piani nutrizionali
- work_schedules - orari di lavoro
- specializations - specializzazioni

### MongoDB (logging)

- system_logs - log di sistema
- notification_logs - log notifiche email

## Sicurezza

- Password criptate con BCrypt
- Autenticazione stateless con JWT
- Ruoli e permessi con Spring Security
- Validazione input con Bean Validation
- Protezione endpoint con @PreAuthorize

## Stato degli Account

| Stato | Descrizione |
|-------|-------------|
| ACTIVE | Account attivo |
| PENDING | In attesa di approvazione (nutrizionisti) |
| SUSPENDED | Sospeso (in attesa di revisione) |
| SELF_DISABLED | Disabilitato volontariamente |
| DISABLED | Disabilitato permanentemente |

## Test eseguiti

- AdminServiceTest (10 test)
- AuthServiceTest (9 test)
- AppointmentProposalServiceTest (4 test)
- DisableProfileServiceTest (8 test)
- EmailServiceTest (6 test)
- GeocodingServiceTest (4 test)
- PasswordResetServiceTest (8 test)