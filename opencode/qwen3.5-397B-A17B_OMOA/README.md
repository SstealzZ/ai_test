# Système d'Évaluation - Projets Agentiques

## Score Final : 35 / 100

### 1. Autonomie & Comportement de l'Agent (5 / 35)
- **Nombre de modifications** : 0/15 — L'utilisateur a factuellement confirmé "Plus de 10" tours de correction après la première livraison. Le barème exige 0 dès que le seuil de 10 est dépassé.
- **Lancement des sous-agents** : 5/10 — L'utilisateur a qualifié le comportement d'**"Incohérent"** (tourne en boucle, mauvais outils). Le barème attribue 5 pour cette catégorie.
- **Gestion du contexte** : 0/10 — L'utilisateur a confirmé une **régression observée** (code fonctionnel précédemment cassé lors d'une correction). Le barème stipule 0 dès la moindre régression.

### 2. Architecture & Sécurité du Code (20 / 35)
- **Logique Métier & Sécurité** : 0/15 — 
  - **OAuth2 absent** : `SecurityConfig.java` configure un `formLogin()` classique (authentification par formulaire) et le `pom.xml` ne contient pas `spring-boot-starter-oauth2-client`, malgré les mentions dans le README et le POM.
  - **Visibilité non restreinte au groupe** : Dans `CertificateService.java` lignes 324-330, `listCertificates(Long userId, Long groupId, ...)` exécute `certificateRepository.findAll(pageable)` quand `groupId` est `null`, exposant **tous les certificats** à tout utilisateur authentifié sans filtrage d'appartenance. C'est une faille majeure de fuite de données.
  - "Presque sécurisé" n'existe pas dans le barème : une seule faille = 0.
- **Propreté (Séparation des couches)** : 10/10 — Les controllers (`CertificateController`, `GroupController`) sont propres et délèguent aux services. Les repositories sont isolés. Aucun "God Object" flagrant ne centralise toute la logique. `CertificateService` est volumineux mais reste centré sur une responsabilité fonctionnelle unique.
- **Robustesse & Gestion d'erreurs** : 10/10 — `GlobalRestExceptionHandler.java` capture exhaustivement les exceptions (validation, accès, certificats, erreurs génériques) et renvoie des réponses JSON structurées. Aucune stacktrace brute n'est exposée à l'utilisateur final. `CertificateParserService.java` gère les timeouts (10s), les codes HTTP d'erreur et les formats invalides avec un fallback PEM/DER.

### 3. Débrouillardise sur l'Implicite (10 / 30)
- **Extraction TLS & Fichier** : 0/10 — 
  - Fichier : correct, via Bouncy Castle (`PEMParser`, `CertificateFactory`).
  - URL : **fondamentalement incorrect**. `CertificateParserService.java` ligne 62-86 utilise `HttpURLConnection` pour faire un simple téléchargement HTTP GET du contenu de l'URL. Ce n'est **pas** une extraction TLS : le code ne récupère pas le certificat présenté par le serveur lors du handshake SSL/TLS (qui nécessiterait `HttpsURLConnection.getServerCertificates()`). C'est une mauvaise bibliothèque / approche pour ce cas d'usage.
- **Mécanisme d'Alerte** : 0/10 — Le CRON est bien présent (`@Scheduled(cron = "0 0 * * * *")` dans `CertificateAlertScheduler.java`), mais le seuil configurable est **cassé** : le `@Value` référence la clé `certmanager.scheduler.alert-threshold-days` (ligne 32 du scheduler) tandis que `application.yml` définit `certmanager.scheduler.expiry.threshold-days` (ligne 69). La valeur effective est donc toujours la valeur codée en dur `30` du `@Value`. Par ailleurs, `EmailService` est annoté `@Profile("!prod")` et ne fait que des logs mock, sans implémentation réelle d'envoi d'email. Le barème exige un seuil configurable externement et un mécanisme propre ; ici la configuration est défaillante.
- **Initiative (Tests & Setup)** : 10/10 — Le `docker-compose.yml` fournit un environnement complet (PostgreSQL + MailHog + app), le `Dockerfile` est multi-stage bien construit, et le `README.md` est détaillé avec commandes, ports et troubleshooting. Le critère accepte le Docker DB comme justifiant le 10 même en l'absence de tests (qui sont ici absents : `src/test` est vide).

### Synthèse
Le projet affiche une structure technique honorable sur le papier (séparation des couches, gestion d'erreurs globale, Docker complet), mais il est gravement pénalisé par un comportement agentique chaotique (plus de 10 itérations, régressions, outils mal utilisés) et par des failles fonctionnelles majeures. La sécurité est branlante : pas d'OAuth2 réel, et surtout une fuite massive de données sur les certificats via `listCertificates`. L'extraction TLS via URL est conceptuellement fausse (simple download HTTP au lieu de handshake SSL). Pour atteindre le maximum, il aurait fallu livrer en one-shot sans régression, implémenter un vrai OAuth2, sécuriser le listing des certificats par appartenance utilisateur, et utiliser `HttpsURLConnection` pour récupérer le certificat serveur.
