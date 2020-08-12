# Atlas
Java implementation of numerous utility libraries and modules to speed up development processes and streamline similar uses of logic.

Note: This current project runs as a plugin, but will not in the future, as multiple projects will possibly use different versions of this project and should maven-shade it in.

# Background
Atlas is a collection of utility packages that will allow other projects to use it as a dependency in order to save developmental time to exploit common classes/collections.

The following categories exist within this project:
- Callbacks
- Claims
- Commands
- Data
- GUI
- Listeners
- Private Messaging
- Task Schedulers
- Data Structures
- Auto Updaters
- Algorithmic Utils
- World

# Requirements
Atlas utilizes Maven as build automation.

# Package Breakdown (See help)
### Commands
Commands are essential for player experience and design flow, as well as useful for debugging for developers.

TODO: Breakdown Atlas Structure

### FlickerlessScoreboard
[FlickerlessScoreboard](https://github.com/GodComplexMC/godcomplex-core/blob/master/src/main/java/org/godcomplex/core/util/scoreboard/FlickerlessScoreboard.java) can be used to generate and update non-flickering scoreboards despite the rapid updating.

[See here](https://www.spigotmc.org/threads/creating-a-flickerless-scoreboard.297532/) for an in-depth tutorial on how to implement them.
