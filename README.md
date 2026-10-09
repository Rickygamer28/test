# Shadered+ (NeoForge 1.21.1): an addon for Shadered

Adds four new skies to **Shadered** by Noodlegamer76: Jupiter, Cat, Twilight and Hell.
They are added as real Shadered sky types, so everything in Shadered that works with sky types works with them:
- Skyblock items (Jupiter Sky Block, ...) that place Shadered's own skyblock, drawn by Shadered's renderer.
- Shadered's filters (posterize, grayscale, ...) on those skyblocks.
- The **Sky Emitter**: right-click it with one of our skyblocks, like with Shadered's.
- **Illusorite ore** veins can hold our skies, Illusorite can roll them, and Shadered's creative tab lists the ores for them.
- Breaking, pick-block, saving and syncing go through Shadered's own code.

**Requires Shadered for 1.21.1** (built against 1.21.1-1.5.8, https://modrinth.com/mod/shadered) and everything
Shadered itself needs (GeckoLib, Sodium, ...). Our skyblock items are listed in Shadered's creative tab.
Recipes (shapeless): Jupiter = Glass + Amethyst Shard, Cat = Glass + Raw Cod, Twilight = Glass + Pink Dye, Hell = Glass + Nether Wart.

**Removing Shadered+ from a world:** Shadered can't load blocks whose sky type no longer exists. Break all
Shadered+ skies (blocks, ores, emitter settings) before removing the mod from a world.

## Models (like Shadered's Maxwell)
- **OIIA Cat** (`skyblocks:oiia_cat`): a 3D cat that faces you when placed. Give it a **redstone signal** and it plays
  its own spin-and-bob animation (from the model file, about 4.8 s loop) from the start, for as long as the signal lasts;
  without a signal it stands still. Inventory shows a flat icon, hands/item frames show the model.

How it works: Shadered's model loader reads every `models/complex/` file of every mod, so our models live in
`assets/skyblocks/models/complex/` (here `oiia_cat.glb`, converted from the original FBX in `tools/models/oiia_cat/`
with the texture embedded and reduced to 1024x1024). `OiiaCatRenderer` hands the model to Shadered's model renderer,
like Shadered's `MaxwellRenderer`. Shadered's animator only animates skeletons, and this model animates as a whole, so
`OiiaCatModel.animate` plays the file's keyframes (`OiiaCatAnimation`) itself.

To add another model: put the .glb in `models/complex/`, then copy the OIIA Cat block, block entity, renderers,
blockstate/models/loot/lang and adjust the scale. Models with a skeleton can use Shadered's `Animator` instead.

## Build
Requires JDK 21 and internet access (Gradle downloads NeoForge, Shadered from Modrinth's maven, and GeckoLib).
    gradle wrapper --gradle-version 8.10.2
    ./gradlew build      # jar in build/libs/shadered-plus-1.0.0.jar

### Build without installing anything (GitHub Actions)
Push this folder to a GitHub repository. The workflow in .github/workflows/build.yml builds the mod;
open the Actions tab, click the latest run, and download the "shadered-plus-jar" artifact.

To build against another Shadered release, change `shadered_version` in gradle.properties.

## How it hooks into Shadered (mixins, skyblocks.mixins.json)
Shadered keeps its skies in two enums, `SkyblockType` and `SkyblockItemTypes`, plus code that branches on them.
- `SkyblockTypeMixin` / `SkyblockItemTypesMixin` add `SKYBLOCKS_<NAME>` constants to both enums while they are created,
  so `values()`, `valueOf()`, saving and every loop over sky types include our skies. (The `*Invoker` mixins give
  access to the enums' private constructors.)
- `SkyblockRegistryMixin` links each new sky type to its item type (used for drops, pick block, Sky Emitter).
- `SkyblockRendererMixin` (`getData`), `SkyblockEntityRendererMixin` (its switch over sky types) and
  `ModRenderTypesMixin` (`getSkyboxTextureId`, used by item render types) send our sky types to our own batch data
  and skybox textures.
- `client/ShaderedSkies` registers one `SkyboxRenderPass` per sky with Shadered's `ComplexPassRenderer`, the same way
  Shadered's `SkyblockRenderer` registers its own skies.
- `ModItems` registers one Shadered `SkyblockItem` per sky (placing Shadered's skyblock).

These mixins target Shadered 1.21.1-1.5.8. If a Shadered update changes those classes, the game stops at startup
with a mixin error, and the mixins need updating.

## Add your own sky
1. Put six square cube-map faces in `tools/skies/<name>_sky_block/px.png nx.png py.png ny.png pz.png nz.png`
   (+X east, -X west, +Y up, -Y down, +Z south, -Z north). From a 2:1 panorama (needs Python, numpy, Pillow):
       python tools/equirect_to_cubemap.py my_sky.png my_sky_block 1024
   then move the six files into that folder with the short names.
2. Add `<name>_sky_block` as a new last line of `tools/skies/order.txt`.
3. Run `python tools/build_shadered_faces.py`. It writes Shadered's skybox layout to
   `textures/environment/<name>/` (front/back/left/right/top/bottom, matching Shadered's default SkyboxTranslation).
4. Add `<name>` to `SkyNames.NAMES`, in the same position as in order.txt.
5. Add a 16x16 icon `textures/block/<name>_sky_block.png` (particle texture), an item model (copy one),
   lang entries `item.skyblocks.<name>_sky_block` and `skyblock_type.skyblocks.<name>`, and optionally a recipe.
   It then automatically becomes a Shadered sky type: skyblock, Sky Emitter, filters, Illusorite ore veins.

## Limitations
- Framed Blocks can't use skyblocks as camo (Shadered 1.21.1 draws them with block entities).
- Shader packs, Sodium and so on: whatever Shadered supports.

## Credits
Requires and builds on **Shadered** by Noodlegamer76 (https://github.com/Noodlegamer76/Shadered), licensed CC BY-NC 4.0
(master branch LICENSE.txt; the 1.21.1 branch's mod_license since commit 02e330c, 9 Oct 2026). Made with the author's
permission. Shadered+ is an unofficial addon. It patches Shadered's classes at runtime (mixins) and contains no copied
Shadered code or assets. If Shadered code or assets are copied in later: credit Noodlegamer76, link the license,
say what was changed, no commercial use, and keep the original artists' credits for any skybox images.

## Model credit
OIIA Cat: the "Oiiaioooooiai" cat model (file `OiiaioooooiaiFin.fbx`, texture `Muchkin2_BaseColor.png`) was supplied
by the user. Add its source, author and license here (most Sketchfab models require a credit line).

## Image credit
The Jupiter sky (tools/skies/jupiter_sky_block/) is a user-supplied cube map, as are the Cat panorama
(tools/source/cat_sky_block.png), the Twilight cube map and the Hell cube map. Add each source, author and license here
(some images, for example CC BY ones, require a credit line).
