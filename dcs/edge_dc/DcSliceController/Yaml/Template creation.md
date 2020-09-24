# Kubernetes
Kubernetes template creation
## VM Requeriments:
* OS: Ubuntu server 18.04
* Memory: 2 GB
* Vcpus: 2
* Storage 10 GB
* Hypervisor: XEN/LVM + Libvirt
* SSH with root user allowed
* Network access

## Environment configuration
### Make sure that swap service is disabled
Turn off swap:
```shell
swapoff -a
```
Remove any entrys of swap in '/etc/fstab':
```shell
sudo vim /etc/fstab
```
Check if swap file exists:
```shell
sudo ls -lh /swap.img
```
If exists, disable it:
```shell
sudo swapoff /swap.img
```
Remove swap file:
```shell
sudo rm -f /swap.img
```

### Configure cloud-init package
If the cloud-init package is installed you also need to edit the cloud.cfg file.
To check if the package is installed run the following command:
```shell
ls -l /etc/cloud/cloud.cfg
```
If you see the following output it means that the package is not installed and no further action is required.
```shell
ls: cannot access '/etc/cloud/cloud.cfg': No such file or directory
```
If the package is installed the output will look like the following:
```shell
-rw-r--r-- 1 root root 3169 Apr 27 09:30 /etc/cloud/cloud.cfg
```
In this case you’ll need to open the '/etc/cloud/cloud.cfg' file:
```shell
sudo nano /etc/cloud/cloud.cfg
```
Search for *preserve_hostname* and change the value from *false* to *true*.

### Copy scripts to VM
After finish environment configuration, copy the following files (in this repository) to '/root/' (in your vm):
* Scripts/Kubernetes/install_services.sh
* Scripts/Kubernetes/change_hostname.sh
* Scripts/Kubernetes/install_pod.sh

