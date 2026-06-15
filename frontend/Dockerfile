
#Build dell'applicazione React
FROM node:20-alpine AS builder

WORKDIR /app

# Copia package.json e package-lock.json (se esiste)
COPY package*.json ./

# Installa le dipendenze
RUN npm ci --omit=dev

# Copia il codice sorgente
COPY . .

# Build dell'applicazione
RUN npm run build

# Fase 2: Serve con Nginx
FROM nginx:1.25-alpine

# Installa wget per health check
RUN apk add --no-cache wget

# Copia i file buildati dalla fase builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copia la configurazione nginx personalizzata
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Esponi la porta 80
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

# Avvia nginx
CMD ["nginx", "-g", "daemon off;"]