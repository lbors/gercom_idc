
# Deployment of Netdata Cluster

sudo snap install helm --classic
sudo helm init
sleep 60
git clone https://github.com/netdata/helmchart.git netdata
sudo kubectl create serviceaccount --namespace kube-system tiller
sudo kubectl create clusterrolebinding tiller-cluster-rule --clusterrole=cluster-admin --serviceaccount=kube-system:tiller

sudo kubectl patch deploy --namespace kube-system tiller-deploy -p '{"spec":{"template":{"spec":{"serviceAccount":"tiller"}}}}'
sleep 90
sudo helm install --name my-release ./netdata --set master.database.persistence=false --set slave.database.persistence=false -f valuesNECOS.yml
sleep 60
sudo kubectl port-forward --address 0.0.0.0 netdata-master-0 20000:19999 &
