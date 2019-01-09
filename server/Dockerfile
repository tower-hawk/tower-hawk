FROM towerhawk/alpine-java-builder:latest as builder

ARG JAR=*.jar

RUN mkdir -p config/checks plugins

COPY $JAR /build/

COPY /target/plugins/* /build/plugins/

FROM towerhawk/alpine-java-onbuild:8-1.1.0

COPY --from=builder /build/ $APP_HOME/

ENV DEFAULT_JMX_PORT=14295 \
  DEFAULT_JOLOKIA_PORT=24295 \
  OVERRIDES="--towerhawk.monitor.pluginsDir=$APP_HOME/plugins/"

USER $USER:$GROUP
