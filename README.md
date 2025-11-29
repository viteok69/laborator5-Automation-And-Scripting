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

## 2. Setting Up SSH Agent
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
<img width="1566" height="68" alt="image" src="https://github.com/user-attachments/assets/6c6e025e-990d-4120-b1f9-48a1779771c4" />

## 3. Creating Ansible Agent
Agentul Ansible va fi folosit de Jenkins pentru a executa playbook-uri Ansible care vor configura serverul de testare.

Creează un fișier Dockerfile.ansible_agent pentru agentul Ansible, bazat pe imaginea Ubuntu. În acest fișier, instalează Ansible și dependențele necesare.
Creează chei SSH pentru integrarea Jenkins cu agentul Ansible și configurează Jenkins să folosească aceste chei.
Creează chei SSH pentru conectarea agentului Ansible la serverul de testare și configurează Ansible să folosească aceste chei. Conexiunea la serverul de testare trebuie efectuată ca utilizatorul ansible.

Adaugă un serviciu "ansible-agent" în fișierul compose.yaml folosind Dockerfile-ul creat.

Cream fisierul Dockerfile.ansible_agent cu urmatorul continut:
```bash
FROM ubuntu:latest

ENV DEBIAN_FRONTEND=noninteractive

RUN apt update && apt install -y \
    openssh-server \
    ansible \
    python3 \
    git \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /var/run/sshd
RUN echo 'PasswordAuthentication yes' >> /etc/ssh/sshd_config
RUN useradd -m -s /bin/bash jenkins && echo "jenkins:password" | chpasswd
RUN usermod -aG sudo jenkins

RUN mkdir -p /home/jenkins/.ssh && chown jenkins:jenkins /home/jenkins/.ssh

EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]
```

Cream nodul ansible-agent in Jenkins:
<img width="1559" height="59" alt="image" src="https://github.com/user-attachments/assets/afe77a18-97ad-4b8d-be82-81e401bae3d2" />

## 4. Creating Test Server
Serverul de testare va reprezenta un mediu de testare care va fi configurat folosind playbook-uri Ansible și în cadrul căruia va fi testată funcționalitatea aplicației PHP.

Creează un fișier Dockerfile.test_server cu configurația serverului de testare, bazat pe imaginea Ubuntu. În acest fișier, instalează openssh-server și configurează-l să funcționeze cu chei SSH.

Crearea fisierului Dockerfile.test_server cu urmatorului continut:
```bash
FROM ubuntu:latest

ENV DEBIAN_FRONTEND=noninteractive

RUN apt update && apt install -y \
    openssh-server \
    sudo \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /var/run/sshd
RUN echo 'PermitRootLogin no' >> /etc/ssh/sshd_config
RUN echo 'PasswordAuthentication no' >> /etc/ssh/sshd_config

RUN useradd -m -s /bin/bash ansible
RUN echo "ansible ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

RUN mkdir -p /home/ansible/.ssh
RUN chown -R ansible:ansible /home/ansible/.ssh
RUN chmod 700 /home/ansible/.ssh
COPY id_rsa_ansible_to_test.pub /tmp/ansible_key.pub
RUN cat /tmp/ansible_key.pub >> /home/ansible/.ssh/authorized_keys \
    && chown ansible:ansible /home/ansible/.ssh/authorized_keys \
    && chmod 600 /home/ansible/.ssh/authorized_keys

EXPOSE 22
EXPOSE 80
CMD ["/usr/sbin/sshd", "-D"]
```

Lansarea Serverului de Test:
```bash
docker-compose up -d --build test-server
```

<img width="1919" height="899" alt="image" src="https://github.com/user-attachments/assets/05555d4e-b85b-4d35-926d-a5ec8e5c02cd" />

## 5. Creating Ansible Playbook for Test Server Configuration
Creează un director ansible. În acest director, definește un fișier de inventar hosts.ini în care specifici parametrii de conexiune la serverul de testare.
Creează un playbook Ansible setup_test_server.yml care va efectua următoarele sarcini pe serverul de testare:

Instalarea și configurarea Apache2.
Instalarea și configurarea PHP și a extensiilor necesare.
Configurarea unui virtual host Apache pentru proiectul PHP.

Fișierul de Inventar hosts.ini cu urmatorul continut:
```bash
[webservers]
test-server ansible_host=test-server
```



Playbook-ul Ansible setup_test_server.yml cu urmatorul continut:
```bash
---
- hosts: webservers
  become: yes
  become_method: sudo
  roles:
    - web-server
```

Fisierul main.yaml de task-uri instalează Apache2, PHP-FPM și configurează site-ul:
```bash
---
- name: 1. Update apt cache
  apt:
    update_cache: yes
    cache_valid_time: 3600

- name: 2. Install required packages (Apache2, PHP and extensions)
  apt:
    name:
      - apache2
      - php
      - libapache2-mod-php
      - php-mysqli
      - php-cli
      - php-mbstring
      - php-xml
    state: present
  notify: Restart Apache

- name: 3. Create the web root directory
  file:
    path: /var/www/html
    state: directory
    owner: www-data
    group: www-data
    mode: '0755'

- name: 4. Copy custom Apache Virtual Host configuration file
  template:
    src: default.conf.j2
    dest: /etc/apache2/sites-available/000-default.conf
  notify: Restart Apache

# Handler:
- name: Restart Apache
  service:
    name: apache2
    state: restarted
```

## 6. Pipeline for Building and Testing PHP Project
Creează un director pipelines în directorul lab05. Creează un pipeline Jenkins pentru construirea și testarea proiectului PHP folosind agentul SSH. Pentru aceasta, creează un fișier php_build_and_test_pipeline.groovy în directorul pipelines.

Pipeline-ul ar trebui să includă următoarele etape (stages):
Clonarea depozitului (repository) cu proiectul PHP.
Instalarea dependențelor proiectului folosind Composer.
Rularea testelor unitare folosind PHPUnit.
Raportarea rezultatelor testelor.

Cream pipeline php_build_and_test_pipeline.groovy pentru build si test cu urmatorul continut:
```bash
def PROJECT_NAME = 'php-project'
def REPO_URL = 'https://github.com/viteok69/laborator5-Automation-And-Scripting' 

pipeline {
    agent { label 'ssh-agent' }
    options {
        skipDefaultCheckout true
    }
    stages {
        stage('Checkout Source Code') {
            steps {
                git branch: 'main', url: REPO_URL 
            }
        }
        stage('Install Dependencies') {
            steps {
                dir(PROJECT_NAME) {
                    sh 'composer install --no-dev --prefer-dist' 
                }
            }
        }
        stage('Run Unit Tests') {
            steps {
                dir(PROJECT_NAME) {
                    sh './bin/phpunit tests'
                }
            }
        }
        stage('Build Artifact') {
            steps {
                script {
                    sh "zip -r ../deploy-artifact-${BUILD_NUMBER}.zip *"
                    archiveArtifacts artifacts: "../deploy-artifact-${BUILD_NUMBER}.zip"
                }
            }
        }
    }
    post {
        failure {
            echo "Build failed. Check tests."
        }
    }
}
```
Acest script va rula pe ssh-agent.

## 7. Pipeline for Test Server Configuration Using Ansible








