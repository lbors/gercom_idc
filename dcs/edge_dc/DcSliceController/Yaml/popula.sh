#!/bin/bash
MY_IP="10.126.1.187"

curl -X POST --data-binary @POST/post_user.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/user

curl -X POST --data-binary @POST/post_controller.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/controller

curl -X POST --data-binary @POST/post_host.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/host

#For osm template
curl -X POST --data-binary @POST/osm-template/post_vim_type.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/vim_type
curl -X POST --data-binary @POST/osm-template/post_template.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/template
#Slice Request osm
# curl -X POST --data-binary @POST/osm-template/request_slice_part_osm.yaml -H "Content-type: text/x-yaml" http://10.126.1.187:5000/slice_part/request


#For Kubernetes template
curl -X POST --data-binary @POST/kube-template/post_vim_type.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/vim_type
curl -X POST --data-binary @POST/kube-template/post_template.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/template
# Slice Request kubenertes
# curl -X POST --data-binary @POST/request_slice_part_kube.yaml -H "Content-type: text/x-yaml" http://10.126.1.187:5000/slice_part/request

