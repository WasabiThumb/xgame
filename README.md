# XGame
A minigame plugin for Spigot 1.8 - 1.19 created using [XPlug](https://github.com/WasabiThumb/xplug), a LUA platform

## Minigames
- Free-for-all
  - Last man standing wins
- King of the Hill
  - Stay on top of the hill to gain points
- The Building Game
  - A game of telephone with builds, original by SethBling
- Test Minigame
  - test minigame pls ignore LOL

To create your own minigames, you can reference the XPlug API wiki and the included "test minigame" for a template.
Minigames are hotloaded and the plugin does NOT need to be restarted when minigames are modified or added, simply use ``/xg reload``.
Default minigames are loaded to the plugin's data folder only when necessary, and in the future the plugin will use the version stored in the
data folder (``/plugins/XGame``) as the definitive version.

## Functionality
This plugin is designed mainly for use on small SMPs. All minigames happen in the same world in order to keep things running fast.
The first few times a minigame is played will likely be slower than subsequent games as the world needs to be created and pregenerated.
When the game ends, players are returned to where they were before the game started along with their previous inventory, health, etc.
Players can leave the minigame by teleporting to a different world (this will eventually be a dedicated command) or by relogging.

## Commands
| Name | Description | Permission |
| :-: | :-: | :-: |
| xgame | Same as /xg help | |
| xgame help | Shows basic help for XGame, currently does nothing | |
| xgame info | Also does nothing | |
| xgame play | Opens up the Minigame menu | |
| xgame invite | Invites a player to your lobby. Public lobbies can be joined in the ``/xg play`` menu | |
| xgame join | Accepts an invite | | 
| xgame schem | Actions related to XGame's proprietary ``.mschem`` format. This is necessary because the format uses [MaterialLib](https://github.com/WasabiThumb/MaterialLib) (bundled in XPlug) in order to make schematics version-independent from 1.8+. | xgame.schem |
| xgame schem save \[name] | Saves your WorldEdit clipboard to the named .mschem file | xgame.schem |
| xgame schem save \[name] true | Saves your WorldEdit clipboard to the named .mschem.base file. I don't feel like explaining the difference. | xgame.schem |
| xgame schem paste \[name] | Pastes the named .mschem file at your current location | xgame.schem |

## Notices
- All minigames are made in LUA and interpreted with XPlug. XPlug must be installed on the server for XGame to work
- Both XGame and XPlug are experimental software