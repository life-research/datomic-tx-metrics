FROM openjdk:8u222-jre

ENV DATOMIC_VERSION 0.9.5966

ARG DATOMIC_ACC_USER
ARG DATOMIC_ACC_PASS

RUN wget -q --http-user=${DATOMIC_ACC_USER} --http-password=${DATOMIC_ACC_PASS} https://my.datomic.com/repo/com/datomic/datomic-pro/$DATOMIC_VERSION/datomic-pro-$DATOMIC_VERSION.zip -O datomic-pro-$DATOMIC_VERSION.zip
RUN unzip -q /datomic-pro-${DATOMIC_VERSION}.zip
RUN rm /datomic-pro-${DATOMIC_VERSION}.zip
RUN mv /datomic-pro-${DATOMIC_VERSION} /datomic

ENV DATOMIC_TX_METRICS_VERSION 0.1.0-alpha

ADD https://github.com/life-research/datomic-tx-metrics/releases/download/v${DATOMIC_TX_METRICS_VERSION}/datomic-tx-metrics-${DATOMIC_TX_METRICS_VERSION}-standalone.jar /datomic/lib/
ADD /transactor.properties /datomic/
ADD /logback.xml /datomic/bin/logback.xml
ADD /start.sh /datomic/start
RUN chmod +x /datomic/start

VOLUME /datomic/log

# Replacing the netty version that comes with Datomic may be necessary if it's
# too old. Otherwise the metric collector's server won't start because of
# missing functions which eventually leads to the transactor not being able to
# finish startup.
RUN find /datomic/lib -name 'netty-all-.*' -delete
ADD https://repo1.maven.org/maven2/io/netty/netty-all/4.1.42.Final/netty-all-4.1.42.Final.jar /datomic/lib

EXPOSE 4334
EXPOSE 8080

ADD /datomic-tx-metrics.jar /datomic/lib/

WORKDIR /datomic
CMD ["./start"]
