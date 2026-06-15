from flask import Blueprint, request, jsonify
from services import calculate_road_distance

geosort_bp = Blueprint('geosort', __name__)

@geosort_bp.route('/api/geosort', methods=['POST'])
def geosort_nutritionists():
    data = request.get_json()
    
    patient_lat = data.get('patient_lat') or data.get('patientLat')
    patient_lon = data.get('patient_lon') or data.get('patientLon')
    nutritionists = data.get('nutritionists', [])
    
    if not patient_lat or not patient_lon:
        return jsonify({"error": "Coordinate paziente mancanti"}), 400
    
    nutritionists_with_distance = []
    for n in nutritionists:
        lat = n.get('lat') or n.get('latitude')
        lon = n.get('lon') or n.get('longitude')
        
        if lat and lon:
            distance, duration = calculate_road_distance(patient_lat, patient_lon, lat, lon)
            nutritionists_with_distance.append({
                'id': n.get('id'),
                'distance': distance,
                'duration': duration
            })
        else:
            nutritionists_with_distance.append({
                'id': n.get('id'),
                'distance': 999999,
                'duration': None
            })
    
    sorted_list = sorted(nutritionists_with_distance, key=lambda x: x['distance'])
    
    return jsonify({
        'sorted_ids': [item['id'] for item in sorted_list],
        'distances': [item['distance'] for item in sorted_list],
        'durations': [item['duration'] for item in sorted_list]
    })