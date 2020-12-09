FROM adoptopenjdk:11-jre-hotspot

LABEL maintainer="Anton Zering (synthomat)"

COPY target/geoscope-standalone.jar /
EXPOSE 3000/tcp

CMD ["java", "-jar", "/geoscope-standalone.jar"]
