FROM openjdk:17
MAINTAINER hf
WORKDIR /
ADD target/hfai.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar"]
CMD ["app.jar"]

# @docker 检测代码
RUN echo "Checking code using Docker"