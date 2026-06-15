from .health_routes import health_bp
from .distance_routes import distance_bp
from .geosort_routes import geosort_bp
from .bmi_routes import bmi_bp
from .report_routes import report_bp

def register_routes(app):
    app.register_blueprint(health_bp)
    app.register_blueprint(distance_bp)
    app.register_blueprint(geosort_bp)
    app.register_blueprint(bmi_bp)
    app.register_blueprint(report_bp)