package codes.wasabi.xgame.ext.worldedit.basic;

import codes.wasabi.xgame.ext.worldedit.WorldEditInterface;
import codes.wasabi.xgame.ext.worldedit.WorldEditSelection;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BasicWorldEditInterface implements WorldEditInterface {

    private final WorldEdit worldEdit;
    private final WorldEditPlugin plugin;

    public BasicWorldEditInterface() {
        worldEdit = WorldEdit.getInstance();
        plugin = WorldEditPlugin.getInstance();
    }

    public final WorldEdit getWorldEdit() {
        return worldEdit;
    }

    public final WorldEditPlugin getPlugin() {
        return plugin;
    }

    @Override
    public @Nullable WorldEditSelection getSelection(Player player) {
        Region region;
        try {
            region = plugin.wrapPlayer(player).getSelection();
        } catch (IncompleteRegionException e) {
            return null;
        }
        return new BasicWorldEditSelection(player, region);
    }

}
