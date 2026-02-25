#!/bin/bash
# Script d'aide à la construction native pour GraalVM sous Linux (Bash)
# Cible : Compilation Standard GLibC + Packaging AppImage

# Les dépendances nécessaires pour la compilation doivent être installées sur la machine hôte:
# sudo apt update
# sudo apt install build-essential libz-dev zlib1g-dev libxext-dev libxi-dev libxrender-dev libxtst-dev

# Capture du répertoire racine du projet (avant tout changement de répertoire)
PROJECT_ROOT="$PWD"

#---------------------------------------------
# Configuration et Arguments
#---------------------------------------------
APP_NAME="$1"
APP_VERSION="$2"
ORG_NAME="$3"
MAIN_CLASS="$4"
LANG_CODE="$5"
COUNTRY_CODE="$6"

# Vérification du nombre d'arguments
if [ "$#" -ne 6 ]; then
    echo "Usage: $0 <AppName> <Version> <Organization> <MainClass> <Lang> <Country>"
    exit 1
fi

# GUID/AppID pour le packaging (Windows/AppImage Bundle ID)
APP_GUID="AA74CA9F-E961-4A51-A0CB-228B63029F74"

# ID de classe WM pour l'intégration de bureau Linux (Format Reverse Domain Name recommandé)
APP_ID_BASE="${ORG_NAME}.${APP_NAME}"
APP_ID_DOMAIN=$(echo "$LANG_CODE.$APP_ID_BASE" | tr '[:upper:]' '[:lower:]')

# --- Nom canonique pour le binaire (canscan en minuscules) ---
CANONICAL_NAME="canscan"

echo "-----------------------------------------------"
echo "[INFO] Application name           : $APP_NAME"
echo "[INFO] App ID GUID                : $APP_GUID"
echo "[INFO] Nom de domaine AppID       : $APP_ID_DOMAIN"
echo "[INFO] Version                    : $APP_VERSION"
echo "[INFO] Nom du binaire généré      : $CANONICAL_NAME-$APP_VERSION"
echo "[INFO] Cible de compilation       : Linux Standard (GLibC)"
echo "-----------------------------------------------"
echo

echo
echo "[1/5] Cleaning previous config-trace, dist, output..."
rm -rf config-trace dist output 2>/dev/null
mkdir -p config-trace dist output

echo
echo "[2/5] Moved to dist..."
cd dist || { echo "Failure of cd dist"; exit 1; }

echo
echo "[3/5] Simulating runtime usage to generate native-image config-trace..."
java --enable-native-access=ALL-UNNAMED \
     -agentlib:native-image-agent=config-output-dir=../config-trace \
     -Djava.awt.headless=false \
     -Dawt.appname="$CANONICAL_NAME" \
     -Dsun.java2d.xrender=true \
     -Dsun.java2d.uiScale.enabled=true \
     -Duser.language="$LANG_CODE" \
     -Duser.country="$COUNTRY_CODE" \
     -Duser.region="$COUNTRY_CODE" \
     -cp "../target/canscan-$APP_VERSION.jar:../target/test-classes" \
          fr.softsf.canscan.NativeImageConfigSimulator

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Configuration simulation failed"
    exit 1
fi

## 🏗️ Étape 4/5 : Compilation Native (GLibC Standard)

echo
echo "[4/5] Building native image (GLibC Standard)..."
echo "=========================================================="
echo
native-image --enable-native-access=ALL-UNNAMED \
    --no-fallback \
    --strict-image-heap \
    -march=compatibility \
    -H:+UnlockExperimentalVMOptions \
    -H:IncludeLocales="$LANG_CODE" \
    -H:ConfigurationFileDirectories=../.myresources/scripts/config-manual/linux,../config-trace \
    -H:Name=$CANONICAL_NAME \
    -H:Class="$MAIN_CLASS" \
    -Dawt.appname="$CANONICAL_NAME" \
    -Duser.language="$LANG_CODE" \
    -Duser.country="$COUNTRY_CODE" \
    -Duser.region="$COUNTRY_CODE" \
    -Djava.awt.headless=false \
    -Dsun.java2d.xrender=true \
    -Dsun.java2d.uiScale.enabled=true \
    -J-Xmx7G \
    -jar "../target/canscan-$APP_VERSION.jar"

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Native image compilation failed."
    exit 1
fi

# RETOUR À LA RACINE DU PROJET
cd ..

## 💾 Étape 5/5 : Création de l'AppImage

echo
echo "[5/5] Packaging as AppImage..."
echo

APP_DIR="AppDir"
OUTPUT_APPIMAGE="output/$APP_NAME-$APP_VERSION-x86_64.AppImage"
ICON_SOURCE_PATH="$PROJECT_ROOT/.myresources/images/CanScanx256.png"

