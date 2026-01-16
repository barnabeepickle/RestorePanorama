# Make Panorama

<img src="src/main/resources/banner.png" alt="Make Panorama Logo" width=10% height=10%>

A simple Minecraft mod that adds the abilty to take high fidelity panorama screenshots to Minecraft 1.12.2

## About

I was inspired by another mod called [Panorama ScreenMake](https://modrinth.com/mod/panorama_screen), which uses Minecraft's built in panorama code.

I wanted to try to do a similar thing but for Minecraft 1.12.2 but as I discovered 1.12.2 does not have this code as far as I can tell, so I set out to implement equivalent code in 1.12.2 and this is what that is.

### Dependencies

Requires [Cleanroom](https://github.com/CleanroomMC/Cleanroom) version `0.3.31-alpha` or later. <br/>
No external libraries are required at the moment.

## Features

 - **Lightweight** : Its extremely simple and should add no overhead well playing the game normally.
 - **One Keybind** : Just press one button (`F9` by default, can be changed) and you've got yourself a panorama of your suroundings.
 - **Compatible** : Uses no mixins, relies on vanilla functionality (the same code that hides your HUD) and has been tested with [Celeritas](https://git.taumc.org/embeddedt/celeritas).
 - **Convenient** : Outputs to `.minecraft/panoramas/panorama_TIMESTAMP` already with the correct file names for a resourcepack.
 - **High Res** : Panoramas are taken at `4096` x `4096` pixels and down scaled to `1024` x `1024` pixels for a nice crisp image.

### Features That Aren't Here

At the moment there is no config file for setting the resolution or other options, this could be added in the future (especially if people download the mod) but is not yet a feature.

## Pull Requests

I will attempt to review and merge any bug fix pull requests but when it comes to new features its not guaranteed.

Additionally if you want to port this mod away from 1.12.2 you should just fork the code base and do your own thing.
