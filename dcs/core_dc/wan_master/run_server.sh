cd /mnt/src/

echo "Loading..."
gunicorn api:app  -b 0.0.0.0:8080