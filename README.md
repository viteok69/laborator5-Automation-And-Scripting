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





