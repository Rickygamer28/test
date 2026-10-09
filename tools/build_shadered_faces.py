#!/usr/bin/env python3
"""
Converts our cube maps into the skybox layout Shadered uses. Run it after adding or changing a sky.

  python tools/build_shadered_faces.py        (needs numpy and Pillow)

Input:  tools/skies/order.txt                 one block name per line (line number = sky index)
        tools/skies/<name>/<face>.png         our six faces px nx py ny pz nz (OpenGL cube-map convention,
                                              +X east, +Y up, +Z south), all square and the same size
Output: src/main/resources/assets/skyblocks/textures/environment/<sky>/{front,back,left,right,top,bottom}.png
        where <sky> is the block name without "_sky_block".

Shadered (SkyBoxRenderer.renderBlockSkybox) draws six quads around the camera, each with its own texture,
and SkyboxTranslation applies a rotation and a flip to each quad's UVs. This tool reproduces exactly that
geometry for the DEFAULT SkyboxTranslation, and for every pixel of every Shadered face looks up which
world direction it covers, then copies our cube-map texel for that direction. The result looks the same
in Shadered as our cube map did in our own shader.
"""
import os
import sys
import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "skyblocks", "textures", "environment")
OUR_FACES = ["px", "nx", "py", "ny", "pz", "nz"]

# Shadered quads (far = 1): file name -> the four corners in draw order (SkyBoxRenderer.renderBlockSkybox)
QUADS = {
    "front":  [(-1, -1, -1), (1, -1, -1), (1, 1, -1), (-1, 1, -1)],    # -Z (north)
    "back":   [(1, -1, 1), (-1, -1, 1), (-1, 1, 1), (1, 1, 1)],        # +Z (south)
    "left":   [(1, -1, -1), (1, -1, 1), (1, 1, 1), (1, 1, -1)],        # +X (east)
    "right":  [(-1, -1, 1), (-1, -1, -1), (-1, 1, -1), (-1, 1, 1)],    # -X (west)
    "top":    [(-1, 1, -1), (1, 1, -1), (1, 1, 1), (-1, 1, 1)],        # +Y
    "bottom": [(-1, -1, 1), (1, -1, 1), (1, -1, -1), (-1, -1, -1)],    # -Y
}
# Default SkyboxTranslation: (rotation, flip) per face
DEFAULT = {
    "front": ("ROTATE_90_CCW", "HORIZONTAL"),
    "back": ("ROTATE_90_CCW", "HORIZONTAL"),
    "left": ("ROTATE_90_CCW", "HORIZONTAL"),
    "right": ("ROTATE_90_CCW", "HORIZONTAL"),
    "top": ("NONE", "HORIZONTAL"),
    "bottom": ("ROTATE_180", "HORIZONTAL"),
}
CORNER_UV = [(0.0, 1.0), (0.0, 0.0), (1.0, 0.0), (1.0, 1.0)]  # drawQuad's per-vertex UVs before transforms


def transform_uv(u, v, rotation, flip):
    """Same as SkyBoxRenderer.vertexUV."""
    if rotation == "ROTATE_90_CW":
        u, v = 1.0 - v, u
    elif rotation == "ROTATE_180":
        u, v = 1.0 - u, 1.0 - v
    elif rotation == "ROTATE_90_CCW":
        u, v = v, 1.0 - u
    if flip == "HORIZONTAL":
        u = 1.0 - u
    elif flip == "VERTICAL":
        v = 1.0 - v
    elif flip == "BOTH":
        u, v = 1.0 - u, 1.0 - v
    return u, v


