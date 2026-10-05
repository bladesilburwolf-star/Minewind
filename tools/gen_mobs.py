#!/usr/bin/env python3
"""Writes placeholder mob models (box-built .glb + 16x16 palette texture) for the Minewind creatures.

Existing files are never overwritten, so real models you drop in stay put. Run from the repo root:
    python tools/gen_mobs.py
Model faces +Z; entity mode in the mod stands it on the ground and scales its longest side to "size" blocks.
"""
import json, os, struct, zlib

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources', 'assets', 'minewind')
CELL = {'A': (0.25, 0.25), 'B': (0.75, 0.25), 'C': (0.25, 0.75), 'D': (0.75, 0.75)}

def box(cx, cy, cz, sx, sy, sz, cell):
    faces = [((1,0,0), [(1,-1,-1),(1,1,-1),(1,1,1),(1,-1,1)]), ((-1,0,0), [(-1,-1,1),(-1,1,1),(-1,1,-1),(-1,-1,-1)]),
             ((0,1,0), [(-1,1,-1),(-1,1,1),(1,1,1),(1,1,-1)]), ((0,-1,0), [(-1,-1,1),(-1,-1,-1),(1,-1,-1),(1,-1,1)]),
             ((0,0,1), [(-1,-1,1),(1,-1,1),(1,1,1),(-1,1,1)]), ((0,0,-1), [(1,-1,-1),(-1,-1,-1),(-1,1,-1),(1,1,-1)])]
    P, N, T, I = [], [], [], []
    for n, vs in faces:
        b = len(P)
        for x, y, z in vs:
            P.append((cx + x*sx/2, cy + y*sy/2, cz + z*sz/2)); N.append(n); T.append(CELL[cell])
        I += [b, b+1, b+2, b, b+2, b+3]
    return P, N, T, I

def write_glb(path, parts):
    P, N, T, I = [], [], [], []
    for p, n, t, i in parts:
        o = len(P); P += p; N += n; T += t; I += [x + o for x in i]
    pack = lambda f, items: b''.join(struct.pack(f, *i) for i in items)
    pos, nrm, uv = pack('<3f', P), pack('<3f', N), pack('<2f', T)
    idx = b''.join(struct.pack('<H', i) for i in I)
    binary = pos + nrm + uv + idx
    binary += b'\0' * (-len(binary) % 4)
    mn = [min(v[k] for v in P) for k in range(3)]; mx = [max(v[k] for v in P) for k in range(3)]
    o1, o2, o3 = len(pos), len(pos) + len(nrm), len(pos) + len(nrm) + len(uv)
    doc = {"asset": {"version": "2.0"}, "scene": 0, "scenes": [{"nodes": [0]}], "nodes": [{"mesh": 0}],
           "meshes": [{"primitives": [{"attributes": {"POSITION": 0, "NORMAL": 1, "TEXCOORD_0": 2}, "indices": 3}]}],
           "accessors": [{"bufferView": 0, "componentType": 5126, "count": len(P), "type": "VEC3", "min": mn, "max": mx},
                         {"bufferView": 1, "componentType": 5126, "count": len(P), "type": "VEC3"},
                         {"bufferView": 2, "componentType": 5126, "count": len(P), "type": "VEC2"},
                         {"bufferView": 3, "componentType": 5123, "count": len(I), "type": "SCALAR"}],
           "bufferViews": [{"buffer": 0, "byteOffset": 0, "byteLength": len(pos)}, {"buffer": 0, "byteOffset": o1, "byteLength": len(nrm)},
                           {"buffer": 0, "byteOffset": o2, "byteLength": len(uv)}, {"buffer": 0, "byteOffset": o3, "byteLength": len(idx)}],
           "buffers": [{"byteLength": len(binary)}]}
    js = json.dumps(doc).encode(); js += b' ' * (-len(js) % 4)
    glb = struct.pack('<III', 0x46546C67, 2, 12 + 8 + len(js) + 8 + len(binary))
    glb += struct.pack('<II', len(js), 0x4E4F534A) + js + struct.pack('<II', len(binary), 0x004E4942) + binary
    open(path, 'wb').write(glb)

