FROM openjdk:17
MAINTAINER hf
WORKDIR /
ADD build/libs/hfai-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar"]
CMD ["app.jar"]

# @docker 检测代码
RUN echo "Checking code using Docker"