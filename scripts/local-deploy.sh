#!/bin/bash
# scripts/local-deploy.sh
# Versione per PROGETTO SCOLASTICO - funziona in locale

echo "========================================="
echo "🚀 NUTRITIONISTS - LOCAL DEPLOY"
echo "========================================="

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Verifica che siamo nella directory giusta
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}❌ docker-compose.yml non trovato!${NC}"
    echo "Esegui questo script dalla cartella ROOT del progetto"
    exit 1
fi

# Carica variabili d'ambiente
if [ -f ".env" ]; then
    echo -e "${BLUE}📄 Caricamento .env...${NC}"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo -e "${YELLOW}⚠️ .env non trovato, uso default${NC}"
    cp .env.example .env
fi

# Step 1: Build delle immagini Docker
echo ""
echo -e "${BLUE}📦 Step 1/4: Build immagini Docker...${NC}"
docker-compose build --parallel

# Step 2: Ferma i container esistenti
echo ""
echo -e "${BLUE}🛑 Step 2/4: Fermo container esistenti...${NC}"
docker-compose down

# Step 3: Avvia i container
echo ""
echo -e "${BLUE}▶️ Step 3/4: Avvio container...${NC}"
docker-compose up -d

# Step 4: Health check
echo ""
echo -e "${BLUE}🏥 Step 4/4: Health check...${NC}"
sleep 10

# Verifica Spring Boot
echo -n "   Spring Boot (porta 8080): "
if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ OK${NC}"
else
    echo -e "${RED}❌ NON RISPONDE${NC}"
fi

# Verifica Flask
echo -n "   Flask (porta 5000): "
if curl -s http://localhost:5000/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ OK${NC}"
else
    echo -e "${RED}❌ NON RISPONDE${NC}"
fi

# Verifica Frontend
echo -n "   Frontend (porta 80): "
if curl -s http://localhost > /dev/null 2>&1; then
    echo -e "${GREEN}✅ OK${NC}"
else
    echo -e "${RED}❌ NON RISPONDE${NC}"
fi

# Riepilogo finale
echo ""
echo "========================================="
echo -e "${GREEN}✅ DEPLOY COMPLETATO!${NC}"
echo "========================================="
echo ""
echo "📍 Servizi disponibili su localhost:"
echo "   🌐 Frontend:    http://localhost"
echo "   🔧 API Spring:  http://localhost:8080/api"
echo "   🐍 API Flask:   http://localhost:5000"
echo ""
echo "📋 Comandi utili:"
echo "   docker-compose logs -f     # Vedi log"
echo "   docker-compose down        # Ferma tutto"
echo "   docker-compose restart     # Riavvia"
echo "========================================="