# Sky Blocks (NeoForge 1.21.1)

Decorative blocks that show a skybox on their faces through a custom core shader,
like a window into another sky. One block: the Milky Way Sky Block.

Recipe (shapeless): Glass + Amethyst Shard.

## Build & run
Requires JDK 21. Add the Gradle wrapper first:
    gradle wrapper --gradle-version 8.10.2
    ./gradlew runClient
    ./gradlew build      # jar in build/libs/

## Build without installing anything (GitHub Actions)
Push this folder to a GitHub repository. The workflow in .github/workflows/build.yml builds the mod;
open the Actions tab, click the latest run, and download the "skyblocks-jar" artifact.

## Add your own sky
1. Add a 2:1 equirectangular (panorama) PNG: assets/skyblocks/textures/sky/<name>_sky_block.png
2. Add a 16x16 icon: assets/skyblocks/textures/block/<name>_sky_block.png
3. Copy the blockstate, block model, item model, loot table and lang entry of an existing block.
4. Register it in ModBlocks, add it to ModBlockEntities, the pickaxe tag and the creative tab.

## How it works
- SkyBlock uses RenderShape.ENTITYBLOCK_ANIMATED, so it is drawn by SkyBlockRenderer.
- The renderer draws the visible faces with a custom RenderType using shaders/core/sky_block.*
- The fragment shader rebuilds the world-space view direction of each pixel from ProjMat, ModelViewMat and
  ScreenSize, then samples the equirectangular image (longitude/latitude). This is a window onto a sky at
  infinity: it stays fixed in the world, and view bobbing (which lives in ProjMat) does not disturb it.
- Core shaders don't work with Iris/Oculus shader packs.

## Item rendering
The item (inventory, hand, dropped, item frames) is drawn by SkyBlockItemRenderer with shaders/core/sky_item.*.
Items are drawn with their own camera matrices, so SkyMatrices copies the world camera rotation and inverse
projection into the item shader once per frame (RenderLevelStageEvent, AFTER_SKY). The item shows the sky as
seen at its position on screen, so it changes as you look around, like the placed block.

## Image credit
The Milky Way sky image (assets/skyblocks/textures/sky/milky_way_sky_block.png) is a user-supplied panorama.
Add its author, source and license here (some panoramas, for example CC BY ones, require a credit line).