Set the permissions:
```shell
chmod +x /root/install_services.sh
chmod +x /root/change_hostname.sh
chmod +x /root/install_pod.sh
```
Execute 'install_services.sh' file. If the installation is success the output will look like the following:
```shell
OUTPUT:

Hit:1 http://archive.ubuntu.com/ubuntu bionic InRelease
Get:2 http://archive.ubuntu.com/ubuntu bionic-updates InRelease [88.7 kB]
Get:3 http://archive.ubuntu.com/ubuntu bionic-backports InRelease [74.6 kB]
Get:4 http://archive.ubuntu.com/ubuntu bionic-security InRelease [88.7 kB]
Get:5 http://archive.ubuntu.com/ubuntu bionic-updates/main amd64 Packages [573 kB]
Get:6 http://archive.ubuntu.com/ubuntu bionic-updates/universe amd64 Packages [755 kB]                                                                                                                            
Get:7 http://archive.ubuntu.com/ubuntu bionic-updates/universe Translation-en [200 kB]                                                                                                                            
Fetched 1,780 kB in 7s (247 kB/s)                                                                                                                                                                                 
Reading package lists... Done
Reading package lists... Done
Building dependency tree       
Reading state information... Done
curl is already the newest version (7.58.0-2ubuntu3.6).
The following packages were automatically installed and are no longer required:
  linux-headers-4.15.0-29 linux-headers-4.15.0-29-generic linux-image-4.15.0-29-generic linux-modules-4.15.0-29-generic linux-modules-extra-4.15.0-29-generic
Use 'apt autoremove' to remove them.
The following NEW packages will be installed:
  apt-transport-https
0 upgraded, 1 newly installed, 0 to remove and 119 not upgraded.
Need to get 1,692 B of archives.
After this operation, 153 kB of additional disk space will be used.
Get:1 http://archive.ubuntu.com/ubuntu bionic-updates/universe amd64 apt-transport-https all 1.6.10 [1,692 B]
Fetched 1,692 B in 0s (3,633 B/s)               
Selecting previously unselected package apt-transport-https.
(Reading database ... 137857 files and directories currently installed.)
Preparing to unpack .../apt-transport-https_1.6.10_all.deb ...
Unpacking apt-transport-https (1.6.10) ...
Setting up apt-transport-https (1.6.10) ...
OK
Hit:1 http://archive.ubuntu.com/ubuntu bionic InRelease   
Hit:2 http://archive.ubuntu.com/ubuntu bionic-updates InRelease                     
Get:3 http://archive.ubuntu.com/ubuntu bionic-backports InRelease [74.6 kB]                          
Get:5 http://archive.ubuntu.com/ubuntu bionic-security InRelease [88.7 kB]          
Get:4 https://packages.cloud.google.com/apt kubernetes-xenial InRelease [8,993 B]            
Get:6 https://packages.cloud.google.com/apt kubernetes-xenial/main amd64 Packages [25.0 kB]
Fetched 197 kB in 3s (78.1 kB/s)   
Reading package lists... Done
Reading package lists... Done
Building dependency tree       
Reading state information... Done
The following packages were automatically installed and are no longer required:
  linux-headers-4.15.0-29 linux-headers-4.15.0-29-generic linux-image-4.15.0-29-generic linux-modules-4.15.0-29-generic linux-modules-extra-4.15.0-29-generic
Use 'apt autoremove' to remove them.
The following additional packages will be installed:
  conntrack cri-tools kubernetes-cni socat
The following NEW packages will be installed:
  conntrack cri-tools kubeadm kubectl kubelet kubernetes-cni socat
0 upgraded, 7 newly installed, 0 to remove and 119 not upgraded.
Need to get 50.6 MB of archives.
After this operation, 290 MB of additional disk space will be used.
Get:1 http://archive.ubuntu.com/ubuntu bionic/main amd64 conntrack amd64 1:1.4.4+snapshot20161117-6ubuntu2 [30.6 kB]
Get:2 https://packages.cloud.google.com/apt kubernetes-xenial/main amd64 cri-tools amd64 1.12.0-00 [5,343 kB]
Get:6 http://archive.ubuntu.com/ubuntu bionic/main amd64 socat amd64 1.7.3.2-2ubuntu2 [342 kB]
Get:3 https://packages.cloud.google.com/apt kubernetes-xenial/main amd64 kubernetes-cni amd64 0.7.5-00 [6,473 kB]
Get:4 https://packages.cloud.google.com/apt kubernetes-xenial/main amd64 kubelet amd64 1.14.0-00 [21.5 MB]
Get:5 https://packages.cloud.google.com/apt kubernetes-xenial/main amd64 kubectl amd64 1.14.0-00 [8,801 kB]                                                                                                       
Get:7 https://packages.cloud.google.com/apt kubernetes-xenial/main amd64 kubeadm amd64 1.14.0-00 [8,147 kB]                                                                                                       
Fetched 50.6 MB in 14s (3,618 kB/s)                                                                                                                                                                               
Selecting previously unselected package conntrack.
(Reading database ... 137861 files and directories currently installed.)
Preparing to unpack .../0-conntrack_1%3a1.4.4+snapshot20161117-6ubuntu2_amd64.deb ...
Unpacking conntrack (1:1.4.4+snapshot20161117-6ubuntu2) ...
Selecting previously unselected package cri-tools.
Preparing to unpack .../1-cri-tools_1.12.0-00_amd64.deb ...
Unpacking cri-tools (1.12.0-00) ...
Selecting previously unselected package kubernetes-cni.
Preparing to unpack .../2-kubernetes-cni_0.7.5-00_amd64.deb ...
Unpacking kubernetes-cni (0.7.5-00) ...
Selecting previously unselected package socat.
Preparing to unpack .../3-socat_1.7.3.2-2ubuntu2_amd64.deb ...
Unpacking socat (1.7.3.2-2ubuntu2) ...
Selecting previously unselected package kubelet.
Preparing to unpack .../4-kubelet_1.14.0-00_amd64.deb ...
Unpacking kubelet (1.14.0-00) ...
Selecting previously unselected package kubectl.
Preparing to unpack .../5-kubectl_1.14.0-00_amd64.deb ...
Unpacking kubectl (1.14.0-00) ...
Selecting previously unselected package kubeadm.
Preparing to unpack .../6-kubeadm_1.14.0-00_amd64.deb ...
Unpacking kubeadm (1.14.0-00) ...
Setting up conntrack (1:1.4.4+snapshot20161117-6ubuntu2) ...
Setting up kubernetes-cni (0.7.5-00) ...
Setting up cri-tools (1.12.0-00) ...
Setting up socat (1.7.3.2-2ubuntu2) ...
Setting up kubelet (1.14.0-00) ...
Created symlink /etc/systemd/system/multi-user.target.wants/kubelet.service → /lib/systemd/system/kubelet.service.
Setting up kubectl (1.14.0-00) ...
Processing triggers for man-db (2.8.3-2) ...
Setting up kubeadm (1.14.0-00) ...
kubelet set on hold.
kubeadm set on hold.
kubectl set on hold.
Hit:1 http://archive.ubuntu.com/ubuntu bionic InRelease
Hit:2 http://archive.ubuntu.com/ubuntu bionic-updates InRelease                                    
Get:4 http://archive.ubuntu.com/ubuntu bionic-backports InRelease [74.6 kB]       
Hit:3 https://packages.cloud.google.com/apt kubernetes-xenial InRelease                     
Get:5 http://archive.ubuntu.com/ubuntu bionic-security InRelease [88.7 kB]                  
Fetched 163 kB in 2s (72.7 kB/s)   
Reading package lists... Done
Hit:1 http://archive.ubuntu.com/ubuntu bionic InRelease                  
Hit:3 http://archive.ubuntu.com/ubuntu bionic-updates InRelease                                    
Get:4 http://archive.ubuntu.com/ubuntu bionic-backports InRelease [74.6 kB]                                  
Hit:2 https://packages.cloud.google.com/apt kubernetes-xenial InRelease          
Get:5 http://archive.ubuntu.com/ubuntu bionic-security InRelease [88.7 kB]
Fetched 163 kB in 4s (37.5 kB/s)    
Reading package lists... Done
Reading package lists... Done
Building dependency tree       
Reading state information... Done
ca-certificates is already the newest version (20180409).
curl is already the newest version (7.58.0-2ubuntu3.6).
apt-transport-https is already the newest version (1.6.10).
The following packages were automatically installed and are no longer required:
  linux-headers-4.15.0-29 linux-headers-4.15.0-29-generic linux-image-4.15.0-29-generic linux-modules-4.15.0-29-generic linux-modules-extra-4.15.0-29-generic
Use 'apt autoremove' to remove them.
The following additional packages will be installed:
  python3-software-properties
The following packages will be upgraded:
  python3-software-properties software-properties-common
2 upgraded, 0 newly installed, 0 to remove and 117 not upgraded.
Need to get 31.9 kB of archives.
After this operation, 0 B of additional disk space will be used.
Do you want to continue? [Y/n] Y
Get:1 http://archive.ubuntu.com/ubuntu bionic-updates/main amd64 software-properties-common all 0.96.24.32.7 [9,908 B]
Get:2 http://archive.ubuntu.com/ubuntu bionic-updates/main amd64 python3-software-properties all 0.96.24.32.7 [22.0 kB]
Fetched 31.9 kB in 1s (33.6 kB/s)                 
(Reading database ... 137932 files and directories currently installed.)
Preparing to unpack .../software-properties-common_0.96.24.32.7_all.deb ...
Unpacking software-properties-common (0.96.24.32.7) over (0.96.24.32.4) ...
Preparing to unpack .../python3-software-properties_0.96.24.32.7_all.deb ...
Unpacking python3-software-properties (0.96.24.32.7) over (0.96.24.32.4) ...
Processing triggers for man-db (2.8.3-2) ...
Setting up python3-software-properties (0.96.24.32.7) ...
Processing triggers for dbus (1.12.2-1ubuntu1) ...
Setting up software-properties-common (0.96.24.32.7) ...
OK
Get:1 https://download.docker.com/linux/ubuntu bionic InRelease [64.4 kB]
Hit:2 http://archive.ubuntu.com/ubuntu bionic InRelease                                               
Get:3 https://download.docker.com/linux/ubuntu bionic/stable amd64 Packages [5,673 B]                                    
Hit:4 http://archive.ubuntu.com/ubuntu bionic-updates InRelease                                                                      
Get:5 http://archive.ubuntu.com/ubuntu bionic-backports InRelease [74.6 kB]                         
Hit:6 https://packages.cloud.google.com/apt kubernetes-xenial InRelease             
Get:7 http://archive.ubuntu.com/ubuntu bionic-security InRelease [88.7 kB]
Fetched 233 kB in 5s (43.6 kB/s)    
Reading package lists... Done
Hit:1 https://download.docker.com/linux/ubuntu bionic InRelease
Hit:2 http://archive.ubuntu.com/ubuntu bionic InRelease                                             
Hit:3 http://archive.ubuntu.com/ubuntu bionic-updates InRelease                                    
Get:5 http://archive.ubuntu.com/ubuntu bionic-backports InRelease [74.6 kB]      
Hit:4 https://packages.cloud.google.com/apt kubernetes-xenial InRelease                     
Get:6 http://archive.ubuntu.com/ubuntu bionic-security InRelease [88.7 kB]                  
Fetched 163 kB in 3s (63.3 kB/s)   
Reading package lists... Done
Reading package lists... Done
Building dependency tree       
Reading state information... Done
The following packages were automatically installed and are no longer required:
  linux-headers-4.15.0-29 linux-headers-4.15.0-29-generic linux-image-4.15.0-29-generic linux-modules-4.15.0-29-generic linux-modules-extra-4.15.0-29-generic
Use 'apt autoremove' to remove them.
The following additional packages will be installed:
  aufs-tools cgroupfs-mount libltdl7 pigz
The following NEW packages will be installed:
  aufs-tools cgroupfs-mount docker-ce libltdl7 pigz
0 upgraded, 5 newly installed, 0 to remove and 117 not upgraded.
Need to get 40.3 MB of archives.
After this operation, 198 MB of additional disk space will be used.
Do you want to continue? [Y/n] Y
Get:1 https://download.docker.com/linux/ubuntu bionic/stable amd64 docker-ce amd64 18.06.0~ce~3-0~ubuntu [40.1 MB]
Get:2 http://archive.ubuntu.com/ubuntu bionic/universe amd64 pigz amd64 2.4-1 [57.4 kB]
Get:3 http://archive.ubuntu.com/ubuntu bionic/universe amd64 aufs-tools amd64 1:4.9+20170918-1ubuntu1 [104 kB]
Get:4 http://archive.ubuntu.com/ubuntu bionic/universe amd64 cgroupfs-mount all 1.4 [6,320 B]
Get:5 http://archive.ubuntu.com/ubuntu bionic/main amd64 libltdl7 amd64 2.4.6-2 [38.8 kB]
Fetched 40.3 MB in 8s (4,781 kB/s)                                                                                                                                                                                
Selecting previously unselected package pigz.
(Reading database ... 137932 files and directories currently installed.)
Preparing to unpack .../archives/pigz_2.4-1_amd64.deb ...
Unpacking pigz (2.4-1) ...
Selecting previously unselected package aufs-tools.
Preparing to unpack .../aufs-tools_1%3a4.9+20170918-1ubuntu1_amd64.deb ...
Unpacking aufs-tools (1:4.9+20170918-1ubuntu1) ...
Selecting previously unselected package cgroupfs-mount.
Preparing to unpack .../cgroupfs-mount_1.4_all.deb ...
Unpacking cgroupfs-mount (1.4) ...
Selecting previously unselected package libltdl7:amd64.
Preparing to unpack .../libltdl7_2.4.6-2_amd64.deb ...
Unpacking libltdl7:amd64 (2.4.6-2) ...
Selecting previously unselected package docker-ce.
Preparing to unpack .../docker-ce_18.06.0~ce~3-0~ubuntu_amd64.deb ...
Unpacking docker-ce (18.06.0~ce~3-0~ubuntu) ...
Setting up aufs-tools (1:4.9+20170918-1ubuntu1) ...
Processing triggers for ureadahead (0.100.0-20) ...
Setting up cgroupfs-mount (1.4) ...
Processing triggers for libc-bin (2.27-3ubuntu1) ...
Processing triggers for systemd (237-3ubuntu10.13) ...
Setting up libltdl7:amd64 (2.4.6-2) ...
Processing triggers for man-db (2.8.3-2) ...
Setting up pigz (2.4-1) ...
Setting up docker-ce (18.06.0~ce~3-0~ubuntu) ...
Created symlink /etc/systemd/system/multi-user.target.wants/docker.service → /lib/systemd/system/docker.service.
Created symlink /etc/systemd/system/sockets.target.wants/docker.socket → /lib/systemd/system/docker.socket.
Processing triggers for ureadahead (0.100.0-20) ...
Processing triggers for libc-bin (2.27-3ubuntu1) ...
Processing triggers for systemd (237-3ubuntu10.13) ...
```
Now you can remove the '/root/install_services.sh' file.

