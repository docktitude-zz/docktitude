#@[--DOCKTITUDE-SCRIPT
#@ #!/bin/sh -
#@
#@ sudo docker run -it --rm --name docktitude \
#@   -v /var/run/docker.sock:/var/run/docker.sock \
#@   -v $(which docker):/bin/docker \
#@   -v $HOME/docker-contexts:/docker-contexts \
#@   docktitude:latest
#@DOCKTITUDE-SCRIPT--]
#
#
#
FROM debian:latest
MAINTAINER me@docktitude.io

ENV DOCKTITUDE_VERSION 1.0.0
ENV DEBIAN_FRONTEND noninteractive

ADD docktitude_${DOCKTITUDE_VERSION}_amd64.deb /tmp/

RUN echo "deb http://httpredir.debian.org/debian jessie-backports main contrib" >> /etc/apt/sources.list \
 && apt-get update -qq \
 && apt-get install -qqy xz-utils sudo locales bash-completion openjdk-8-jre-headless --no-install-recommends \
 && locale-gen en_US.UTF-8 \
 && localedef -c -f UTF-8 -i en_US en_US.UTF-8 \
 && dpkg -i /tmp/docktitude*.deb \
 && echo ". /etc/bash_completion.d/docktitude" >> /etc/bash.bashrc \
 && mkdir /docker-contexts \
 && rm -rf /var/lib/apt/lists/* \
 && rm /tmp/*.deb

ENV LANG en_US.UTF-8

VOLUME /docker-contexts
WORKDIR /docker-contexts

CMD ["/bin/bash"]
