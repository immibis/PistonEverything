# Piston Everything
_Minecraft coremod that enables vanilla pistons to push additional blocks_

## General
What this mod intends to do:
* Let you push blocks with tile-entities using pistons
* Let you specify which of those blocks you can push

What this mod does *not* intend to do:
* Let you push blocks like obsidian or bedrock with pistons
* Function in a manner inconsistent with existing vanilla behaivor (pushing more than 12 blocks, etc)

## Compiling
1. Install the Minecraft Forge version specified in the build.gradle file into a folder.
2. Clone or unzip this repository into a folder.
3. Set up ForgeGradle as usual.
4. Run `gradle build` for this source.
5. A release jar should be generated in build/libs/

If you do not have Gradle installed system-wide and instead use the Gradle wrapper installed with Forge, you should copy those files over and instead run `gradlew build`.

## Installing the finished mod
1. Place jar file into mods/
2. That's it, really
