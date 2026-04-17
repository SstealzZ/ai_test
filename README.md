# Benchmark Agentique — Cert Alert

Ce repository contient une évaluation comparative de plusieurs modèles d'IA agentiques sur un même test technique : la conception et l'implémentation d'une application **Spring Boot** de gestion et d'alerte d'expiration de certificats TLS.

## Le sujet

Construire une application full-stack avec les exigences suivantes :

- **Backend REST** en Spring Boot pour gérer des certificats TLS
- **Extraction de certificats** depuis une URL (handshake TLS) ou un fichier uploadé (`.cer`/`.pem`)
- **Sécurité OAuth2/JWT** avec isolation des données par groupe d'utilisateurs
- **Rôles** : `VIEWER` (lecture seule) et `MANAGER` (ajout/suppression)
- **Alertes** : mécanisme schedulé (CRON) avec un seuil configurable de jours avant expiration
- **Frontend SPA** minimal pour interagir avec l'API
- **Tests et/ou Docker** pour faciliter la prise en main

## Méthodologie d'évaluation

Chaque modèle a reçu le même prompt technique et a été laissé autonome dans un environnement OhMyOpenCode (OMOA pour les modèles OpenCode, Claude Code pour Anthropic). L'évaluation repose sur une grille stricte de **100 points** définie dans [`notation.md`](./notation.md), répartie en trois axes :

| Axe | Points | Description |
|---|---|---|
| **1. Autonomie & Comportement de l'Agent** | 35 | Nombre d'itérations post-livraison, usage cohérent des outils (lecture avant écriture, builds/tests), gestion du contexte sans régression |
| **2. Architecture & Sécurité du Code** | 35 | OAuth2 configuré, rôles respectés, isolation par groupe, séparation des couches, gestion d'erreurs robuste |
| **3. Débrouillardise sur l'Implicite** | 30 | Bonnes API Java pour l'extraction TLS, CRON configuré proprement, initiative (tests, Docker, README) |

> **Règle d'or** : si un critère exige la perfection (15, 10 ou 0), une seule faille suffit à valoir **0**.

## Classement des modèles

| Rang | Modèle | Score | Résumé |
|:---:|:---|:---:|:---|
| 🥇 1 | **Claude Opus 4.6** | **95/100** | Livrable quasi-production-ready. Architecture propre, OAuth2 Resource Server, 22 tests, gestion d'erreurs globale. Seul bémol : 1 à 5 itérations post-livraison. |
| 🥇 1 | **Sonnet 4.6** | **95/100** | Qualité équivalente à Opus : OAuth2 avec Spring Authorization Server, tests complets, séparation des couches exemplaire. Quelques itérations nécessaires également. |
| 3 | **GPT-5.3** | **80/100** | Excellente architecture et débrouillardise (tests + API TLS natives), mais pénalisé par l'absence stricte d'OAuth2 (formLogin classique à la place). |
| 4 | **GLM-5.1** | **65/100** | Architecture et sécurité solides (35/35), mais seuil d'alerte YAML non branché dans le code, et absence totale de tests/Docker/README. |
| 5 | **Kimi K2.5** | **62/100** | Bonne séparation des couches, mais JWT maison codé en dur (pas de vrai OAuth2), gestion d'erreurs absente, et CRON/seuil codés en dur. |
| 6 | **Qwen 3.5 397B-A17B** | **35/100** | Processus agentique difficile (>10 itérations, régressions). Faille majeure de visibilité des certificats, extraction TLS via URL incorrecte, OAuth2 absent. |
| 7 | **Gemma 4 31B** | **5/100** | Échec total de génération : l'agent a tenté d'orchestrer des sous-agents mais avec un formatage JSON défectueux, aboutissant à un dossier de travail vide. |

## Structure du repo

```
ai_test/
├── notation.md                 # Grille d'évaluation complète
├── claudecode/
│   └── opus-4.6/               # Implémentation Claude Opus 4.6 (95 pts)
├── opencode/
│   ├── sonnet4.6-OMOA/         # Implémentation Sonnet 4.6 (95 pts)
│   ├── gpt-5.3_OMOA/           # Implémentation GPT-5.3 (80 pts)
│   ├── glm-5.1_OMOA/           # Implémentation GLM-5.1 (65 pts)
│   ├── kimik2.5_OMOA/          # Implémentation Kimi K2.5 (62 pts)
│   ├── qwen3.5-397B-A17B_OMOA/ # Implémentation Qwen 3.5 (35 pts)
│   └── gemma4-31b_OMOA/        # Implémentation Gemma 4 31B (5 pts)
└── README.md                   # Ce fichier
```

Chaque sous-répertoire contient son propre `README.md` avec le rapport d'évaluation détaillé, ainsi que le code source complet de l'application.

## Principaux enseignements

- **Les modèles Anthropic (Opus & Sonnet)** excellent sur ce type de tâche agentique structurée : architecture propre, sécurité correctement implémentée, et bonne autonomie dans la résolution des problèmes implicites.
- **GPT-5.3** montre une très bonne capacité d'orchestration et de structuration, mais peut rater des contraintes explicites (ici OAuth2) malgré une implémentation fonctionnelle alternative.
- **Les modèles open-source chinois (GLM, Kimi, Qwen)** présentent des architectures backend correctes mais peinent sur la rigueur des exigences fonctionnelles et sécuritaires. Leur processus agentique est aussi plus laborieux.
- **Gemma 4 31B** illustre les limites actuelles de certains modèles open-source sur l'orchestration d'outils : comprendre le concept n'est pas suffisant, il faut aussi le formater correctement.

---

*Benchmark réalisé le 17 avril 2026.*
