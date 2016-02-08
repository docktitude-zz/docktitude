# Docktitude - The Easy Way to Build your Docker images Hierarchy

![](https://raw.githubusercontent.com/docktitude/docktitude/master/docs/docktitude-logo.png "http://docktitude.io")



## Requirements

### For Linux packages or binary version
- Any Java v1.8+ set in the PATH environment variable
- Download: [OpenJDK](http://openjdk.java.net/install/index.html) | [Oracle JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

### Docker
- Obviously you need it :)
- Download: [Docker](https://www.docker.com)


## Installation

- Debian, Ubuntu, ...
```
$> apt-get install bash-completion
$> dpkg -i docktitude_1.0.0_amd64.deb
```

- Centos, Fedora, ...
```
$> yum install bash-completion
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
  -v your-docker-contexts-dir:/docker-contexts \
  docktitude/docktitude
```


## Getting started

1. **cd docker-contexts** *(where you store your Docker contexts)*
2. **docktitude config** *(check the computed tags for the build according to the contexts naming convention)*
3. **docktitude upgrade** *(cascade build: from the root to the leafs)*


## Usage examples

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
```


```bash
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


## Documentation

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


## Licensing

Docktitude is licensed under Apache License, Version 2.0.