def write_png(path, colors):  # colors: dict cell letter -> rgb
    pal = {(0, 0): colors['A'], (1, 0): colors['B'], (0, 1): colors['C'], (1, 1): colors['D']}
    rows = [b'\0' + b''.join(bytes(pal[(x // 8, y // 8)] + (255,)) for x in range(16)) for y in range(16)]
    ch = lambda t, d: struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    open(path, 'wb').write(b'\x89PNG\r\n\x1a\n' + ch(b'IHDR', struct.pack('>IIBBBBB', 16, 16, 8, 6, 0, 0, 0)) + ch(b'IDAT', zlib.compress(b''.join(rows))) + ch(b'IEND', b''))

def legs(xs, zs, y, w, h, d, cell='C'):
    return [box(x, y, z, w, h, d, cell) for x in xs for z in zs]

MOBS = {
    # name: (palette A,B,C,D, size in blocks, parts)
    'mudcrab': ((120, 85, 55), (165, 110, 70), (70, 50, 35), (235, 235, 235), 1.1,
                [box(0, .25, 0, 1.0, .3, .8, 'A'), box(0, .42, -.05, .8, .12, .6, 'B'),
                 box(-.45, .25, .55, .18, .14, .45, 'B'), box(.45, .25, .55, .18, .14, .45, 'B'),
                 box(-.1, .4, .42, .06, .1, .06, 'D'), box(.1, .4, .42, .06, .1, .06, 'D')]
                + legs([-.55, .55], [-.25, 0, .25], .08, .2, .16, .08)),
    'scrib': ((205, 190, 150), (225, 205, 160), (130, 110, 80), (30, 30, 30), 0.7,
              [box(0, .2, 0, .35, .28, .55, 'A'), box(0, .22, .35, .25, .2, .2, 'B'),
               box(-.07, .27, .46, .05, .05, .04, 'D'), box(.07, .27, .46, .05, .05, .04, 'D')]
              + legs([-.14, .14], [-.15, .15], .05, .08, .1, .08)),
    'alit': ((95, 120, 70), (130, 150, 85), (60, 70, 45), (230, 200, 60), 1.9,
             [box(0, .65, 0, .5, .5, 1.1, 'A'), box(0, .95, .6, .22, .5, .22, 'A'), box(0, 1.25, .8, .3, .25, .5, 'B'),
              box(0, .6, -.9, .2, .2, .8, 'A'), box(-.1, 1.33, 1.0, .06, .06, .06, 'D'), box(.1, 1.33, 1.0, .06, .06, .06, 'D')]
             + legs([-.22, .22], [-.4, .4], .25, .14, .5, .14)),
    'kwama_forager': ((200, 160, 70), (225, 190, 95), (110, 85, 40), (30, 30, 30), 1.1,
                      [box(0, .45, 0, .7, .6, 1.0, 'A'), box(0, .4, .65, .4, .35, .35, 'B'),
                       box(-.1, .45, .83, .06, .06, .04, 'D'), box(.1, .45, .83, .06, .06, .04, 'D')]
                      + legs([-.38, .38], [-.3, 0, .3], .12, .1, .24, .1)),
}

if __name__ == '__main__':
    os.makedirs(os.path.join(ROOT, 'models', 'glb', 'entity'), exist_ok=True)
    os.makedirs(os.path.join(ROOT, 'textures', 'entity'), exist_ok=True)
    for name, (a, b, c, d, size, parts) in MOBS.items():
        glb = os.path.join(ROOT, 'models', 'glb', 'entity', name + '.glb')
        png = os.path.join(ROOT, 'textures', 'entity', name + '.png')
        opt = os.path.join(ROOT, 'models', 'glb', 'entity', name + '.json')
        if not os.path.exists(glb): write_glb(glb, parts); print('wrote', os.path.relpath(glb, ROOT))
        if not os.path.exists(png): write_png(png, {'A': a, 'B': b, 'C': c, 'D': d}); print('wrote', os.path.relpath(png, ROOT))
        if not os.path.exists(opt): open(opt, 'w').write(json.dumps({"size": size}) + '\n')
