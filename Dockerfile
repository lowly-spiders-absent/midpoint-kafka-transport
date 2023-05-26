FROM evolveum/midpoint:4.7-alpine

RUN mkdir -p /opt/midpoint/var/lib/
COPY container-files/setenv.sh /opt/midpoint/var/
COPY target/midpoint-kafka-transport-jar-with-dependencies.jar /opt/midpoint/var/lib/mkc.jar
