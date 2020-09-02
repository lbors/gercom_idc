FROM ubuntu:18.04 AS SRO
ENV DEBIAN_FRONTEND noninteractive
RUN apt update && apt install python3 python3-pip libyaml-dev libpython3-dev wget -y 
RUN pip3 install flask requests pyyaml
RUN mkdir /home/SRO && cd /home/SRO && mkdir generated received logs
COPY config.py /home/SRO/config.py
COPY sro-rest.py /home/SRO/sro-rest.py
COPY slice_resource_orchestrator.py /home/SRO/slice_resource_orchestrator.py
COPY elasticity.py /home/SRO/elasticity.py
COPY sro_logger.py /home/SRO/sro_logger.py
WORKDIR /home/SRO

FROM ubuntu:18.04 AS SDB
ENV DEBIAN_FRONTEND noninteractive
RUN apt update && apt install python3 python3-pip libyaml-dev libpython3-dev wget -y 
RUN pip3 install flask requests pyyaml neo4j
RUN mkdir /home/SDB && mkdir /home/SDB/logs
COPY config.py /home/SDB/config.py
COPY clean_slices_db.py /home/SDB/clean_slices_db.py
COPY sdb-rest.py /home/SDB/sdb-rest.py
COPY slices_database.py /home/SDB/slices_database.py
COPY db_tools.py /home/SDB/db_tools.py
COPY sro_logger.py /home/SDB/sro_logger.py
WORKDIR /home/SDB