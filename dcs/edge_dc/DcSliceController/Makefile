MY_IP=10.126.1.187
PORT=5000
POST=./Yaml/POST
LOGS=./Logs/logs.log

ordem:
		@echo "ORDEM: user, controller, vim_type, template, host"

teste:
		curl $(MY_IP):$(PORT)

user:     ## User
		@echo 'Adicionando user' \
		&& curl -X POST --data-binary @$(POST)/post_user.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):5000/user

controller: ## Controller
		@echo 'Adicionando controller' \
		&& curl -X POST --data-binary @$(POST)/post_controller.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):5000/controller

host:    ## Host
		@echo "Adcionando host" \
		&& curl -X POST --data-binary @$(POST)/post_host.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):5000/host

##For osm template
osm_vim_type: ## Post vim type
		@echo "Adicionando osm vim type" \
		&& curl -X POST --data-binary @$(POST)/osm-template/post_vim_type.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):5000/vim_type

osm_template: ## Post template osm
		@echo "Adicionando template osm" \
		&& curl -X POST --data-binary @$(POST)/osm-template/post_template.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):5000/template

osm_request:  ## Slice Request osm
		@echo 'Requisitando criação de slice com vim osm' \
		&& curl -X POST --data-binary @$(POST)/osm-template/request_slice_part_osm.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):5000/slice_part/request

#For Kubernetes template
kube_vim_type: ## Post Kubernetes type
		@echo "Adicionando Kubernetes vim type" \
		&& curl -X POST --data-binary @$(POST)/kube-template/post_vim_type.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):5000/vim_type

kube_template: ## Post template Kubernetes
		@echo "Adicionando template Kubernetes" \
		&& curl -X POST --data-binary @$(POST)/kube-template/post_template.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):5000/template
kube_request: ## Slice Request kubenertes
		@echo 'Requisitando criação de slice com vim Kubernetes' \
		&& curl -X POST --data-binary @$(POST)/request_slice_part_kube.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):5000/slice_part/request

logs:        ## Logs
		@echo "Carregando Logs" \
		&& cat $(LOGS)

activate:   ## Ativação da slice
		@echo "Ativando slice" \
		&& curl -X POST --data-binary @$(POST)/activate_slice_part.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):$(PORT)/slice_part/activate

delete:    ## Remoção da Slice
		@echo "Removendo slice" \
                && curl -X DELETE --data-binary @$(POST)/activate_slice_part.yaml -H "Content-type: text/x-yaml" http://$(MY_IP):$(PORT)/slice_part

clear:     ## Limpa logs e banco
		@echo "Limpando logs e banco de dados" \
		&& sudo rm -r Logs/* \
		&& sudo rm -r Database/data/mysqldb/*

