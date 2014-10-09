# Base image init

FROM dockerfile/java:openjdk-7-jdk
RUN apt-get -y install wget
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein && chmod a+x ./lein && sudo mv ./lein /usr/bin/
ENV LEIN_ROOT true
RUN lein

WORKDIR /code
ADD project.clj /code/project.clj
RUN lein deps

WORKDIR /code
ADD . /code