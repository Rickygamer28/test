#!/usr/bin/env python3
"""
Builds the sky textures the mod actually loads. Run it again whenever you add or change a sky.

  python tools/build_sky_atlas.py          (needs Pillow)

Input:   tools/skies/order.txt             one block name per line; the line number is the sky's INDEX
         tools/skies/<name>/<face>.png     six square cube-map faces: px nx py ny pz nz
                                           (+X, -X, +Y up, -Y down, +Z, -Z), all the same size

Output:  src/main/resources/assets/skyblocks/textures/sky/atlas_<face>.png
             six tall images. Sky number k occupies rows [k*S, (k+1)*S) of every one of them.
             The shaders read them with Sampler3..Sampler8.
         src/main/resources/assets/skyblocks/textures/block/<name>.png
             the 16x16 block icon, rewritten with a MARKER ALPHA = 240 - 8*k.
             The patched chunk shader (assets/minecraft/shaders/core/rendertype_solid.fsh) finds sky
             blocks by that alpha, and k tells it which row of the atlases to use.
             The icon's colors are left alone, so without the patched shader the block is still a
             normal, fully visible block showing the icon.

The index of a sky must match the number passed to `new SkyBlock(props, index)` in ModBlocks.
"""
import os
import sys
from PIL import Image

FACES = ["px", "nx", "py", "ny", "pz", "nz"]
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
RES = os.path.join(ROOT, "src", "main", "resources", "assets", "skyblocks", "textures")
MAX_SKIES = 12          # alpha 240 - 8*11 = 152 is the lowest marker the shader accepts


def marker_alpha(index):
    return 240 - 8 * index


def main():
    with open(os.path.join(HERE, "skies", "order.txt")) as f:
        names = [line.strip() for line in f if line.strip()]
    if len(names) > MAX_SKIES:
        sys.exit("Too many skies (max %d)" % MAX_SKIES)

    # Every face of every sky is resized to the largest face found, so the atlases are uniform.
    size = 0
    for name in names:
        for face in FACES:
            w, h = Image.open(os.path.join(HERE, "skies", name, face + ".png")).size
            if w != h:
                sys.exit("%s/%s.png is not square (%dx%d)" % (name, face, w, h))
            size = max(size, w)

    os.makedirs(os.path.join(RES, "sky"), exist_ok=True)
    for face in FACES:
        atlas = Image.new("RGBA", (size, size * len(names)), (0, 0, 0, 255))
        for k, name in enumerate(names):
            img = Image.open(os.path.join(HERE, "skies", name, face + ".png")).convert("RGBA")
            if img.size[0] != size:
                img = img.resize((size, size), Image.LANCZOS)
            atlas.paste(img, (0, k * size))
        atlas.save(os.path.join(RES, "sky", "atlas_%s.png" % face), optimize=True)
        print("atlas_%s.png  %dx%d" % (face, atlas.size[0], atlas.size[1]))

    for k, name in enumerate(names):
        path = os.path.join(RES, "block", name + ".png")
        icon = Image.open(path).convert("RGBA")
        icon.putalpha(marker_alpha(k))
        icon.save(path)
        print("%-20s index %d  marker alpha %d" % (name, k, marker_alpha(k)))


if __name__ == "__main__":
    main()
