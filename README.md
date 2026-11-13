# Real Life (Fabric, Minecraft 1.21.11+)

## Build
1. Install JDK 21.
2. Open https://fabricmc.net/develop/template/ and copy the current loader_version,
   loom_version and fabric_version for your Minecraft version into gradle.properties.
3. Add the Gradle wrapper once:  `gradle wrapper`  (or copy gradlew + gradle/ from the Fabric template)
4. `./gradlew build`  -> jar in build/libs/ (use the one without -sources).
5. Drop the jar and Fabric API into your mods folder.

## What is in v0.1
- Structural stability: blocks overhanging more than 4 blocks from supported ground collapse.
  Support Beam (dark oak log, iron ingot, dark oak log, vertical) carries spans for free.
- Thirst: sneak + empty hand on water, or drink a potion/water bottle.
- Temperature: biome + night + indoor insulation + heat from lit furnaces, campfires, lava.
  Thermometer (copper, redstone, copper) shows it. Freezing and dehydration hurt.

## No installs: build the .jar online
1. Make a free GitHub account, create a new repository, and upload everything in this folder
   (including the hidden .github folder).
2. Open the repo's "Actions" tab and wait for "Build mod jar" to finish (a couple of minutes).
3. Open the finished run, download the "reallife-jar" artifact, unzip it.
4. Put the jar that does NOT end in -sources.jar into your mods folder, plus Fabric API.
