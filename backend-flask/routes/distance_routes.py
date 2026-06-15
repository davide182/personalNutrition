from flask import Blueprint, request, jsonify
from services import calculate_road_distance

distance_bp = Blueprint('distance', __name__)

@distance_bp.route('/api/distance', methods=['POST'])
def get_distance():
    data = request.get_json()
    origin = data.get('origin')
    destination = data.get('destination')
    
    if not origin or not destination:
        return jsonify({"error": "Coordinate origine e destinazione obbligatorie"}), 400
    
    distance, duration = calculate_road_distance(
        origin['lat'], origin['lon'],
        destination['lat'], destination['lon']
    )
    
    return jsonify({
        "distance_km": distance,
        "duration_min": duration if duration else None,
        "source": "osrm" if duration else "haversine"
    })