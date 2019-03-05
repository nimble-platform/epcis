#FROM tomcat:8.0-jre8
#MAINTAINER Salzburg Research <nimble-srfg@salzburgresearch.at>

#ARG finalName
#ADD $finalName /usr/local/tomcat/webapps
#CMD ["catalina.sh", "run"]

FROM nimbleplatform/nimble-base
MAINTAINER Salzburg Research <nimble-srfg@salzburgresearch.at>

# copy resource folder
COPY /resources /resources
RUN ls /resources

#COPY /resources/ca.crt /usr/local/share/ca-certificates/
#RUN update-ca-certificates

VOLUME /tmp
ARG finalName
ENV JAR '/'$finalName
ARG port
ADD $finalName $JAR
RUN touch $JAR
ENV PORT 8091
EXPOSE $PORT
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "epcis.jar"]