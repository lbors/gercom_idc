#!/bin/bash
MY_IP="10.126.1.32"

curl -X POST --data-binary @Yaml/POST/post_user.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/user

curl -X POST --data-binary @Yaml/POST/post_controller.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/controller

curl -X POST --data-binary @Yaml/POST/post_vim_type.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/vim_type

curl -X POST --data-binary @Yaml/POST/post_template.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/template

curl -X POST --data-binary @Yaml/POST/post_host.yaml -H "Content-type: text/x-yaml" http://"$MY_IP":5000/host

