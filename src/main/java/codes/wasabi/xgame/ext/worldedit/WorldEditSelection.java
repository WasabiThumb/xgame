package codes.wasabi.xgame.ext.worldedit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public interface WorldEditSelection {

    World getWorld();

    Location getOrigin();

    Vector getMins();

    Vector getMaxs();

}
