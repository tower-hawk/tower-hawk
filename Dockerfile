FROM anapsix/alpine-java:8_server-jre_unlimited

ENV APP_HOME=/app/towerhawk UID=507 GID=507 GROUP=towerhawk USER=towerhawk

VOLUME /tmp

ADD target/towerhawk-0.0.1-SNAPSHOT.jar $APP_HOME/app.jar
ADD entrypoint.sh $APP_HOME/

RUN addgroup -g $GID $GROUP \
  && adduser -u $UID -h $APP_HOME -s /bin/sh -D -H -G $GROUP $USER \
  && chown -R $USER:$GROUP /app $APP_HOME

EXPOSE 4295

USER $USER:$GROUP

WORKDIR $APP_HOME

ENTRYPOINT [ "/bin/bash", "entrypoint.sh" ]
