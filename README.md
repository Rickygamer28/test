# Shadered+ (NeoForge 1.21.1): an addon for Shadered

Adds four more skyblocks to **Shadered** by Noodlegamer76: Jupiter, Cat, Twilight and Hell Sky Blocks.
They are drawn by Shadered's own skyblock renderer, so they look and behave like Shadered's skyblocks.
Shadered's filter items work on them too (hold one in the other hand while placing, or use it on a placed block).

**Requires Shadered for 1.21.1** (built against 1.21.1-1.5.8, https://modrinth.com/mod/shadered) and everything
Shadered itself needs (GeckoLib, Sodium, ...).

All blocks are listed in Shadered's own creative tab, after Shadered's items.
Recipes (shapeless): Jupiter = Glass + Amethyst Shard, Cat = Glass + Raw Cod, Twilight = Glass + Pink Dye, Hell = Glass + Nether Wart.

## Build
Requires JDK 21 and internet access (Gradle downloads NeoForge and Shadered from Modrinth's maven).
    gradle wrapper --gradle-version 8.10.2
    ./gradlew build      # jar in build/libs/shadered-plus-1.0.0.jar

### Build without installing anything (GitHub Actions)
Push this folder to a GitHub repository. The workflow in .github/workflows/build.yml builds the mod;
open the Actions tab, click the latest run, and download the "shadered-plus-jar" artifact.

To build against another Shadered release, change `shadered_version` in gradle.properties.

## How it hooks into Shadered
Shadered registers its skies in `SkyblockRenderer` (one `SkyblockBatchData` + one `SkyboxRenderPass` per sky).
Shadered+ does the same for its own skies, in `client/ShaderedSkies`:
- At client setup, one `SkyboxRenderPass` per sky is added to Shadered's `ComplexPassRenderer` (stage AFTER_SKY).
  Each frame it draws the sky from `textures/environment/<sky>/front|back|left|right|top|bottom.png` into its own
  screen-sized target, and registers that target in `SkyblockRenderer.DATA_LIST`.
- `SkyBlockEntity` extends Shadered's `SkyblockHolderEntity`, so it stores Shadered's filter pass.
  `SkyBlockRenderer` (the block entity renderer) adds each block to its sky's batch data, like Shadered's
  `SkyblockEntityRenderer`. Shadered's `SkyblockRenderPass` then draws all of them with its skyblock shaders.
- Items (`SkyBlockItemRenderer`) draw a cube with Shadered's skyblock shader, its "Skybox" sampler set to our
  sky's target, like Shadered's own skyblock item render types.

## Add your own sky
1. Put six square cube-map faces in `tools/skies/<name>_sky_block/px.png nx.png py.png ny.png pz.png nz.png`
   (+X east, -X west, +Y up, -Y down, +Z south, -Z north). From a 2:1 panorama (needs Python, numpy, Pillow):
       python tools/equirect_to_cubemap.py my_sky.png my_sky_block 1024
   then move the six files into that folder with the short names.
2. Add `<name>_sky_block` as a new last line of `tools/skies/order.txt`.
3. Run `python tools/build_shadered_faces.py`. It writes Shadered's skybox layout to
   `textures/environment/<name>/` (front/back/left/right/top/bottom, matching Shadered's default SkyboxTranslation).
4. Add a 16x16 icon `textures/block/<name>_sky_block.png` (used for breaking particles).
5. Copy the blockstate, block model, item model, loot table and recipe of an existing sky block, add its lang name
   and add it to the pickaxe tag.
6. Add the name and a map color to SKY_NAMES / SKY_COLORS in ModBlocks, in the same position as in order.txt.

## Limitations
- Because the blocks are drawn by Shadered's renderer (block entities), Framed Blocks can't use them as camo.
- Shader packs, Sodium and so on: whatever Shadered supports, these blocks support.
- Worlds made with older Shadered+ versions (no block entity): re-place those sky blocks.

## Credits
Requires and builds on **Shadered** by Noodlegamer76 (https://github.com/Noodlegamer76/Shadered), licensed CC BY-NC 4.0
(master branch LICENSE.txt; the 1.21.1 branch's mod_license since commit 02e330c, 9 Oct 2026). Made with the author's
permission. Shadered+ is an unofficial addon. It uses Shadered's public classes at runtime and contains no copied
Shadered code or assets. If Shadered code or assets are copied in later: credit Noodlegamer76, link the license,
say what was changed, no commercial use, and keep the original artists' credits for any skybox images.

## Image credit
The Jupiter Sky Block faces (tools/skies/jupiter_sky_block/) are a user-supplied cube map,
as are the Cat panorama (tools/source/cat_sky_block.png), the Twilight cube map and the Hell cube map. Add each source,
author and license here (some images, for example CC BY ones, require a credit line).
