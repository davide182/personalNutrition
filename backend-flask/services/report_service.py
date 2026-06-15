from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
from reportlab.lib.units import cm
import io
import base64

def generate_pdf_report(patient_name, weight, height, age, gender, diagnosis, recommendations):
    """Genera report nutrizionale in PDF e restituisce base64"""
    from .bmi_service import calculate_bmi, get_bmi_category
    
    bmi = calculate_bmi(weight, height)
    bmi_category = get_bmi_category(bmi)
    
    buffer = io.BytesIO()
    c = canvas.Canvas(buffer, pagesize=A4)
    width, height_page = A4
    
    # Header
    c.setFont("Helvetica-Bold", 18)
    c.drawString(2*cm, height_page - 2*cm, "Piano Nutrizionale")
    
    c.setFont("Helvetica", 12)
    c.drawString(2*cm, height_page - 4*cm, f"Paziente: {patient_name}")
    c.drawString(2*cm, height_page - 5*cm, f"Età: {age} anni")
    c.drawString(2*cm, height_page - 6*cm, f"Sesso: {'Maschile' if gender == 'M' else 'Femminile'}")
    
    # Dati antropometrici
    c.setFont("Helvetica-Bold", 14)
    c.drawString(2*cm, height_page - 8*cm, "Dati Antropometrici")
    
    c.setFont("Helvetica", 12)
    c.drawString(2*cm, height_page - 9*cm, f"Peso: {weight} kg")
    c.drawString(2*cm, height_page - 10*cm, f"Altezza: {height} cm")
    c.drawString(2*cm, height_page - 11*cm, f"BMI: {bmi} ({bmi_category})")
    
    # Diagnosi
    if diagnosis:
        c.setFont("Helvetica-Bold", 14)
        c.drawString(2*cm, height_page - 13*cm, "Diagnosi")
        c.setFont("Helvetica", 12)
        y = height_page - 14*cm
        for line in diagnosis.split('\n')[:5]:
            c.drawString(2*cm, y, line[:80])
            y -= 0.5*cm
    
    # Raccomandazioni
    if recommendations:
        c.setFont("Helvetica-Bold", 14)
        y = height_page - 18*cm
        c.drawString(2*cm, y, "Raccomandazioni")
        c.setFont("Helvetica", 12)
        y -= 1*cm
        for line in recommendations.split('\n')[:10]:
            if y < 3*cm:
                c.showPage()
                y = height_page - 2*cm
            c.drawString(2*cm, y, line[:80])
            y -= 0.5*cm
    
    # Footer
    c.setFont("Helvetica", 10)
    c.drawString(2*cm, 2*cm, "Documento generato automaticamente da Nutritionists App")
    
    c.save()
    buffer.seek(0)
    return base64.b64encode(buffer.getvalue()).decode('utf-8')