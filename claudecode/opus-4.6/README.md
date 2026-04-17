# Cert Alert

## Score Final : 95 / 100

### 1. Autonomie & Comportement de l'Agent (30 / 35)
- **Nombre de modifications** : 10/15 — 1 à 5 tours de correction/ajustement ont été nécessaires après la première livraison. Ce n'est donc pas un one shot, mais reste dans la fourchette des ajustements normaux.
- **Lancement des sous-agents** : 10/10 — L'agent a lu les fichiers avant d'écrire et a lancé les builds/tests de manière cohérente, sans comportement passif ou incohérent.
- **Gestion du contexte (Itération)** : 10/10 — Aucune régression observée lors des itérations. Le code précédemment fonctionnel a été préservé ou amélioré intelligemment.

### 2. Architecture & Sécurité du Code (35 / 35)
- **Logique Métier & Sécurité** : 15/15 — OAuth2 Resource Server configuré avec `spring-boot-starter-oauth2-resource-server`. Les rôles sont respectés via `@PreAuthorize("hasRole('MANAGER')")`. La visibilité est strictement restreinte au groupe d'appartenance grâce aux méthodes repository group-scoped et au test `crossGroupIsolation`.
- **Propreté (Séparation des couches)** : 10/10 — Controllers propres, logique métier dans les Services, requêtes isolées dans les Repositories. Aucun "God Object".
- **Robustesse & Gestion d'erreurs** : 10/10 — `GlobalExceptionHandler` capture proprement les erreurs et retourne des JSON structurés. URL injoignable et mauvais fichier .cer sont gérés sans stacktrace brute exposée.

### 3. Débrouillardise sur l'Implicite (30 / 30)
- **Extraction TLS & Fichier** : 10/10 — Utilisation correcte des APIs Java natives (`SSLContext` + `SSLSocket` pour l'URL, `CertificateFactory` pour le fichier). Aucune bibliothèque inventée.
- **Mécanisme d'Alerte** : 10/10 — CRON propre via `@Scheduled(cron = "${cert-alert.alert.scan-cron}")` avec seuil configurable dans `application.yml`.
- **Initiative (Tests & Setup)** : 10/10 — 22 tests fournis spontanément (intégration + unitaires) et un README complet avec instructions de lancement.

### Synthèse

Ce projet est d'excellente facture technique : architecture Spring Boot bien structurée, sécurité OAuth2/JWT correctement implémentée avec isolation des groupes, gestion d'erreurs professionnelle, et une bonne débrouillardise sur les implicites (CRON configurable, APIs Java natives pour TLS, tests d'intégration complets). La seule raison de ne pas atteindre le 100/100 est le nombre de modifications post-livraison (1 à 5), qui empêche d'accéder au 15/15 du critère "One shot". Pour atteindre le maximum, il aurait fallu une livraison fonctionnelle du premier coup sans aucun ajustement nécessaire. À part cela, le code est production-ready pour un test technique.

---

Pour les détails techniques, le guide d'utilisation et la documentation complète du projet, voir [ARCHITECTURE.md](./ARCHITECTURE.md).
