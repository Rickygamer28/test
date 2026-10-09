#!/usr/bin/env python3
"""
Convert an equirectangular (2:1 panorama) image into the six cube-map faces the mod uses.

    python tools/equirect_to_cubemap.py <panorama.png> <block_name> [face_size]

Writes src/main/resources/assets/skyblocks/textures/sky/<block_name>_{px,nx,py,ny,pz,nz}.png
Needs: Python 3, numpy, Pillow.

The face layout matches shaders/core/sky_block.fsh and sky_item.fsh (OpenGL cube-map convention,
image row 0 = top). If you already have six cube-map face images, skip this tool and just
name them <block_name>_px.png ... _nz.png (see README).
"""
import os
import sys
import numpy as np
from PIL import Image

# face -> function(sc, tc) giving the 3D direction (x, y, z) for that face
FACES = {
    "px": lambda sc, tc: (np.ones_like(sc), -tc, -sc),
    "nx": lambda sc, tc: (-np.ones_like(sc), -tc, sc),
    "py": lambda sc, tc: (sc, np.ones_like(sc), tc),
    "ny": lambda sc, tc: (sc, -np.ones_like(sc), -tc),
    "pz": lambda sc, tc: (sc, -tc, np.ones_like(sc)),
    "nz": lambda sc, tc: (-sc, -tc, -np.ones_like(sc)),
}


def sample_equirect(img, d):
    """Bilinear lookup in an equirectangular image (wraps horizontally)."""
    h, w, _ = img.shape
    x, y, z = d
    n = np.sqrt(x * x + y * y + z * z)
    u = np.arctan2(z, x) / (2 * np.pi) + 0.5
    v = 0.5 - np.arcsin(np.clip(y / n, -1, 1)) / np.pi
    fx = u * w - 0.5
    fy = np.clip(v * h - 0.5, 0, h - 1)
    x0 = np.floor(fx).astype(np.int64)
    y0 = np.floor(fy).astype(np.int64)
    tx = (fx - x0)[..., None]
    ty = (fy - y0)[..., None]
    x1 = (x0 + 1) % w
    x0 = x0 % w
    y1 = np.minimum(y0 + 1, h - 1)
    top = img[y0, x0] * (1 - tx) + img[y0, x1] * tx
    bot = img[y1, x0] * (1 - tx) + img[y1, x1] * tx
    return top * (1 - ty) + bot * ty


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)
    src, name = sys.argv[1], sys.argv[2]
    size = int(sys.argv[3]) if len(sys.argv) > 3 else 1024
    here = os.path.dirname(os.path.abspath(__file__))
    out = os.path.join(here, "..", "src", "main", "resources", "assets", "skyblocks", "textures", "sky")
    os.makedirs(out, exist_ok=True)

    img = np.asarray(Image.open(src).convert("RGB"), dtype=np.float32)
    print(f"panorama {img.shape[1]}x{img.shape[0]} -> 6 faces of {size}x{size}")

    t = (np.arange(size, dtype=np.float32) + 0.5) / size * 2 - 1      # -1..1 across the face
    sc, tc = np.meshgrid(t, t)                                        # sc = columns (u), tc = rows (v)
    for face, fn in FACES.items():
        rgb = sample_equirect(img, fn(sc, tc))
        path = os.path.join(out, f"{name}_{face}.png")
        Image.fromarray(np.clip(rgb + 0.5, 0, 255).astype(np.uint8)).save(path, optimize=True)
        print("wrote", os.path.normpath(path))


if __name__ == "__main__":
    main()
