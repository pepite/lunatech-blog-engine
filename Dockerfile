FROM openjdk:8

RUN \
  curl -L -o sbt-1.3.8.deb https://dl.bintray.com/sbt/debian/sbt-1.3.8.deb && \
  dpkg -i sbt-1.3.8.deb && \
  rm sbt-1.3.8.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

RUN mkdir -p /build/project
ADD project /build/project
ADD app /build/app
ADD conf /build/conf
ADD public /build/public
ADD build.sbt /build/build.sbt

WORKDIR /build
RUN sbt dist 
RUN \
  unzip target/universal/dist.zip && \
  mv dist /opt && \
  chown daemon:daemon /opt/dist && \
  rm -rf /build

USER daemon
WORKDIR /opt/dist

ENTRYPOINT /opt/dist/bin/lunatech-blog-engine
