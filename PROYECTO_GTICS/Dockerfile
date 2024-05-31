FROM openjdk:17.0.2-jdk
VOLUME /tmp
EXPOSE 8080
ADD ./target/PROYECTO_GTICS-0.0.1-SNAPSHOT.jar proyecto.jar
ENTRYPOINT ["java","-jar","proyecto.jar"]