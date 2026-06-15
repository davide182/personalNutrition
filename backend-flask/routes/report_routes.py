from flask import Blueprint, request, jsonify
from services import generate_pdf_report

report_bp = Blueprint('report', __name__)

@report_bp.route('/api/nutritional-report', methods=['POST'])
def generate_report():
    data = request.get_json()
    
    patient_name = data.get('patient_name')
    weight = data.get('weight')
    height = data.get('height')
    age = data.get('age')
    gender = data.get('gender', 'M')
    diagnosis = data.get('diagnosis', '')
    recommendations = data.get('recommendations', '')
    
    if not patient_name or not weight or not height or not age:
        return jsonify({"error": "Nome paziente, peso, altezza ed età obbligatori"}), 400
    
    pdf_base64 = generate_pdf_report(patient_name, weight, height, age, gender, diagnosis, recommendations)
    
    return jsonify({
        "pdf_base64": pdf_base64,
        "filename": f"piano_nutrizionale_{patient_name.replace(' ', '_')}.pdf"
    })