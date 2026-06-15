from flask import Blueprint, request, jsonify
from services import calculate_bmi, calculate_bmr, calculate_daily_calories, get_bmi_category

bmi_bp = Blueprint('bmi', __name__)

@bmi_bp.route('/api/bmi', methods=['POST'])
def get_bmi():
    data = request.get_json()
    
    weight = data.get('weight')
    height = data.get('height')
    age = data.get('age')
    gender = data.get('gender', 'M')
    activity_level = data.get('activity_level', 'moderate')
    
    if not weight or not height or not age:
        return jsonify({"error": "Peso, altezza ed età obbligatori"}), 400
    
    bmi = calculate_bmi(weight, height)
    bmr = calculate_bmr(weight, height, age, gender)
    daily_calories = calculate_daily_calories(bmr, activity_level)
    
    return jsonify({
        "bmi": bmi,
        "bmr": bmr,
        "daily_calories": daily_calories,
        "bmi_category": get_bmi_category(bmi)
    })