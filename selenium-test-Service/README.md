![Logo taf](./logo_taf.png)

# Test Automation Framework

Cadriciel d'automatisation de tests permettant l'utilisation de plusieurs outils (Selenium, Gatling, etc.) via une interface web unique.

**Stack technique** : Java Spring Boot + Angular

## Démarrage rapide

**Prérequis** : Docker et Docker Compose

```bash
cd selenium-test-Service
docker compose up
```

**Prérequis** : Pour forcer le re-build

```bash
cd selenium-test-Service
docker compose up --build --force-recreate
```

### Accès

- **Frontend** : http://localhost:4200/home
- **Container principal** : selenium-test-service
- **Container Selenium** : selenium-1

## État du projet

### Hiver 2026

- Correction des fichiers de configuration (erreur HTTP 500)
- Amélioration de la documentation

### Perspectives futures

Aucune pour le moment.

### Problèmes actuels

Aucun.

