FROM azul/zulu-openjdk-centos:8-latest

COPY mci-api/build/distributions/mci-*.noarch.rpm /tmp/mci.rpm
RUN yum install -y /tmp/mci.rpm && rm -f /tmp/mci.rpm && yum clean all
COPY env/docker_mci /etc/default/mci
ENTRYPOINT . /etc/default/mci && java -jar /opt/mci/lib/mci-schema-*.jar && java -Dserver.port=$MCI_PORT -DMCI_LOG_LEVEL=$MCI_LOG_LEVEL -jar  /opt/mci/lib/mci-api.war

