# Système d'Évaluation - Projets Agentiques

## Score Final : 35 / 100

### 1. Autonomie & Comportement de l'Agent (5 / 35)
- **Nombre de modifications** : 0/15 — L'utilisateur a confirmé **"Plus de 10"** tours de correction/ajustement après la première livraison. Le barème exige 0 dès que le seuil de 10 est dépassé.
- **Lancement des sous-agents** : 5/10 — L'utilisateur a qualifié le comportement d'**"Incohérent"** (tourne en boucle, mauvais outils). Le barème attribue 5 pour cette catégorie.
- **Gestion du contexte** : 0/10 — L'utilisateur a confirmé une **"Régression observée"** (code fonctionnel précédemment cassé lors d'une correction). Le barème stipule 0 dès la moindre régression.

### 2. Architecture & Sécurité du Code (20 / 35)
- **Logique Métier & Sécurité** : 0/15 —
  - **OAuth2 absent** : `SecurityConfig.java` (lignes 49-53) configure un `formLogin()` classique. Le `pom.xml` ne contient pas `spring-boot-starter-oauth2-client`.
  - **Visibilité non restreinte au groupe** : Dans `CertificateService.java` lignes 324-330, `listCertificates(Long userId, Long groupId, ...)` exécute `certificateRepository.findAll(pageable)` quand `groupId` est `null`, exposant **tous les certificats** à tout utilisateur authentifié sans filtrage d'appartenance. C'est une faille majeure de fuite de données.
  - "Presque sécurisé" n'existe pas dans le barème : une seule faille = 0.
- **Propreté (Séparation des couches)** : 10/10 — Les controllers (`CertificateController`, `GroupController`) sont propres et délèguent aux services. Les repositories sont isolés. Aucun "God Object" flagrant ne centralise toute la logique.
- **Robustesse & Gestion d'erreurs** : 10/10 — `GlobalRestExceptionHandler.java` capture exhaustivement les exceptions (validation, accès, certificats, erreurs génériques) et renvoie des réponses JSON structurées. Aucune stacktrace brute n'est exposée à l'utilisateur final. `CertificateParserService.java` gère les timeouts (10s), les codes HTTP d'erreur et les formats invalides.

### 3. Débrouillardise sur l'Implicite (10 / 30)
- **Extraction TLS & Fichier** : 0/10 —
  - Fichier : correct, via Bouncy Castle (`PEMParser`, `CertificateFactory`).
  - URL : **fondamentalement incorrect**. `CertificateParserService.java` ligne 62-86 utilise `HttpURLConnection` pour faire un simple téléchargement HTTP GET du contenu de l'URL. Ce n'est **pas** une extraction TLS : le code ne récupère pas le certificat présenté par le serveur lors du handshake SSL/TLS (qui nécessiterait `HttpsURLConnection.getServerCertificates()`). C'est une mauvaise bibliothèque / approche pour ce cas d'usage.
- **Mécanisme d'Alerte** : 0/10 —
  - Le CRON est bien présent (`@Scheduled(cron = "0 0 * * * *")` dans `CertificateAlertScheduler.java`), mais le seuil configurable est **cassé** : le `@Value` référence la clé `certmanager.scheduler.alert-threshold-days` (ligne 32 du scheduler) tandis que `application.yml` définit `certmanager.scheduler.expiry.threshold-days` (ligne 69). La valeur effective est donc toujours la valeur codée en dur `30` du `@Value`.
  - Par ailleurs, `EmailService` est annoté `@Profile("!prod")` et ne fait que des logs mock, sans implémentation réelle d'envoi d'email. Le barème exige un seuil configurable externement et un mécanisme propre ; ici la configuration est défaillante.
- **Initiative (Tests & Setup)** : 10/10 — Le `docker-compose.yml` fournit un environnement complet (PostgreSQL + MailHog + app), le `Dockerfile` est multi-stage bien construit, et le projet compile. `src/test` est vide mais le critère accepte le Docker DB comme justifiant le 10.

### Synthèse
Le score reste à **35/100** après itérations supplémentaires car les défauts structurels n'ont pas été corrigés et le processus agentique a été désastreux (plus de 10 itérations, régressions, outils mal utilisés). Les points positifs sont la séparation des couches et la gestion d'erreurs globale, mais ils sont noyés par des failles critiques : absence d'OAuth2, fuite massive de données dans `listCertificates`, extraction TLS via URL conceptuellement fausse, et mécanisme d'alerte avec configuration cassée. Pour atteindre un score honorable, il aurait fallu livrer en 5 modifications maximum sans régression, implémenter un vrai OAuth2, sécuriser le listing des certificats par appartenance utilisateur, utiliser `HttpsURLConnection.getServerCertificates()` pour l'extraction TLS, et aligner la clé de configuration du seuil d'alerte dans le YAML.
