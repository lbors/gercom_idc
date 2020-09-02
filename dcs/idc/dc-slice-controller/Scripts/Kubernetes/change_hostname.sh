start=$SECONDS

hostnamectl set-hostname MYHOSTNAME
sed -i "s/kube-template/MYHOSTNAME/g" /etc/hosts

duration=$(( SECONDS - start ))
echo "executed in $duration seconds"
