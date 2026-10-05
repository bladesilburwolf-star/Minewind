# Minewind
Morrowind World And Mechanics inside Minecraft Fabric 1.21.1.

## Build

Minewind targets Minecraft 1.21.1, Fabric Loader 0.16.5, Fabric API 0.102.1+1.21.1, Yarn 1.21.1+build.3, Java 21, and Fabric Loom 1.8.12.

The project includes a Gradle bootstrap wrapper so a machine without a preinstalled Gradle command can bootstrap Gradle 9.7.1:

- Windows: `gradlew.bat build`
- Linux/macOS: `./gradlew build`
- Generate/refresh the standard wrapper after the first successful bootstrap with `./gradlew wrapper` or `gradlew.bat wrapper`.

The source uses Fabric/Yarn names, so Yarn mappings are required; do not replace them with Mojang official mappings.

## Project Context

I am building a total-conversion RPG modpack/project using Minecraft Fabric 1.21.1 as an engine/sandbox framework (similar to Garry's Mod).

Do NOT assume standard vanilla Minecraft gameplay, block-grid building, crafting recipes, or survival mechanics. Treat Minecraft strictly as the low-level client/server engine driving a GMod-style Morrowind total conversion.

Technical scope includes high-detail 3D OBJ/JSON entity models, custom viewmodels, non-voxel terrain, overwritten player movement, custom combat/physics engines, dynamic HUDs, full Morrowind RPG mechanics, dynamic spellcasting, stat-based progression, and custom dialogue UI.
