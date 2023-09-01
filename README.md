# WelcomeScreen
WelcomeScreen is a simple mod which adds a welcome screen on the players first join.

### Installation
WelcomeScreen is a mod built for the [Fabric Loader](https://fabricmc.net/). It requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) to be installed separately; all other dependencies are installed with the mod.

### License
WelcomeScreen is licensed under MIT.

### Datapacks
WelcomeScreen requires a datapack to run.  
If you don't know how to create a datapack check out [Data Pack Wiki](https://minecraft.fandom.com/wiki/Data_Pack)
website and try to create your first one for the vanilla game.  
If you know how to create one, the folder path has to be ```data\modid\welcomescreen\YOURFILE.json```  
Welcome screen offers to set the ``title``, ``close button``, ``background``, ``commands`` (which are executed on opening the welcome screen) and multiple ``texts``, ``images`` and ``buttons``.  
``texts``, ``images`` and ``buttons`` need `_1-4` at their description.  
Images use pngs with the size 256x256 (or double it) and an uv of 0.  
Buttons can use translateable keys. `center = true` will center the object to the middle of the screen, otherwise the top left corner is used for coordinate 0,0. `object_center = true` will center the object itself to its center (like the middle of a string as an example).
The background uses 16x16 textures and fills the whole screen if size is set to 0. Otherwise it will use a 256x256 png.

A full example can be found below:
```json
{
    "title": {
        "center": true,
        "pos": [
            0,
            8
        ],
        "text": "This is the Title"
    },
    "close": {
        "center": true,
        "pos": [
            0,
            50
        ],
        "text": "screen.welcomescreen.close"
    },
    "text_1": {
        "center": true,
        "pos": [
            0,
            -40
        ],
        "text": [
            "FIrst line XXXXXXXXXXXXXXXXXXXXXX",
            "jo second line",
            "Another line lul",
            "okay"
        ]
    },
    "image_1": {
        "center": true,
        "pos": [
            60,
            0
        ],
        "size": [
            28,
            29
        ],
        "id": "textures/gui/advancements/tabs.png"
    },
    "button_1": {
        "center": true,
        "pos": [
            0,
            10
        ],
        "size": [
            50,
            20
        ],
        "text": "Wiki",
        "link": "https://globox1997.github.io/wiki/"
    },
    "commands": [
        "give @s minecraft:stick"
    ],
    "background": {
        "center": false,
        "id": "textures/gui/options_background.png",
        "size": [
            0,
            0
        ]
    }
}
```