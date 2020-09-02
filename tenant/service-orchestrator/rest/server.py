from flask import Flask
from rest.service_orchestrator import service_orchestrator

app = Flask(__name__)
    
app.register_blueprint(service_orchestrator)

app.run(host='0.0.0.0', port=5010, debug=True)
