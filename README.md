# DaylightChangerStruggle
A fully-clientsided mod which changes the daylight cycle without the need of fiddling around with commands.
(Unless if you have a preference in doing so!) This mod is intended to expand more on the idea in what it 
means to give the user more control and options to toy around with daylight instead of being a mod for one 
specific purpose.

It is also compatible with mods such as Sodium and Iris.


## Features
* The ability to pick different kinds of cycles (see Cycle Types), not just set the time
* Easings, a way to decide how a transition should animate (Only used in some cycle types)
* A buttery-smooth cycle that isn't based on ticks alone, but on the interpolation of ticks (you have 
  the option to disable this if not wanted)
* Toggle world daylight and the mod's daylight
* Disable night-vision should the server enforce it upon you (this does not have the option to brighten
  up the world like some mods; it just disables the effect alone)
* A user interface which does not rely on libraries to make its own configuration and also features a
  viewable 2-list menu depending if the resolution in the game is supported
* The user interface also has tooltips for almost all of the widgets you interact with so that you know
  what they do and how they function
* Option to use commands alongside the User Interface
  * Trying to edit preferences here is not currently supported yet!


## Cycle Types
Cycle Types are a way to experience daylight in ways that are unique and tailored to your preferences.

Since the initial release of the mod, the following types are available to use:

* Static - Combines `/time set <time>` and `/gamerule doDaylightCycle false` but without
           needing to do both and referred to as "set the time"
* Moving - Moves the time whilst setting a speed (use negative for reverse-daylight cycle)
* System - Adjusts the time based on your system clock, also sets Minecraft days to better represent 
           the current time
* Randomizers - A seizure-friendly daylight cycle which will question why you are playing Minecraft
* Low-to-High Height Time - Depending on your height (or the viewing entity's), you can decide the 
           minimum and maximum value and tie both of them with a set time
* And... more in the future?


## Getting Started
* To show a user interface for the mod, bind the key "Open Time Changer Menu" to anything you'd like to 
and when in-game press the key and the menu should be seen. 
* Or if you want to use commands instead, type either `/daylightchanger`, `/dcs`, `/tcs` or `/timechanger`.


## TODOs
* A major version adding most of JuggleStruggle's ideas is in the works.
  * However, JuggleStruggle cannot guarantee if it will ever come out due to the sheer amount of things 
    required to be implemented, and they're meant to give the player far more control than what the v0.0.X
    versions does.


## Building
Make sure Gradle is using the Java version this project is using (Minecraft version 1.18.1 is Java 17) then 
build it by doing `./gradlew build` in your terminal.


## License
[LGPL-3.0-only](LICENSE.txt "LGPL-3.0-only")
