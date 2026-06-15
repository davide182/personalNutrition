from flask import Flask
from flask_cors import CORS
from routes import register_routes
import os
from dotenv import load_dotenv

# Carica variabili d'ambiente
load_dotenv()

app = Flask(__name__)
CORS(app)

# Registra tutti i blueprint
register_routes(app)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)