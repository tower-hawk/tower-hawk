FROM anapsix/alpine-java:8_server-jre_unlimited
VOLUME /tmp
ADD target/towerhawk-0.0.1-SNAPSHOT.jar /app/towerhawk/app.jar
ADD entrypoint.sh /
EXPOSE 4295
ENTRYPOINT [ "/bin/bash", "entrypoint.sh" ]
