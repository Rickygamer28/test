# Project notes

- Shadered (https://github.com/Noodlegamer76/Shadered): the author (Noodlegamer76) has allowed Shadered+ to use
  material from the repo as a Shadered addon. DONE 2026-10-09: the 1.21.1 branch now declares
  mod_license=Attribution-NonCommercial 4.0 International (commit 02e330c), matching master's CC BY-NC 4.0 LICENSE.txt.
  Shadered code/assets may be used: credit Noodlegamer76, link CC BY-NC 4.0, state changes, no commercial use.
  Skybox images in Shadered are by other artists (OpenGameArt): keep their credits and check their own licenses.
- Shadered 2 (https://github.com/Noodlegamer76/Shadered-2) is Forge 1.20.1 only for now; revisit when a 1.21.1 version exists.
- 2026-10-09: Shadered+ is now a Shadered 1.21.1 addon (user's choice: Shadered renderer only). Our own chunk-shader,
  Sodium and Iris code was removed; Framed Blocks camo of sky blocks no longer works. Hook points used:
  ComplexPassRenderer.add(AFTER_SKY, SkyboxRenderPass), SkyblockBatchData, SkyblockHolderEntity, RegisterShaders.get.
  Our mod license is still MIT because no Shadered code is copied; if Shadered code is copied in, change it to fit CC BY-NC 4.0.
- Sky Emitter support via mixins into SkyEmitterEntity, SkyblockHolderEntity.setBlockType, SkyEmitterRenderer.render (Shadered 1.21.1-1.5.8).
- Sky ores (user request, 2026-10-09): every sky must also have ore versions "like Shadered" — keep this for all future skies.
  Implemented generically (SkyOreBlock with `sky` block-state property, SkyOreFeature picks a random sky per vein),
  so a new sky only needs its `sky.skyblocks.<name>` lang entry; textures are Shadered's illusorite_ore / deepslate_illusorite_ore.
- 2026-10-09 (later): user asked to drop our own ores and instead add our skies everywhere Shadered uses SkyblockType.
  Now: mixins extend SkyblockType + SkyblockItemTypes enums (SKYBLOCKS_<NAME>), map them in SkyblockRegistry, and hook
  SkyblockRenderer.getData / SkyblockEntityRenderer.render / ModRenderTypes.getSkyboxTextureId. Our items are Shadered
  SkyblockItems. Illusorite ore veins therefore include our skies automatically (this replaces the earlier "every sky
  gets ores" rule: new skies get Illusorite ores for free). Our own blocks, ores and Sky Emitter patches were removed.
