# Sky Blocks (NeoForge 1.21.1)

Decorative blocks that show a skybox on their faces through a custom core shader,
like a window into another sky. Three blocks: Night, Sunset and Nebula.

Recipes (shapeless): Glass + Lapis Lazuli / Orange Dye / Amethyst Shard.

## Build & run
Requires JDK 21. Add the Gradle wrapper first:
    gradle wrapper --gradle-version 8.10.2
    ./gradlew runClient
    ./gradlew build      # jar in build/libs/

## Add your own sky
1. Add a 2:1 equirectangular (panorama) PNG: assets/skyblocks/textures/sky/<name>_sky_block.png
2. Add a 16x16 icon: assets/skyblocks/textures/block/<name>_sky_block.png
3. Copy the blockstate, block model, item model, loot table and lang entry of an existing block.
4. Register it in ModBlocks, add it to ModBlockEntities, the pickaxe tag and the creative tab.

## How it works
- SkyBlock uses RenderShape.ENTITYBLOCK_ANIMATED, so it is drawn by SkyBlockRenderer.
- The renderer draws the 6 faces with a custom RenderType using shaders/core/sky_block.*
- The fragment shader turns the view direction into longitude/latitude and samples the texture,
  so the sky stays fixed in the world no matter where the block is.
- Core shaders don't work with Iris/Oculus shader packs.

## Build without installing anything (GitHub Actions)
Push this folder to a GitHub repository. The workflow in .github/workflows/build.yml builds the mod;
open the Actions tab, click the latest run, and download the "skyblocks-jar" artifact.
