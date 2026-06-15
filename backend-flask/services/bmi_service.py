def calculate_bmi(weight, height):
    """Calcola BMI (peso in kg, altezza in cm)"""
    height_m = height / 100
    bmi = weight / (height_m ** 2)
    return round(bmi, 2)

def calculate_bmr(weight, height, age, gender):
    """Calcola BMR con formula Harris-Benedict"""
    if gender == 'M':
        bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
    else:
        bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
    return round(bmr, 2)

def calculate_daily_calories(bmr, activity_level):
    """Calcola fabbisogno calorico giornaliero"""
    activity_factors = {
        'sedentary': 1.2,
        'light': 1.375,
        'moderate': 1.55,
        'active': 1.725,
        'very_active': 1.9
    }
    factor = activity_factors.get(activity_level, 1.2)
    return round(bmr * factor, 2)

def get_bmi_category(bmi):
    if bmi < 18.5:
        return "Sottopeso"
    elif bmi < 25:
        return "Normopeso"
    elif bmi < 30:
        return "Sovrappeso"
    else:
        return "Obesità"