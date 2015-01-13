FROM ubuntu:14.10

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get -y update && apt-get -y install \
    openssh-server \
    python-software-properties \
    software-properties-common

# Java
RUN add-apt-repository ppa:webupd8team/java && apt-get -y update \
    && echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
    && apt-get -y install \
        oracle-java8-installer \
        oracle-java8-set-default

COPY jce/* /usr/lib/jvm/java-8-oracle/jre/lib/security/

RUN echo 'root:P@$$word!' | chpasswd
RUN groupadd votecounter && useradd votecounter -s /bin/bash -m -g votecounter -G votecounter && adduser votecounter sudo
RUN echo 'votecounter:P@$$word!' | chpasswd

COPY target/scala-2.11/vote-counter-assembly-1.0.jar application.conf /home/votecounter/
RUN chown -R votecounter:votecounter /home/votecounter

EXPOSE 8090
USER votecounter
WORKDIR /home/votecounter
CMD ["java", "-cp", "vote-counter-assembly-1.0.jar", "-Dconfig.file=application.conf", "com.softwaremill.votecounter.web.VoteCounterWeb"]