# --- VÉRIFICATION DU BINAIRE ---
# MAINTIENT LE CHEMIN DE L'ANCIEN SCRIPT
EXECUTABLE_PATH="dist/$CANONICAL_NAME"
if [ ! -f "$EXECUTABLE_PATH" ]; then
    echo
    echo "[CRITICAL ERROR] Le binaire natif $EXECUTABLE_PATH n'a pas été trouvé. (Vérifiez la casse du nom ou la compilation GraalVM)."
    exit 1
fi
# -----------------------------------------------------

# 1. Nettoyage et Création de la structure AppDir standard
rm -rf "$APP_DIR"
mkdir -p "$APP_DIR/usr/bin"
mkdir -p "$APP_DIR/usr/lib"

# 2. Copie de l'exécutable
echo "[INFO] Copying executable ($EXECUTABLE_PATH)..."
cp "$EXECUTABLE_PATH" "$APP_DIR/usr/bin/$CANONICAL_NAME"

# 3. Copie des librairies dynamiques (.so)
echo "[INFO] Copying dynamic libraries..."
cp dist/*.so "$APP_DIR/usr/lib/" 2>/dev/null

# 4. Création du script AppRun (Le point d'entrée) - Injecte les AppID XDG/GTK
echo "[INFO] Creating AppRun and forcing XDG/GTK Application ID..."
echo '#!/bin/sh' > "$APP_DIR/AppRun"
echo 'SELF=$(dirname "$(readlink -f "$0")")' >> "$APP_DIR/AppRun"
echo 'export LD_LIBRARY_PATH="$SELF/usr/lib:$LD_LIBRARY_PATH"' >> "$APP_DIR/AppRun"

# Exporte les variables pour forcer l'ID de l'application (le nom) sur les environnements modernes
echo "export XDG_CURRENT_DESKTOP=GNOME" >> "$APP_DIR/AppRun"
echo "export XDG_APPLICATION_ID=$APP_ID_DOMAIN" >> "$APP_DIR/AppRun"
echo "export GDK_APPLICATION_ID=$APP_ID_DOMAIN" >> "$APP_DIR/AppRun"
# SUPPRESSION: echo "export GDK_SET_WMCLASS=$CANONICAL_NAME" >> "$APP_DIR/AppRun"

echo "exec \"\$SELF/usr/bin/$CANONICAL_NAME\" \"\$@\"" >> "$APP_DIR/AppRun"
chmod +x "$APP_DIR/AppRun"

# 5. Création du fichier .desktop
echo "[INFO] Creating .desktop file (using WM Class: $APP_NAME)..."
cat > "$APP_DIR/$CANONICAL_NAME.desktop" <<EOF
[Desktop Entry]
Name=$APP_NAME
Exec=AppRun
Icon=$CANONICAL_NAME
Type=Application
Categories=Graphics;
Comment=Application $APP_NAME Version $APP_VERSION
StartupWMClass=$CANONICAL_NAME
Terminal=false
Keywords=qrcode;
X-AppImage-Version=$APP_VERSION
X-AppImage-Identifier=$APP_ID_DOMAIN
EOF

# 6. Gestion de l'icône
echo "[INFO] Copying icon from $ICON_SOURCE_PATH..."
if [ -f "$ICON_SOURCE_PATH" ]; then
    cp "$ICON_SOURCE_PATH" "$APP_DIR/$CANONICAL_NAME.png"
else
    echo "[ERROR] Icon file '$ICON_SOURCE_PATH' not found. AppImage build may fail or use a default icon."
    touch "$APP_DIR/$CANONICAL_NAME.png"
fi

# 7. Téléchargement de l'outil appimagetool
APPIMAGETOOL_PATH="dist/appimagetool"
if [ ! -f "$APPIMAGETOOL_PATH" ]; then
    echo "[INFO] Downloading appimagetool..."
    mkdir -p dist
    wget -q https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage -O "$APPIMAGETOOL_PATH"
    chmod +x "$APPIMAGETOOL_PATH"
fi

# 8. Génération de l'AppImage final (SANS SIGNATURE)
echo "[INFO] Building final AppImage file (unsigned)..."
ARCH=x86_64 APPIMAGE_BUNDLE_ID="$APP_GUID" VERSION="$APP_VERSION" "$APPIMAGETOOL_PATH" --no-appstream "$APP_DIR" "$OUTPUT_APPIMAGE"

if [ $? -eq 0 ]; then
    echo
    echo "----------------------------------------------------------------"
    echo "[SUCCESS] AppImage généré avec succès !"
    echo "          Fichier : $OUTPUT_APPIMAGE"
    echo "          Note    : AppImage NON SIGNÉE (pas de GPG)"
    echo "----------------------------------------------------------------"

    # Nettoyage complet (AppDir, dist et appimagetool)
    echo "[INFO] Cleaning temporary files..."
    rm -rf "$APP_DIR" dist

else
    echo "[ERROR] Échec de la création de l'AppImage. (Vérifiez la sortie de l'outil AppImageTool pour la raison de l'échec)."
    exit 1
fi

exit 0