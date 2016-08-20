FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/commiteth.jar /commiteth/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/commiteth/app.jar"]
