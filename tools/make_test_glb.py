#!/usr/bin/env python3
"""Writes a procedural test dagger (.glb) plus a matching 16x16 palette texture.

usage: make_test_glb.py OUT.glb [--axis y|x|z] [--png OUT.png]
The blade runs along the chosen axis so you can test auto-orientation. Needs only the stdlib.
"""
import json, struct, sys, zlib

axis = 'y'
png_out = None
args = sys.argv[1:]
out = args.pop(0)
while args:
    a = args.pop(0)
    if a == '--axis': axis = args.pop(0)
    elif a == '--png': png_out = args.pop(0)

# palette cells (u,v centres) in a 16x16 texture
BLADE, GUARD, GRIP, POMMEL = (0.25, 0.25), (0.75, 0.25), (0.25, 0.75), (0.75, 0.75)

def box(cx, cy, cz, sx, sy, sz, uv):
    faces = [((1,0,0), [(1,-1,-1),(1,1,-1),(1,1,1),(1,-1,1)]), ((-1,0,0), [(-1,-1,1),(-1,1,1),(-1,1,-1),(-1,-1,-1)]),
             ((0,1,0), [(-1,1,-1),(-1,1,1),(1,1,1),(1,1,-1)]), ((0,-1,0), [(-1,-1,1),(-1,-1,-1),(1,-1,-1),(1,-1,1)]),
             ((0,0,1), [(-1,-1,1),(1,-1,1),(1,1,1),(-1,1,1)]), ((0,0,-1), [(1,-1,-1),(-1,-1,-1),(-1,1,-1),(1,1,-1)])]
    P, N, T, I = [], [], [], []
    for n, vs in faces:
        base = len(P)
        for x, y, z in vs:
            P.append((cx + x*sx/2, cy + y*sy/2, cz + z*sz/2)); N.append(n); T.append(uv)
        I += [base, base+1, base+2, base, base+2, base+3]
    return P, N, T, I

parts = [box(0, 0.12, 0, 0.05, 0.24, 0.05, GRIP), box(0, 0.26, 0, 0.22, 0.05, 0.07, GUARD),
         box(0, 0.62, 0, 0.09, 0.70, 0.025, BLADE), box(0, -0.02, 0, 0.08, 0.04, 0.08, POMMEL)]
P, N, T, I = [], [], [], []
for p, n, t, i in parts:
    o = len(P); P += p; N += n; T += t; I += [x + o for x in i]

def perm(v):  # move the "up" (Y) axis to the requested axis
    x, y, z = v
    return {'y': (x, y, z), 'x': (y, -x, z), 'z': (x, z, -y)}[axis]
P = [perm(v) for v in P]; N = [perm(v) for v in N]

def pack(fmt, items): return b''.join(struct.pack(fmt, *i) if isinstance(i, tuple) else struct.pack(fmt, i) for i in items)
pos, nrm, uvb, idx = pack('<3f', P), pack('<3f', N), pack('<2f', T), pack('<H', I)
binary = pos + nrm + uvb + idx
while len(binary) % 4: binary += b'\0'
mn = [min(v[k] for v in P) for k in range(3)]; mx = [max(v[k] for v in P) for k in range(3)]
doc = {
    "asset": {"version": "2.0", "generator": "minewind test"},
    "scene": 0, "scenes": [{"nodes": [0]}],
    # a parent node with translation + rotation to exercise node transforms
    "nodes": [{"children": [1], "translation": [5, 2, -3]}, {"mesh": 0, "scale": [2, 2, 2]}],
    "meshes": [{"primitives": [{"attributes": {"POSITION": 0, "NORMAL": 1, "TEXCOORD_0": 2}, "indices": 3}]}],
    "accessors": [
        {"bufferView": 0, "componentType": 5126, "count": len(P), "type": "VEC3", "min": mn, "max": mx},
        {"bufferView": 1, "componentType": 5126, "count": len(P), "type": "VEC3"},
        {"bufferView": 2, "componentType": 5126, "count": len(P), "type": "VEC2"},
        {"bufferView": 3, "componentType": 5123, "count": len(I), "type": "SCALAR"}],
    "bufferViews": [
        {"buffer": 0, "byteOffset": 0, "byteLength": len(pos)},
        {"buffer": 0, "byteOffset": len(pos), "byteLength": len(nrm)},
        {"buffer": 0, "byteOffset": len(pos)+len(nrm), "byteLength": len(uvb)},
        {"buffer": 0, "byteOffset": len(pos)+len(nrm)+len(uvb), "byteLength": len(idx)}],
    "buffers": [{"byteLength": len(binary)}]}
js = json.dumps(doc).encode()
while len(js) % 4: js += b' '
glb = struct.pack('<III', 0x46546C67, 2, 12 + 8 + len(js) + 8 + len(binary))
glb += struct.pack('<II', len(js), 0x4E4F534A) + js + struct.pack('<II', len(binary), 0x004E4942) + binary
open(out, 'wb').write(glb)
print(f"wrote {out}: {len(P)} vertices, {len(I)//3} triangles, blade axis {axis}")

if png_out:
    cols = {(0,0): (170,175,185,255), (1,0): (210,175,60,255), (0,1): (110,70,30,255), (1,1): (60,60,70,255)}
    rows = []
    for y in range(16):
        rows.append(b'\0' + b''.join(bytes(cols[(x // 8, y // 8)]) for x in range(16)))
    def ch(t, d): return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    open(png_out, 'wb').write(b'\x89PNG\r\n\x1a\n' + ch(b'IHDR', struct.pack('>IIBBBBB', 16, 16, 8, 6, 0, 0, 0)) + ch(b'IDAT', zlib.compress(b''.join(rows))) + ch(b'IEND', b''))
    print("wrote", png_out)
