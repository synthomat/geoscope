LABEL maintainer="Anton Zering (synthomat)"

FROM adoptopenjdk:11-jre-hotspot
COPY target/*-standalone.jar /geoscope-standalone.jar
EXPOSE 3000/tcp

CMD ["java", "-jar", "/geoscope-standalone.jar"]
