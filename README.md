# 📲 CanScan

Générez en un instant un code QR pour **ajouter un contact**, **créer un événement de calendrier** ou **ce que vous voulez** !

- [x] 👤 **Partager un contact** : Générer un code QR scannable contenant ses coordonnées au format MECARD
- [x] 📅 **Créer un événement** : Générer un code QR pour ajouter un événement au calendrier au format VCALENDAR (iCalendar)
- [x] 🌐 **Partager ce que vous voulez** : Créer un code QR d’un lien vers un site web, un événement de calendrier, etc.
- [x] 🎨 **Personnaliser l'apparence** : Choisir vos couleurs, modules arrondis, marges et dimensions
- [x] 🖼️ **Ajouter votre logo** : Intégrer une image centrale sans compromettre la lisibilité (PNG, JPG, ou JPEG)
- [x] 📷 **Vérifier et tester** : S’assurer que le code QR est scannable grâce à l’aperçu dynamique
- [x] 💾 **Enregistrer facilement** : Sauvegarder l’image au format PNG

<br>[![License](https://img.shields.io/badge/license-GPLv3.0-brightgreen.svg)](https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme)<br>
[![Snyk Scan](https://img.shields.io/badge/Snyk%20Scan-authentication%20required-brightgreen?logo=snyk)](https://app.snyk.io/org/lob2018/project/d091601e-bd98-48ed-bd26-d3f369708314)<br>
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Lob2018_CanScan&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Lob2018_CanScan)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Lob2018_CanScan&metric=bugs)](https://sonarcloud.io/summary/new_code?id=Lob2018_CanScan)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Lob2018_CanScan&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=Lob2018_CanScan)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Lob2018_CanScan&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Lob2018_CanScan)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=Lob2018_CanScan&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=Lob2018_CanScan)<br>
[![Open Issues](https://img.shields.io/github/issues/lob2018/CanScan)](https://github.com/Lob2018/CanScan/issues)
[![Open Pull Requests](https://img.shields.io/github/issues-pr/lob2018/CanScan)](https://github.com/Lob2018/CanScan/pulls)
[![GitHub release](https://img.shields.io/github/v/release/lob2018/CanScan)](https://github.com/Lob2018/CanScan/releases)

<a href="https://github.com/Lob2018/CanScan/releases/latest">
 <img src="https://raw.githubusercontent.com/Lob2018/CanScan/master/.myresources/images/CanScan_in_action.png" alt="CanScan in action"  width="400"/>
</a>

## 🎬 Démo

[<img src="https://raw.githubusercontent.com/Lob2018/CanScan/master/.myresources/images/Miniature-tuto-CanScan.jpg" alt="Tutoriel CanScan" width="100" height="56"/>](https://youtu.be/uJBtth4vPvE)

## 📑 Contenu

- ✨ [Fonctionnalités](#-fonctionnalités)
  - [Les champs de la MECARD](#les-champs-de-la-mecard)
  - [Les champs de l'Agenda](#les-champs-de-lagenda)
  - [Le champ de Saisie libre](#le-champ-de-saisie-libre-requis)
  - [Personnalisation](#réglages-disponibles)
  - [Aperçu](#aperçu)
  - [Export](#export)
- 🪄 [Utilisation](#-utilisation)
  - [Étapes d’utilisation](#étapes-dutilisation)
  - [Vérification dynamique](#vérification-dynamique)
- 📥 [Installation et maintenance](#-installation-et-maintenance)
  - [Installation](#installation)
  - [Mise à jour](#mise-à-jour)
  - [Désinstallation](#désinstallation)
- 📘 [Informations techniques](#-informations-techniques)
  - [Technologies utilisées](#-technologies)
  - [Documentation](https://lob2018.github.io/CanScan/)
  - [Licence](#licence)

## ✨ Fonctionnalités

### Les champs de la MeCard
- **Nom, prénom** : Identité du contact (requis)
- **Téléphone** : Numéro de téléphone
- **Courriel** : Adresse électronique
- **Organisation** : Nom de l'entreprise
- **Adresse** : Adresse postale
- **Lien** : URL de site ou profil

### Les champs de l'Agenda
- **Titre** : Nom de l'événement (requis)
- **Identifiant** : Identifiant unique de l'événement au format `NOM@SOFT64.FR` (requis et généré automatiquement)
- **Début** : Date et heure de début de l'événement (requis)
- **Fin** : Date et heure de fin de l'événement (requis)
- **Nom** : Nom de l'organisateur
- **Coordonnées** : Latitude et longitude du lieu (format : `latitude;longitude`)

> 📍 Les coordonnées géographiques génèrent automatiquement deux champs : GEO (coordonnées brutes) et LOCATION (lien OpenStreetMap cliquable)

### Le champ de Saisie libre (requis)

📚 [Documentation complète des formats ZXing](https://github.com/zxing/zxing/wiki/Barcode-Contents)

| Type               | Texte à saisir                   | Action sur mobile |
|--|----------------------------------|-|
| 🌐 Site internet   | `https://soft64.fr`              | Ouvre le navigateur |
| 📞 Téléphone       | `tel:+33123456789`               | Lance l’appel |
| 📧 Email           | `mailto:contact@example.com?...` | Ouvre l’app Mail |
| 📅 Calendrier      | `BEGIN:VEVENT...END:VEVENT`      | Ajoute au calendrier |
| 📶 Wi-Fi           | `WIFI:T:...;;`                   | Connexion Wi-Fi |
| 📍 Géolocalisation | `geo:48.8566,2.3522`             | Ouvre l’app Cartes |
| 👤 Contact         | `BEGIN:VCARD...END:VCARD`        | Ajoute au répertoire |
| 💬 SMS             | `SMSTO:+33...`                   | Ouvre l’app Messages |
| 📝 Texte brut      | `Un texte à copier`              | Affiche le texte |

### Réglages disponibles
- **Logo** : Image centrale
- **Taille du logo** : 27% par défaut ⚠️
- **Marge** : 3 par défaut ⚠️
- **Couleur du fond** : Blanc ⚠️
- **Couleur des modules** : Noir ⚠️
- **Dimension** : 400x400px par défaut ⚡️
- **Modules ronds** : Optionnel ⚠️

> ⚡️ Trop grande dimension = baisse de performance
> ⚠️ Toujours tester la lisibilité du QR

### Aperçu
- **Aperçu dynamique** dans l’interface

### Export
- **PNG** haute qualité

## 🪄 Utilisation

### Étapes d’utilisation

## 🪄 Utilisation

### Étapes d'utilisation

1. **Sélectionner** le mode (Contact, Agenda ou Saisie libre)
2. **Remplir** les champs
3. **Ajouter** un logo *(optionnel)*
4. **Personnaliser** les réglages *(optionnel)*
5. **Vérifier** la lisibilité avec l'aperçu
6. **Enregistrer** en PNG, et copier les données en texte brut dans le presse-papiers

### Vérification dynamique

📱 Tester le QR avec une app mobile (appareil photo, Google Lens, etc.) avant de l’enregistrer.

## 📥 Installation et maintenance

### Installation

1. **Télécharger** la dernière version : [Releases](https://github.com/Lob2018/CanScan/releases/latest)
2. **Lancer** `CanScan-v.v.v.v-x64.exe`
3. **Suivre** les étapes : dossier, raccourci, lancement
4. **Visual C++** inclus automatiquement

> 📌 Compatible Windows 64 bits uniquement

#### Vérification du fichier *(optionnel)*

```bash
gpg --import canscan-public-key.asc
gpg --verify  CanScan-0.9.0.0-x64.exe.asc CanScan-0.9.0.0-x64.exe
```

📖 [Manuel GnuPG](https://gnupg.org/documentation/manuals/gnupg/)

### Mise à jour

CanScan détecte automatiquement les nouvelles versions et affiche un bouton de mise à jour dans l’interface.

1. **Cliquer** sur le bouton de mise à jour ou télécharger la dernière version depuis les [releases GitHub](https://github.com/Lob2018/CanScan/releases/latest).
2. **Lancer** le fichier `.exe` — la version précédente sera automatiquement reconnue.
3. **Suivre** les instructions :
    - Le raccourci est mis à jour *(si présent)*
    - La nouvelle version est disponible immédiatement

### Désinstallation

1. Ouvrir **Panneau de configuration** → *Programmes*
2. Rechercher **CanScan**
3. Cliquer sur **Désinstaller**
4. Suivre les instructions

> Tous les fichiers installés seront supprimés, y compris les raccourcis et les entrées du registre

> Le runtime Visual C++ installé avec CanScan peut rester sur le système après désinstallation.<br>Il est utilisé par d’autres applications et peut être supprimé manuellement si nécessaire.

## 📘 Informations techniques

### ️ Technologies

- Java Swing
- FlatLaf
- LGoodDatePicker
- ZXing
- Correction d'erreur niveau H
- Liberica Native Image
- VC_redist.x64.exe inclus

### Licence

GPLv3.0 License © 2025 SOFT64.FR Lob2018
📜 [Consulter la licence complète](https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme)
