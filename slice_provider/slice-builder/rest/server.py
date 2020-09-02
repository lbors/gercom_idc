from flask import Flask
from rest.slice_builder import slice_builder

app = Flask(__name__)
    
app.register_blueprint(slice_builder)

app.run(host='0.0.0.0', port=5003, debug=True)