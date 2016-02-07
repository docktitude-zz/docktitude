\title{\includegraphics{logo.png}\\ The Easy Way to Build your Docker images Hierarchy}
\date{}
\maketitle



# Requirements

## For Linux packages or binary version
- OpenJDK8 (JRE or any Java v1.8 set in the PATH environment variable)
- Docker (to run the commands based on Docker)

## For the Docker image
- Docker


# Installation

- Debian, Ubuntu, ...
```
$> dpkg -i docktitude_1.0.0_amd64.deb
```

- Centos, Fedora, ...
```
$> rpm -ivh docktitude-1.0.0.x86_64.rpm
```

- Binary
```
$> tar xzf docktitude-1.0.0-bin.tar.gz
```

- Docker image
```
$> docker run -it --rm --name docktitude \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which docker):/bin/docker \
  -v $HOME/docker-contexts:/docker-contexts \
  docktitude:latest
```


# Getting started

1. **cd docker-contexts** *(where you store your Docker contexts)*
2. **docktitude config** *(check the computed tags for the build according to the contexts naming convention)*
3. **docktitude upgrade** *(cascade build: from the root to the leafs)*

\pagebreak

# Usage examples

```java
$> docktitude --help
usage: docktitude [--help] <command> [<args>]

Commands:
   build <context>    Build context Docker image
   clean              Remove exited Docker containers and useless images
                      Use -v to remove the associated volumes
   config             List auto-configured Docker images building tags
   export             Export all contexts except binaries to a tar archive
   info               Show information relating to the Dockerfile files
   op <name>          Change maintainer information in the Dockerfile files
   print <context>    Show context Dockerfile
   script <context>   Show shell script for defined docktitude script tags
   snapshot           Display Docker images and save the selected one (.tar)
   status             Show local Docker images update status
   tree               List Docker images in a tree-like format
   update             Update external Docker images
   upgrade            Build cascade local Docker images
   version            Show version information
```

\pagebreak

```bash
$> docktitude tree
.
+-- debian:latest*
|   +-- debian:local
|       +-- apache
|       +-- common/mailcatcher
|       +-- debian:jdk6
|       |   +-- jmxtrans
|       +-- debian:jdk7
|       |   +-- debian:jdk7-ui
|       |   |   +-- gatling
|       |   +-- spring/petclinic
|       +-- debian:jdk8
|       |   +-- activemq
|       |   +-- debian:jdk8-ui
|       |       +-- debian:jdk8-scm
|       |       |   +-- eclipse
|       |       |   +-- idea
|       |       |   +-- netbeans
|       |       +-- libreoffice
|       +-- grafana
|       +-- influxdb
|       +-- telegraf
|           +-- metrics:jdk6
|               +-- perf/app1
+-- nginx*
    +-- demo
```

\pagebreak

```bash
$> docktitude print app1
--------------------------------
+++ app1
[/docker-contexts/perf/app1/Dockerfile]
--------------------------------
FROM metrics:jdk6
MAINTAINER demo@docktitude.io

# Hierarchy demo
--------------------------------



$> docktitude script libreoffice
--------------------------------
+++ libreoffice [ SHELL SCRIPT ]
--------------------------------
#!/bin/sh -

docker run -it --rm --name libreoffice \
  -e DISPLAY=unix$DISPLAY \
  -e GDK_SCALE \
  -e GDK_DPI_SCALE \
  -v /tmp/.X11-unix:/tmp/.X11-unix \
  -v /etc/localtime:/etc/localtime:ro \
  libreoffice
--------------------------------




```



# Licensing

Docktitude is licensed under Apache License, Version 2.0.
