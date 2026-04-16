# Évaluation du Projet CertAlert

## Score Final : 65 / 100

---

## 1. Autonomie & Comportement de l'Agent (20 / 35)

*Source : réponses factuelles de l'utilisateur dans la session de génération.*

### Nombre de modifications : 5/15
L'utilisateur a indiqué **"5-10 modifications"** après la première livraison. Ce n'est donc pas un one-shot (15), ni dans la fourchette 1-5 (10). Le barème 5-10 correspond à **5 points**.

### Lancement des sous-agents : 5/10
L'utilisateur a noté **5/10**, indiquant un comportement incohérent (tours en boucle ou mauvais outils), sans atteindre le 0 "pas du tout".

### Gestion du contexte (Itération) : 10/10
L'utilisateur a attribué **10/10** : l'agent modifiait le code existant intelligemment sans le casser ni le faire régresser.

---

## 2. Architecture & Sécurité du Code (35 / 35)

### Logique Métier & Sécurité : 15/15
- **OAuth2 Resource Server JWT** correctement configuré dans `SecurityConfig.java` (lignes 44-75, `NimbusJwtDecoder` avec clé HMAC-SHA256).
- **Rôles respectés** : `CertificateService.ensureCanManage()` (lignes 166-169) bloque l'ajout/suppression si le rôle n'est pas `CERT_MANAGER`.
- **Visibilité restreinte au groupe** : `listCertificates` filtre par `groupId` (ligne 60), et `getCertificate`/`deleteCertificate` vérifient `cert.getGroup().getId().equals(user.getGroup().getId())` (lignes 71-73, 86-88).

### Propreté (Séparation des couches) : 10/10
Architecture en couches nette : 5 controllers, 4 services, 4 repositories, 5 entités, 5 DTOs, configs et handler séparés. Aucun "God Object" : les controllers délèguent aux services (ex: `CertificateController.java` ligne 36), qui délèguent aux repositories. La logique métier, le parsing TLS, la sécurité et la persistance sont isolés.

### Robustesse & Gestion d'erreurs : 10/10
- `GlobalExceptionHandler.java` utilise `@RestControllerAdvice` et `ProblemDetail` (RFC 7807) pour `IllegalArgumentException` (400), `SecurityException` (403) et `AccessDeniedException` (403).
- Les cas limites exigés sont gérés : URL injoignable (catchée dans `CertificateParserService.parseFromUrl()`, ligne 49, relancée en `IllegalArgumentException`) et fichier `.cer` invalide (catchée dans `parseFromBytes()`, ligne 24).
- Aucun `catch(e) {}` vide.

---

## 3. Débrouillardise sur l'Implicite (10 / 30)

### Extraction TLS & Fichier : 10/10
- **Fichier** : `CertificateFactory.getInstance("X.509")` puis `generateCertificate(is)` dans `CertificateParserService.parseX509()` (lignes 54-57).
- **URL** : `SSLContext`, `SSLSocket`, `startHandshake()` et `getPeerCertificates()` dans `parseFromUrl()` (lignes 35-48).
- Conversion PEM via `Base64.getMimeEncoder(64, ...)` (lignes 70-75). APIs Java standard, aucune bibliothèque inventée.

### Mécanisme d'Alerte : 0/10
Le CRON `@Scheduled` est bien configurable via `application.yml` (`cert-alert.scheduler.check-cron`, ligne 56). Cependant, le **seuil configurable via YAML/Properties n'est pas effectif** : `application.yml` définit `cert-alert.threshold.default-days: 30` (ligne 54), mais cette propriété est **jamais lue ni appliquée** dans le code. Dans `AlertService.java` (ligne 53), si aucun threshold actif n'existe en base, le check est skipé. La règle d'or s'applique : une seule faille = **0**.

### Initiative (Tests & Setup) : 0/10
- `src/test/java` est **vide** (aucun test unitaire).
- Aucun `Dockerfile`.
- Aucun `README.md` n'est présent à la racine du projet.
Le critère exige des tests, un Docker DB ou un README de lancement ; rien n'est fourni.

---

## Synthèse

Le projet **CertAlert** présente une **architecture solide et une sécurité bien implémentée** (OAuth2, isolation par groupe, séparation des couches), ce qui lui vaut le maximum sur la section 2. L'extraction TLS est également réussie avec les bonnes API Java. Cependant, le score est fortement pénalisé par le **manque d'initiative sur l'implicite** : le seuil YAML inopérant, l'absence totale de tests, de Docker et de README coûtent 20 points. Enfin, le processus de génération a nécessité **5 à 10 modifications**, ce qui reflète un travail laborieux plutôt qu'un one-shot. Pour atteindre le maximum, il aurait fallu : brancher la propriété `default-days` du YAML sur le `AlertService`, générer ne serait-ce qu'un `Dockerfile` ou un README, et ajouter quelques tests unitaires sur le parsing de certificats.
