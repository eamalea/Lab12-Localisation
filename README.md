# Localisation – Application Android de suivi GPS avec backend PHP/MySQL

Cette application Android récupère la position GPS de l’utilisateur, l’envoie à un serveur PHP (via Volley) qui la stocke dans une base MySQL, et permet d’afficher toutes les positions enregistrées sur une Google Map.

## Fonctionnalités

- Récupération de la position GPS (latitude, longitude, altitude, précision)
- Envoi automatique au serveur à intervalles réguliers (60s / 150m)
- Stockage MySQL via API REST (JSON)
- Affichage de toutes les positions enregistrées sur une carte interactive
- Gestion des permissions Android (localisation, téléphone)
- Utilisation de Volley pour les requêtes réseau
- Architecture PHP orientée objet (PDO, DAO, Service)

## Prérequis

- Serveur web local (XAMPP/WAMP) avec PHP 7.4+ et MySQL
- Android Studio Hedgehog+
- Un émulateur ou un téléphone avec Google Play Services
- Une clé API Google Maps (Maps SDK for Android)

## Installation

### 1. Base de données

- Lancer phpMyAdmin, créer une base `localisation`
- Exécuter le script SQL fourni dans le dépôt

### 2. Backend PHP

- Copier le dossier `localisation/` dans `htdocs` (XAMPP) ou `www` (WAMP)
- Vérifier que l’URL `http://localhost/localisation/createPosition.php` répond

### 3. Application Android

- Cloner ce dépôt
- Ouvrir le projet dans Android Studio
- Renseigner votre clé Google Maps dans `google_maps_api.xml`
- Modifier l’IP du serveur dans `MainActivity.java` (INSERT_URL) et `MapsActivity.java` (SHOW_URL)
- Lancer l’application sur un appareil

## Améliorations apportées

- Singleton Volley pour optimiser les requêtes
- Fallback sur ANDROID_ID au lieu de l’IMEI (pour compatibilité Android 10+)
- Réponses JSON structurées côté serveur avec codes HTTP
- Validation des paramètres sur le serveur
- Gestion des erreurs avec toasts et logs

## Auteur

Votre Nom – [@votrecompte](https://github.com/votrecompte)

## Licence

Projet pédagogique – libre d’utilisation.
