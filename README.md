# Shadered+ (NeoForge 1.21.1)

Decorative blocks that show a skybox on their faces through a shader,
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
Each sky is a cube map: six square face images `px nx py ny pz nz` (+X, -X, +Y up, -Y down, +Z, -Z).
1. Put the faces in `tools/skies/<block_name>/px.png` ... `nz.png`. To make them from a 2:1 panorama
   (needs Python, numpy, Pillow):
       python tools/equirect_to_cubemap.py my_sky.png my_block_sky_block 1024
   and move the six files into that folder with the short names.
2. Add a 16x16 icon: `src/main/resources/assets/skyblocks/textures/block/<block_name>.png`
3. Add `<block_name>` as a new last line of `tools/skies/order.txt`. Its line number (from 0) is the sky index.
4. Run `python tools/build_sky_atlas.py`. It rebuilds `textures/sky/atlas_*.png` and writes the
   marker alpha into every icon. Run it again after any change to a sky or an icon.
5. Copy the blockstate, block model, item model, loot table and recipe of an existing sky block, add its lang
   name and add it to the pickaxe tag.
6. Add the name and a map color to SKY_NAMES / SKY_COLORS in ModBlocks, in the same position as in order.txt.
   The item renderer and the creative tab entry follow automatically. Up to 12 skies.

## How it works
- Sky blocks are ordinary solid cubes with a normal `cube_all` model. No block entity, no special renderer.
- The mod ships a patched copy of the vanilla chunk shader, `assets/minecraft/shaders/core/rendertype_solid.*`,
  which replaces vanilla's through the normal resource system (no mixins).
- The sky block textures have a marker alpha of 240 - 8 x index (invisible, since solid blocks ignore alpha).
  For each pixel, the patched shader reads the exact texel under it. When it finds a marker, it rebuilds the
  world-space direction of that pixel from ProjMat, ModelViewMat and ScreenSize and looks it up in sky number
  `index` of the six atlases (Sampler3..Sampler8). Every other block is drawn exactly as in vanilla.
- The result is a window onto a sky at infinity. It stays fixed in the world, and view bobbing (which lives in
  ProjMat) does not disturb it. Faces are culled like any full block.
- Once per frame (RenderLevelStageEvent AFTER_SKY, just before the terrain), SkyAtlas puts the atlases into
  texture slots 3..8.

## Sodium
Sodium reads its terrain shader straight from its own jar, so the resource override above can't reach it.
Two optional mixins (`skyblocks.mixins.json`, only applied when Sodium is installed) handle it instead:
- `SodiumShaderLoaderMixin` patches `sodium:blocks/block_layer_opaque.fsh` as Sodium loads it
  (compat/sodium/SodiumShaderPatch adds the same marker check and sky lookup, from shaders/sodium/sky_lookup.glsl).
  If the shader doesn't look as expected, it is left untouched.
- `SodiumShaderInterfaceMixin` runs after Sodium sets up each terrain pass. It binds the sky atlases and the screen size,
  and turns the sky off in passes with blending (translucent), so stained glass and similar are never mistaken for a sky.

Sodium warns about resource packs that replace `rendertype_solid`, because it never uses that shader.
`pack.mcmeta` lists our two files under `sodium.ignored_shaders` (Sodium's own opt-out), so the warning
doesn't appear. The override still works without Sodium.

## Iris shader packs (best effort)
With a shader pack on, Iris builds the terrain shaders from the pack's `gbuffers_terrain`. Two more optional mixins:
- `IrisSodiumProgramsMixin` patches the solid and cutout terrain programs right before Iris compiles them
  (compat/iris/IrisShaderPatch). The pack's `main()` is renamed and wrapped. The vertex side passes the atlas UV on,
  and the fragment side replaces the pack's first colour output (location 0, usually albedo) with the sky where it
  finds a marker. Shadow and water programs are not touched.
- `IrisSodiumShaderMixin` binds the atlases and uniforms on the highest free texture units after Iris sets up each pass.
The pack still lights, fogs and post-processes those pixels its own way, so the sky looks a bit different in every
pack (darker at night or in caves, for example). Packs with geometry/tessellation terrain shaders, or without a
location-0 output, are left unpatched (icon texture). The held/inventory item may not show the sky with a pack on.

## Framed Blocks
Because the sky is part of the block's ordinary model and texture, a Framed Blocks frame that uses a sky block
as camo copies those quads. The patched shader then draws the sky on it, on slopes, slabs, panels and so on,
with no Framed-specific code. Only the placed frame shows the sky; a framed block as an item in the
inventory shows the plain icon.

## Limitations
- Works with the vanilla chunk renderer, with Sodium for NeoForge (checked against the 0.6.13 and 0.8.13 shader
  sources for 1.21.1) and, best effort, with Iris shader packs (see below). Not supported: Embeddium. Oculus has no
  1.21.1 version; on NeoForge 1.21.1 shader packs run through Iris itself.
- Another mod or resource pack that also replaces `rendertype_solid` will conflict. Whichever one loads on top wins.
- A solid-layer texture from another mod with a pixel alpha between about 150 and 244 would also be treated as
  a sky. Vanilla opaque textures are always 255.
- Worlds saved with the older version had a block entity in each sky block. On load the game logs a warning
  and drops that leftover data. The blocks themselves are kept.

## Item rendering
The item (inventory, hand, dropped, item frames) is drawn by SkyBlockItemRenderer with shaders/core/sky_item.*,
which reads the same atlases (the vertex color's red channel carries the sky index).
Items are drawn with their own camera matrices, so SkyMatrices copies the world camera rotation and inverse
projection into the item shader once per frame. The item shows the sky as seen at its position on screen,
so it changes as you look around, like the placed block.

## Image credit
The Jupiter Sky Block faces (tools/skies/jupiter_sky_block/) are a user-supplied cube map,
as are the Cat panorama (tools/source/cat_sky_block.png), the Twilight cube map and the Hell cube map. Add each source, author and
license here (some images, for example CC BY ones, require a credit line).
