start=$SECONDS

sudo kubeadm init --pod-network-cidr=192.168.0.0/16 --apiserver-advertise-address=MYIP

mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

kubectl apply -f https://docs.projectcalico.org/v3.5/getting-started/kubernetes/installation/hosted/etcd.yaml

kubectl apply -f https://docs.projectcalico.org/v3.5/getting-started/kubernetes/installation/hosted/calico.yaml

duration=$(( SECONDS - start ))
echo "executed in $duration seconds"
