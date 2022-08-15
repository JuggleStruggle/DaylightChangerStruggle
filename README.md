# DaylightChangerStruggle
A fully-clientsided mod which changes the daylight cycle without the need of fiddling around commands.
(Unless if your goal is to do that.)

It is also compatible with Sodium and Iris as it changes the client's world lunar cycle/sky angle and
disables the night vision effect.

## Features
* The ability to pick many different types of cycles (see Cycle Types), not just set the time
* Easings, a way to decide how a transition should animate (Only used in some cycle types)
* A buttery-smooth cycle that isn't based on ticks alone, but on the interpolation of ticks (you have 
  the option to disable this if not wanted)
* Toggle world daylight and the mod's daylight
* Disable night-vision should the server enforce it upon you (this does not have the option to brighten
  up the world like some mods; it just disables the effect alone)
* A user-interface which does not rely on libraries to make its own configuration and also features a
  viewable 2-list menu depending if the resolution in the game is supported
* The user interface also has tooltips for almost all of the widgets you interact with so that you know
  what they do and how they function

## Cycle Types
Oh, what's that? Cycle types? Cycle types are daylight cycles which you can pick and see the world
traverse depending on the selected type.

For starters, we offer the following types:

* Static - Combines `/time set <time>` and `/gamerule doDaylightCycle false` but without
           needing to do both and referred to as "set the time"
* Moving - Moves the time whilist setting a speed (use negative for reverse-daylight cycle)
* System - Adjusts the time based on your system clock, also sets Minecraft days to better represent 
           the current time
* Randomizers - A seizure-friendly daylight cycle which will question why you are playing Minecraft
* Low-to-High Height Time - Depending on your height (or the viewing entity's), you can decide the 
           minimum and maximum value and tie both of them with a set time
* And more in the future

## Getting Started
* To show a user interface for the mod, bind the key "Open Time Changer Menu" to anything you'd like to 
and when in-game press the key and the menu should be seen. 
* Or if you want to use commands instead, type either `/daylightchanger`, `/dcs`, `/tcs` or `/timechanger`.

## TODOs
* A lot for the new version: v0.1.0 (1.18 to 1.18.2 exclusive)
  * It is estimated to take around weeks or even months to get the mod done, largely dependent on the
    motivation and free time
* Soon add support for Quilt Standard Libraries after v0.1.0 releases
  * Ideally for Quilt, I would like to avoid using Fabric API and use its libraries for what it is since 
    I want it to be full Quilt without Fabric dependency, which at the moment it requires Fabric API to do
    its job
    * This does not mean that Fabric support will be dropped.
    * Also since I intend to remain on Fabric over Quilt, it would be nice to know if there's anyone 
      willing to test the Quilt version if they want to do so.
  * Will also have to figure out a way to separate the main project and the rest of the versions, so if
    you have an idea, let me know!

## Building
Make sure Gradle is using the Java version this project is using (or Minecraft version 1.18.1) then 
build it by doing `./gradlew build` in your terminal.

## License
[LGPLv3](LICENSE.txt "LGPLv3") 
