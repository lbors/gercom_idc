from flask import Flask
from rest.service_orchestrator_adaptor import soa

app = Flask(__name__)
    
app.register_blueprint(soa)

app.run(host='0.0.0.0', port=5011, debug=True)
