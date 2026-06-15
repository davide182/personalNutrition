import math
import requests

def calculate_road_distance(lat1, lon1, lat2, lon2):
    """
    Calcola la distanza su strada usando il servizio pubblico OSRM.
    In caso di errore, usa la distanza in linea d'aria (Haversine).
    """
    url = f"http://router.project-osrm.org/route/v1/driving/{lon1},{lat1};{lon2},{lat2}?overview=false"
    
    try:
        response = requests.get(url, timeout=5)
        if response.status_code == 200:
            data = response.json()
            if data['code'] == 'Ok':
                distance_meters = data['routes'][0]['distance']
                distance_km = round(distance_meters / 1000, 2)
                duration_seconds = data['routes'][0]['duration']
                duration_min = round(duration_seconds / 60, 2)
                print(f"OSRM -> Distanza: {distance_km} km, Durata: {duration_min} min")
                return distance_km, duration_min
        else:
            print(f"Errore OSRM: Status {response.status_code}")
    except Exception as e:
        print(f"Errore di connessione a OSRM: {e}")

    print("Attenzione: Fallback a Haversine (linea d'aria).")
    distance_km = calculate_haversine_distance(lat1, lon1, lat2, lon2)
    return distance_km, None

def calculate_haversine_distance(lat1, lon1, lat2, lon2):
    """Calcola distanza in linea d'aria (km)"""
    R = 6371
    lat1_rad = math.radians(lat1)
    lat2_rad = math.radians(lat2)
    delta_lat = math.radians(lat2 - lat1)
    delta_lon = math.radians(lon2 - lon1)
    
    a = math.sin(delta_lat / 2) ** 2 + \
        math.cos(lat1_rad) * math.cos(lat2_rad) * \
        math.sin(delta_lon / 2) ** 2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    
    return round(R * c, 2)