def face_mapping(name):
    """Returns (P1, E1, E2, UV1, F1, F2): point = P1 + s*E1 + t*E2 and uv = UV1 + s*F1 + t*F2."""
    p = [np.array(c, dtype=np.float64) for c in QUADS[name]]
    rot, flip = DEFAULT[name]
    uv = [np.array(transform_uv(u, v, rot, flip)) for u, v in CORNER_UV]
    # Corners: P1 = (s,t)=(0,0), P2 = (0,1), P3 = (1,1), P4 = (1,0)
    assert np.allclose(p[0] + (p[3] - p[0]) + (p[1] - p[0]), p[2])
    assert np.allclose(uv[0] + (uv[3] - uv[0]) + (uv[1] - uv[0]), uv[2])
    return p[0], p[3] - p[0], p[1] - p[0], uv[0], uv[3] - uv[0], uv[1] - uv[0]


def cube_lookup(cube, d):
    """Our convention (same as the old shaders): returns (face index, col, row) for directions d (N,3)."""
    size = cube[0].shape[0]
    a = np.abs(d)
    out_face = np.zeros(len(d), dtype=int)
    sc = np.zeros(len(d))
    tc = np.zeros(len(d))
    ma = np.zeros(len(d))
    xm = (a[:, 0] >= a[:, 1]) & (a[:, 0] >= a[:, 2])
    ym = ~xm & (a[:, 1] >= a[:, 2])
    zm = ~xm & ~ym
    x, y, z = d[:, 0], d[:, 1], d[:, 2]
    for mask, pos, f_pos, f_neg, s_pos, t_pos, s_neg, t_neg, m in [
        (xm, x > 0, 0, 1, -z, -y, z, -y, a[:, 0]),
        (ym, y > 0, 2, 3, x, z, x, -z, a[:, 1]),
        (zm, z > 0, 4, 5, x, -y, -x, -y, a[:, 2]),
    ]:
        sel_p = mask & pos
        sel_n = mask & ~pos
        out_face[sel_p], sc[sel_p], tc[sel_p], ma[sel_p] = f_pos, s_pos[sel_p], t_pos[sel_p], m[sel_p]
        out_face[sel_n], sc[sel_n], tc[sel_n], ma[sel_n] = f_neg, s_neg[sel_n], t_neg[sel_n], m[sel_n]
    u = sc / ma * 0.5 + 0.5
    v = tc / ma * 0.5 + 0.5
    col = np.clip(np.floor(u * size).astype(int), 0, size - 1)
    row = np.clip(np.floor(v * size).astype(int), 0, size - 1)
    return out_face, col, row


def convert(cube, size):
    out = {}
    ii, jj = np.meshgrid(np.arange(size), np.arange(size))      # ii = column, jj = row
    uv = np.stack([(ii.ravel() + 0.5) / size, (jj.ravel() + 0.5) / size], axis=1)
    for name in QUADS:
        p1, e1, e2, uv1, f1, f2 = face_mapping(name)
        m = np.stack([f1, f2], axis=1)                          # uv - uv1 = m @ (s, t)
        st = np.linalg.solve(m, (uv - uv1).T).T
        points = p1 + st[:, :1] * e1 + st[:, 1:] * e2
        face, col, row = cube_lookup(cube, points)
        img = np.zeros((size * size, 3), dtype=np.uint8)
        for k in range(6):
            sel = face == k
            img[sel] = cube[k][row[sel], col[sel]]
        out[name] = img.reshape(size, size, 3)
    return out


def main():
    with open(os.path.join(HERE, "skies", "order.txt")) as f:
        names = [line.strip() for line in f if line.strip()]
    for name in names:
        cube = [np.array(Image.open(os.path.join(HERE, "skies", name, f + ".png")).convert("RGB")) for f in OUR_FACES]
        size = cube[0].shape[0]
        if any(c.shape[:2] != (size, size) for c in cube):
            sys.exit("%s: all faces must be square and the same size" % name)
        sky = name[:-len("_sky_block")] if name.endswith("_sky_block") else name
        folder = os.path.join(OUT, sky)
        os.makedirs(folder, exist_ok=True)
        for face, img in convert(cube, size).items():
            Image.fromarray(img).save(os.path.join(folder, face + ".png"), optimize=True)
        print("%-20s -> textures/environment/%s/ (%dx%d)" % (name, sky, size, size))


if __name__ == "__main__":
    main()
