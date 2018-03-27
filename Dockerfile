#java
FROM anapsix/alpine-java 

MAINTAINER blerterTeam

#create dir
RUN mkdir -p /blerter

#set dir
WORKDIR /blerter

#copy module
COPY target/blerter-vaadin-service.jar .

#export port
EXPOSE 4030

#deploy
CMD java -Xms1024m -Xms1024m -Dorg.apache.coyote.http11.Http11Protocol.COMPRESSION=on  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70 -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -server -jar blerter-vaadin-service.jar
