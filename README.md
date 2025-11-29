# IW03: Ansible Playbook for Server Configuration

> Realizat de studentul: Badia Victor \
> Grupa: I2301
> \
> Verificat de Mihail Croitor

## Objective
Învață să Creezi Playbook-uri Ansible pentru Automatizarea Configurării Serverelor

## Prerequisites
Această temă individuală se bazează pe Tema Individuală precedentă IW04.

Creați un dosar numit lab05 în depozitul vostru GitHub pentru a stoca toate fișierele legate de această lucrare individuală.
Trebuie să aveți instalate Docker și Docker Compose pentru a finaliza această temă.
De asemenea, ar trebui să aveți un depozit cu un proiect PHP care conține teste unitare, la fel ca în Tema Individuală IW04.

Copiați fișierele din lucrarea individuală precedentă lab04 în dosarul lab05.
Această temă include, de asemenea, descrieri ale pașilor din temele individuale anterioare, marcate ca "Review" (Revizuire).

## Assignment
Cream un fisier compose.yaml cu urmatorul continut:
```bash
version: '3.8'

services:
  jenkins-controller:
    image: jenkins/jenkins:lts
    privileged: true
    user: root
    ports:
      - "8080:8080" 
      - "50000:50000" 
    volumes:
      - ./jenkins_home:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock 
    container_name: jenkins-controller

  ssh-agent:
    build:
      context: .
      dockerfile: Dockerfile.ssh_agent
    container_name: ssh-agent
    ports:
      - "2222:22" 

  ansible-agent:
    build:
      context: .
      dockerfile: Dockerfile.ansible_agent
    container_name: ansible-agent
    ports:
      - "2223:22" 

  test-server:
    build:
      context: .
      dockerfile: Dockerfile.test_server
    container_name: test-server
    ports:
      - "80:80"
```

## 1. Installing and Configuring Jenkins
Vom folosi Jenkins pentru a gestiona toate etapele de automatizare. Cream serviciul jenkins-controller în fișierul compose.yaml 
(sau docker-compose.yaml), lansam containerul și îl configuram.

Lansam serviciul Jenkins folosind Docker Compose:
```bash
docker-compose up -d jenkins-controller
```
<img width="1885" height="475" alt="image" src="https://github.com/user-attachments/assets/a27cbc17-e02e-4291-a3b5-d6d81d31380b" />

Deschidem browser-ul și navigam la http://localhost:8080 :
<img width="1235" height="813" alt="image" src="https://github.com/user-attachments/assets/d1142fed-4a47-47bd-9dc8-2eb24af7c76c" />

Vedem ecranul de deblocare Jenkins. Trebuie să gasim parola de administrator generată automat. Folosim comanda:
```bash
docker exec jenkins-controller cat /var/jenkins_home/secrets/initialAdminPassword
```

Alagem Select plugins to install. Ne asiguram dacă sunt prezente următoarele plugin-uri: Docker Pipeline, GitHub Integration, SSH Agent. Dupa tastam Install:
<img width="1225" height="791" alt="image" src="https://github.com/user-attachments/assets/891a2513-5477-4e58-811c-78a9b014b19e" />

După instalare plugin-urilor, cream utilizatorul administrator.
<img width="1919" height="869" alt="image" src="https://github.com/user-attachments/assets/eea7f844-1729-470f-82c9-3e302735cf1d" />

## Setting Up SSH Agent
Agentul SSH va fi folosit de Jenkins pentru a construi proiectul PHP și pentru a rula testele unitare.

Creează un fișier Dockerfile.ssh_agent pentru agentul SSH. Instalează pachetele necesare (PHP-CLI și, eventual, alte dependențe). 
Creează cheile SSH pentru integrarea Jenkins cu agentul SSH și configurează Jenkins să folosească aceste chei.

Adaugă un serviciu "ssh-agent" în fișierul compose.yaml folosind Dockerfile-ul creat.

Cream fisierul Dockerfile.ssh_agent cu urmatorul continut:
```bash
FROM ubuntu:latest

ENV DEBIAN_FRONTEND=noninteractive

RUN apt update && apt install -y \
    openssh-server \
    php-cli \
    git \
    wget \
    php-xml php-zip \
    && rm -rf /var/lib/apt/lists/*

RUN wget https://getcomposer.org/installer -O composer-setup.php \
    && php composer-setup.php --install-dir=/usr/local/bin --filename=composer

RUN mkdir -p /var/run/sshd
RUN echo 'PasswordAuthentication yes' >> /etc/ssh/sshd_config
RUN useradd -m -s /bin/bash jenkins && echo "jenkins:password" | chpasswd
RUN usermod -aG sudo jenkins

EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]
```

Rulam Docker Compose pentru a include noul serviciu:
```bash
docker-compose up -d ssh-agent
```

<img width="1906" height="729" alt="image" src="https://github.com/user-attachments/assets/1a301fbd-2acc-45fc-a004-d892d77c3a01" />

In Jenkins mergem la Manager Jenkins, Nodes and Clouds, New Node:
<img width="1146" height="522" alt="image" src="https://github.com/user-attachments/assets/7758a2f9-0d86-47eb-9602-fec869726ae2" />

Am creat un nod nou cu numele ssh-agent de tip Permanent Agent, cu un executor. Directorul radacina: /home/jenkins/workspace. Eticheta si gazda ssh-agent:
<img width="1554" height="64" alt="image" src="https://github.com/user-attachments/assets/ce04dc22-8118-4a65-a50d-facd10633a2b" />








