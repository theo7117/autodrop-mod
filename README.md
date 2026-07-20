# Auto Dropper (Fabric 1.21.4)

Mod client Fabric pour Minecraft **1.21.4**.

## Fonctionnement

1. Tu appuies sur la touche **H** (par défaut, modifiable dans *Options > Touches* sous la catégorie
   "Auto Dropper") → ça ouvre le menu de configuration du mod.
2. Dans ce menu :
   - Clique sur le bouton **"Touche d'activation"**, puis appuie sur la touche de ton choix
     (ex : `I`). C'est cette touche qui activera/désactivera l'auto-drop en jeu.
   - Renseigne le champ **"Intervalle (en secondes)"** (ex : `60` pour 1 minute).
   - Clique sur **"Sauvegarder et quitter"**.
3. En jeu, tu appuies sur la touche d'activation choisie (ex : `I`) → l'auto-drop se lance :
   toutes les X secondes configurées, l'objet actuellement dans la main est lâché (comme un
   appui sur la touche de drop `Q`). Un nouvel appui sur la même touche le désactive.

La configuration est sauvegardée dans `config/autodropper.json` et est donc conservée d'une
session à l'autre.

> ⚠️ Ce mod automatise une action répétitive (lâcher un objet). Certains serveurs interdisent les
> mods d'automatisation/macros dans leurs règles — vérifie les règles du serveur sur lequel tu
> comptes l'utiliser avant de t'en servir.

## Compiler le mod

### Option A — via GitHub Actions (automatique)

1. Crée un dépôt GitHub et pousse (`push`) tout le contenu de ce dossier dedans.
2. Le workflow `.github/workflows/build.yml` se déclenche automatiquement à chaque `push`.
3. Une fois le build terminé (onglet **Actions** du dépôt), télécharge le fichier `.jar` généré
   depuis l'artifact **"autodropper"** de l'exécution du workflow.

### Option B — en local

Prérequis : **JDK 21** installé.

```bash
# Si tu n'as pas Gradle installé, tu peux d'abord générer le wrapper (une seule fois,
# avec une installation locale de Gradle 8.10+) :
gradle wrapper --gradle-version 8.10

# Puis, à chaque compilation :
./gradlew build        # Linux / macOS
gradlew.bat build       # Windows
```

Le fichier compilé se trouve ensuite dans `build/libs/autodropper-1.0.0.jar`.

## Installer le mod

1. Installe [Fabric Loader](https://fabricmc.net/use/) pour Minecraft 1.21.4.
2. Installe [Fabric API](https://modrinth.com/mod/fabric-api) (version `1.21.4`) dans le dossier
   `mods`.
3. Place le `.jar` compilé du mod dans le dossier `mods` de ton instance Minecraft.

## Structure du projet

```
autodropper/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── .github/workflows/build.yml   # compilation automatique
└── src/main/
    ├── java/com/example/autodropper/
    │   ├── AutoDropperClient.java   # touches, boucle de tick, logique de drop
    │   ├── Config.java              # sauvegarde/chargement config/autodropper.json
    │   └── gui/ConfigScreen.java    # menu de configuration
    └── resources/
        ├── fabric.mod.json
        └── assets/autodropper/lang/  # fr_fr.json, en_us.json
```

## Personnalisation rapide

- Faire lâcher **toute la pile** au lieu d'un seul objet : dans `Config.java`, passe
  `dropWholeStack` à `true` par défaut (ou ajoute un bouton dans `ConfigScreen` pour le
  basculer, sur le même modèle que le bouton "État actuel").
- Changer la touche par défaut du menu (`H`) : modifie `GLFW.GLFW_KEY_H` dans
  `AutoDropperClient.java`.
