
# Deployment of Prometheus Cluster

kubectl apply -f /root/prometheus_instalation.yaml >> prometheus.log
sleep 5
kubectl port-forward --address 0.0.0.0 $(kubectl get pod -l app=prometheus --field-selector=status.phase=Running -o name -n monitoring) -n monitoring 10800:9090 &