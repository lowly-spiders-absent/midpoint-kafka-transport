FROM evolveum/midpoint:4.7-alpine

# replace the jar file
COPY target/midpoint.jar /opt/midpoint/lib/
