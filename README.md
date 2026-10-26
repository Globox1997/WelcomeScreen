# WelcomeScreen
WelcomeScreen is a simple mod which adds a welcome screen on the players first join.

### Installation
WelcomeScreen is a mod built for the [Fabric Loader](https://fabricmc.net/). It requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) to be installed separately; all other dependencies are installed with the mod.

### License
WelcomeScreen is licensed under MIT.

### Commands
- `/welcomescreen edit` opens the editor with the current welcome screen.
- `/welcomescreen show [targets]` shows the welcome screen to yourself or to the given players.
- `/welcomescreen reset` removes the screen saved by the editor, so the datapack screen is used again.

### Datapacks
If you don't know how to create a datapack check out [Data Pack Wiki](https://minecraft.wiki/w/Data_pack).
The file path has to be `data/<namespace>/welcomescreen/<name>.json`. If several files exist, the last one by name is used.

A full example:
```json
{
    "background": {
        "texture": "minecraft:textures/block/dirt.png"
    },
    "elements": [
        {
            "type": "text",
            "anchor": "top_center",
            "pivot": "top_center",
            "y": 10,
            "lines": [ { "text": "This is the Title", "color": "gold" } ],
            "scale": 2.0,
            "shadow": true
        },
        {
            "type": "text",
            "anchor": "center",
            "pivot": "center",
            "y": -40,
            "lines": [ "First line", "Second line" ]
        },
        {
            "type": "image",
            "anchor": "center",
            "pivot": "center",
            "x": 80,
            "texture": "minecraft:textures/misc/unknown_pack.png",
            "width": 32,
            "height": 32
        },
        {
            "type": "button",
            "anchor": "center",
            "pivot": "center",
            "y": 20,
            "label": "Wiki",
            "width": 50,
            "action": "open_url",
            "url": "https://globox1997.github.io/wiki/"
        },
        {
            "type": "button",
            "anchor": "bottom_center",
            "pivot": "bottom_center",
            "y": -10,
            "label": { "translate": "screen.welcomescreen.close" },
            "action": "close"
        }
    ],
    "commands": [
        "give @s minecraft:stick"
    ]
}
```
