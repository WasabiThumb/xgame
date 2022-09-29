package codes.wasabi.xgame.ext.worldedit.basic;

import codes.wasabi.xgame.ext.worldedit.WorldEditSelection;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BasicWorldEditSelection implements WorldEditSelection {

    private final Player player;
    private final Region region;
    public BasicWorldEditSelection(Player player, Region region) {
        this.player = player;
        this.region = region;
    }

    public final Player getPlayer() {
        return player;
    }

    public final Region getRegion() {
        return region;
    }

    @Override
    public World getWorld() {
        return WorldEditPlugin.getInstance().getBukkitImplAdapter().adapt(region.getWorld());
    }

    @Override
    public Location getOrigin() {
        return player.getLocation();
    }

    @Override
    public Vector getMins() {
        return adaptVector(region.getMinimumPoint()).subtract(player.getLocation().toVector());
    }

    @Override
    public Vector getMaxs() {
        return adaptVector(region.getMaximumPoint()).subtract(player.getLocation().toVector()).add(new Vector(1, 0, 1));
    }

    private Vector adaptVector(BlockVector3 vec) {
        return new Vector(
                vec.getBlockX(),
                vec.getBlockY(),
                vec.getBlockZ()
        );
    }

}