### Configure hostname
Change the hostname to 'kube-template':
```shell
sudo hostnamectl set-hostname kube-template
```
Add "kube-template" to the end of the first line of the '/etc/hosts' file.
Example of first line:
```shell
127.0.0.1	localhost.localdomain	localhost kube-template
```

### Configure network
To properly configure the vm's interfaces you must copy the 'Scripts/Kubernetes/01-netcfg.yaml' file (in this repository) to '/etc/netplan/' (in your vm). In addition, you must replace *VM_IP* and *VM_NETMASK* for your Controll IP and Netmask. This IP will be used to access the Kubernetes VMs and make all configurations that is needed to provide the Slice Part.
Example of configured file:
```shell
network:
    version: 2
    renderer: networkd
    ethernets:
      eth0:
        dhcp4: no
        #addresses: [10.10.10.60/16]
      eth1:
        dhcp4: true
        gateway4: <br-control_address>
        nameservers:
          addresses: [8.8.8.8,8.8.4.4]
```
Then, apply changes:
```shell
sudo netplan apply
```

### Remove network interfaces
After shutdown VM, you must remove the vm's interfaces.
In your XEN host, open your vm's configuration file:
```shell
virsh edit VMNAME
```
Remove all interface's tags and save the file. An interface's tag look like the following:
```xml
  <interface type='bridge'>
    <mac address='00:16:3e:47:d8:80'/>
    <source bridge='br-control'/>
  </interface>
```
### Finish
Kubernetes template is done. Now, you must register this VM as an template using the DC Slice Controller API. Make sure of set the 'path' attribute as the vm's name in the 'Yaml/post_tamplate.yaml' file. 
