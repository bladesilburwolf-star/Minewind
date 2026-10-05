# Minewind
Morrowind content for Minecraft Fabric 1.21.1 (Java 21, Gradle 8.10.2, Loom 1.8).

Build: `gradlew.bat build` (Windows) or `./gradlew build`. Dev client: `gradlew runClient`.

## Weapons
Iron Dagger, Steel Longsword, Steel Claymore, Ebony Broadsword (Combat tab).
Textures in `src/main/resources/assets/minewind/textures/item/` are placeholders; replace with PNGs from your own Morrowind install (Icons/w/*.dds converted to PNG).

## GLB models
Put `NAME.glb` in `src/main/resources/assets/minewind/models/glb/` (NAME = item id, e.g. `iron_dagger`) and a texture
`NAME.png` in `textures/item/`. The glb's UVs are mapped onto that PNG. Items without a .glb keep the flat icon.
`iron_dagger` ships with a procedural test dagger (`tools/make_test_glb.py`).

Optional `NAME.json` next to the glb: `{"rotate":[0,0,0],"scale":1.0,"auto_orient":true,"flip":false,"diagonal":true,"offset":[0,0,0]}`.
`auto_orient` turns the longest axis up; set `flip` if the blade points at the hilt; `diagonal` tilts it 45 degrees like vanilla swords.

## More weapons
`python tools/gen_weapons.py` regenerates `MorrowindWeapons.java` from the table at the top of the script (add a `(tier, kind)` row to add a weapon).
It only adds missing icons/models/lang/tag entries, so your own art is never overwritten. Drop `models/glb/<id>.glb` to give any weapon a 3D model.

## Mobs
Mudcrab, Scrib, Alit and Kwama Forager (spawn eggs in the Spawn Eggs tab). Model: `models/glb/entity/<name>.glb`, texture: `textures/entity/<name>.png`,
optional `models/glb/entity/<name>.json` with `{"size": 1.1, "rotate": [0,180,0], "flip": false, "scale": 1.0, "offset": [0,0,0]}`
(`size` = length in blocks of the longest side; the model should face +Z). `python tools/gen_mobs.py` writes placeholder box models if files are missing.
Mobs are static meshes for now (no walk animation) and have no natural spawning yet.
