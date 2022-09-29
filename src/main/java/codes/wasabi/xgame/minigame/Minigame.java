package codes.wasabi.xgame.minigame;

import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;

public interface Minigame {

    String getName();

    String getDescription();

    MetaMaterial getIcon();

    int getMinPlayers();

    int getMaxPlayers();

    MinigameInstance newInstance();

}
