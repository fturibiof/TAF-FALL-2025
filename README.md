<h1 align="center">TAF — Test Automation Framework</h1>

<p align="center">
  <em>Cadriciel unifié d'automatisation de tests — Projet R&D, développé à l'ÉTS Montréal</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-Spring_Boot-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Frontend-Angular-DD0031?logo=angular&logoColor=white" alt="Angular"/>
  <img src="https://img.shields.io/badge/Infra-Docker-2496ED?logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/Tests-Selenium-43B02A?logo=selenium&logoColor=white" alt="Selenium"/>
  <img src="https://img.shields.io/badge/Tests-Gatling-FF9E2A?logo=gatling&logoColor=white" alt="Gatling"/>
</p>

---

**TAF** (Test Automation Framework) est un projet de recherche et développement qui fournit une **interface web unique** pour piloter plusieurs outils de tests automatisés (Selenium, Gatling, tests d'API, etc.).

L'objectif est de centraliser la configuration, l'exécution et le suivi des résultats de tests dans une plateforme unifiée, évitant ainsi de jongler entre différents outils et interfaces.

---
## Architecture microservices

Le projet suit une architecture microservices orchestrée par Docker Compose :

| Service | Rôle |
|---|---|
| `gateway` | Point d'entrée API Gateway |
| `registry` | Service de découverte (Service Registry) |
| `auth` | Authentification et gestion des tokens |
| `user` | Gestion des utilisateurs |
| `autotest-main` | Moteur principal d'orchestration des tests |
| `selenium-test-Service` | Exécution des tests Selenium |
| `test-performance-Service` | Exécution des tests de performance (Gatling) |
| `testapi-Service` | Exécution des tests d'API |
| `export-import` | Import/export des configurations et résultats |

---

## Prérequis
 
| Outil | Version minimale | Installation |
|---|---|---|
| **Git** | 2.x | [git-scm.com/downloads](https://git-scm.com/downloads) |
| **Docker Desktop** | 4.x (ou Docker Engine + Compose) | [docs.docker.com/get-docker](https://docs.docker.com/get-docker/) |
| **PowerShell** | 7+ | [Inclus sur Windows](#-windows) · [À installer sur Linux/macOS](#-linux--macos) |

### Windows
 
PowerShell 7+ est préinstallé sur la plupart des versions récentes de Windows. Si besoin, vous pouvez le mettre à jour via le [Microsoft Store](https://apps.microsoft.com/detail/9MZ1SNWT0N5D) ou avec `winget` :
 
```powershell
winget install Microsoft.PowerShell
```
 
### Linux / macOS
 
Le script de démarrage `start-taf-local.ps1` est écrit en PowerShell. Sur **Linux** et **macOS**, il faut l'installer manuellement.
 
**Ubuntu / Debian :**
 
```bash
sudo apt-get update && sudo apt-get install -y wget apt-transport-https software-properties-common
source /etc/os-release
wget -q https://packages.microsoft.com/config/ubuntu/$VERSION_ID/packages-microsoft-prod.deb
sudo dpkg -i packages-microsoft-prod.deb && rm packages-microsoft-prod.deb
sudo apt-get update && sudo apt-get install -y powershell
```
 
**macOS (Homebrew) :**
 
```bash
brew install powershell/tap/powershell
```
 
**Autres distributions :** voir la [documentation officielle Microsoft](https://learn.microsoft.com/fr-fr/powershell/scripting/install/installing-powershell-on-linux).
 
Une fois installé, lancez les scripts `.ps1` avec `pwsh` :
 
```bash
pwsh ./start-taf-local.ps1 -Mode full -Build
```
 
---

## Démarrage rapide

### 1. Cloner le dépôt

```bash
git clone https://github.com/Automated-Test-Framework/TAF-FALL-2025.git
cd TAF-FALL-2025
```

### 2. Premier lancement (build complet)

```powershell
.\start-taf-local.ps1 -Mode full -Build
```

### 3. Lancements suivants

```powershell
.\start-taf-local.ps1 -Mode full
```

### 4. Lancer une seule équipe

```powershell
.\start-taf-local.ps1 -Mode team1 -Build   # Équipe 1 uniquement
.\start-taf-local.ps1 -Mode team2 -Build   # Équipe 2 uniquement
.\start-taf-local.ps1 -Mode team3 -Build   # Équipe 3 uniquement
```

### 5. Nettoyage complet

```powershell
.\start-taf-local.ps1 -Clean
```

---

## Vérification du déploiement

1. **Attendre 2 à 3 minutes** après la fin du script.
2. **Vérifier l'état des conteneurs :**

   ```bash
   docker compose -f docker-compose-local-test.yml ps
   ```

3. **Accéder aux interfaces web :**

   | Équipe | URL |
   |---|---|
   | Équipe 1 | [http://localhost:4200](http://localhost:4200) |
   | Équipe 2 | [http://localhost:4300](http://localhost:4300) |
   | Équipe 3 | [http://localhost:4400](http://localhost:4400) |

---

## Documentation complète (Wiki)

Une **documentation détaillée** est disponible dans le [Wiki du projet](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki). Vous y trouverez notamment :

- [Guide de démarrage](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Guide-de-d%C3%A9marrage) — instructions pas à pas pour configurer votre environnement
- [Architecture](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Architecture) — vue d'ensemble de l'architecture microservices
- [Architecture Base de données](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Architecture-Base-de-donn%C3%A9es) — modèle de données
- [Frontend](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Frontend) — structure et conventions Angular
- [Backend](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Backend) — services Spring Boot et API
- [Gitflow](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Gitflow) — stratégie de branchement
- [Pipeline CI/CD](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Pipeline-CI-CD) — intégration et déploiement continu
- [Guide de Déploiement Jenkins](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Guide-de-D%C3%A9ploiement-Jenkins) — configuration Jenkins
- [Selenium](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Selenium) — tests fonctionnels
- [Tests API](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Tests-API) — tests de services REST
- [Tests de Performance](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Tests-de-Performance) — tests Gatling
- [Tableau de Bord](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Tableau-de-Bord) — visualisation des résultats
- [Design UX/UI](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Design-UX-UI) — maquettes et guidelines
- [DevOps Pipeline](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/DevOps-Pipeline) — infrastructure et automatisation

> **Consultez le Wiki avant de contribuer** — il contient les conventions, le gitflow et les standards du projet.

---

## Contribuer

1. **Lisez le Wiki**, en particulier les pages [Gitflow](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Gitflow) et [Guide de démarrage](https://github.com/Automated-Test-Framework/TAF-FALL-2025/wiki/Guide-de-d%C3%A9marrage).
2. Forkez le dépôt et créez une branche à partir de `main`.
3. Suivez les conventions de nommage décrites dans le Wiki.
4. Soumettez une Pull Request avec une description claire de vos changements.

---

## Licence

Projet académique — ÉTS, Montréal.
