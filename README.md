# Shadered+ (NeoForge 1.21.1)

Decorative blocks that show a skybox on their faces through a custom core shader,
like a window into another sky. Four blocks: Jupiter, Cat, Twilight and Hell Sky Blocks.

All blocks are in their own creative tab, "Shadered+" (new blocks added to ModBlocks.ITEMS appear there automatically).

Recipes (shapeless): Jupiter = Glass + Amethyst Shard, Cat = Glass + Raw Cod, Twilight = Glass + Pink Dye, Hell = Glass + Nether Wart.

## Build & run
Requires JDK 21. Add the Gradle wrapper first:
    gradle wrapper --gradle-version 8.10.2
    ./gradlew runClient
    ./gradlew build      # jar in build/libs/shadered-plus-1.0.0.jar

## Build without installing anything (GitHub Actions)
Push this folder to a GitHub repository. The workflow in .github/workflows/build.yml builds the mod;
open the Actions tab, click the latest run, and download the "shadered-plus-jar" artifact.

## Add your own sky
Each sky is a cube map: six square face images, named `<block_name>_<face>.png` in
`assets/skyblocks/textures/sky/` with faces `px nx py ny pz nz` (+X, -X, +Y up, -Y down, +Z, -Z).
1. Make the faces from a 2:1 panorama with the included tool (needs Python, numpy, Pillow):
       python tools/equirect_to_cubemap.py my_sky.png my_block_sky_block 1024
   or drop in six faces you already have, using the names above.
2. Add a 16x16 icon: assets/skyblocks/textures/block/<block_name>.png
3. Copy the blockstate, block model, item model, loot table, recipe and lang entry of an existing block.
4. Register it in ModBlocks, add it to ModBlockEntities, the pickaxe tag, the creative tab and the
   item extensions list in ClientSetup.

## How it works
- SkyBlock uses RenderShape.ENTITYBLOCK_ANIMATED, so it is drawn by SkyBlockRenderer.
- The renderer draws the visible faces with a custom RenderType using shaders/core/sky_block.*
- The fragment shader rebuilds the world-space view direction of each pixel from ProjMat, ModelViewMat and
  ScreenSize, then looks it up in the cube map: the dominant axis picks one of the six face textures
  (bound to Sampler0..Sampler5) and the other two components give the position on that face. This is a
  window onto a sky at infinity: it stays fixed in the world, and view bobbing (which lives in ProjMat)
  does not disturb it.
- Core shaders don't work with Iris/Oculus shader packs.

## Item rendering
The item (inventory, hand, dropped, item frames) is drawn by SkyBlockItemRenderer with shaders/core/sky_item.*.
Items are drawn with their own camera matrices, so SkyMatrices copies the world camera rotation and inverse
projection into the item shader once per frame (RenderLevelStageEvent, AFTER_SKY). The item shows the sky as
seen at its position on screen, so it changes as you look around, like the placed block.

## Image credit
The Jupiter Sky Block faces (assets/skyblocks/textures/sky/jupiter_sky_block_*.png) are a user-supplied cube map,
as are the Cat panorama (tools/source/cat_sky_block.png), the Twilight cube map and the Hell cube map. Add each source, author and
license here (some images, for example CC BY ones, require a credit line).
