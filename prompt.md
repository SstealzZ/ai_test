# Prompt du Test Technique

Ce document contient le prompt exact utilisé pour évaluer chaque modèle d'IA agentique dans ce benchmark.

---

## Prompt

```
Act as a Java developer candidate for an option position. You have high flexibility to approach the design. Prove your capability to independently approach the functional specification. Some design details were intentionally omitted to measure your ability to organize yourself.

Test description
Create a service that alerts the operator when a TLS certificate is about to expire.

Features:

access is restricted to specific users
the users are configured with:
group : the certificates created by a group user are visible only for the group members.
role : only some users can add certificates, all users in the group can list them.
add certificate to DB
file upload (like *.cer)
get from URL (Ex. www.google.com)
list certificates, descending by expiry date
add configurable expiration threshold (Ex. 30 days), and alert when close to expiration date
Recommended technical stack

Spring Boot
Oauth2
SPA frontend + REST API
```

---

## Notes sur le prompt

- **Contraintes explicites** : Spring Boot, OAuth2, SPA + REST API
- **Implicites à découvrir** : extraction TLS via handshake (pas simple download), CRON configurable, gestion d'erreurs robuste, tests/setup Docker
- **Flexibilité intentionnelle** : pas de spécification technique détaillée pour mesurer la capacité d'auto-organisation

Ce prompt a été soumis à tous les modèles dans des conditions identiques (environnement OhMyOpenCode pour les modèles OpenCode, Claude Code pour Anthropic).
