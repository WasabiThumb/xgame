package codes.wasabi.xgame.ext.worldedit;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface WorldEditInterface {

    @Nullable WorldEditSelection getSelection(Player player);

}
