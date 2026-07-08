FROM jenkins/jenkins:lts

USER root

RUN apt-get update && \
    apt-get install -y maven docker.io curl git && \
    apt-get clean

RUN curl -LO "https://dl.k8s.io/release/v1.35.0/bin/linux/amd64/kubectl" && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl && \
    rm kubectl

USER jenkins