package codes.wasabi.xgame.util;

import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;

public final class SkullUtil {

    public static ItemStack applyPlayer(ItemStack skull, OfflinePlayer player) {
        ItemMeta meta = skull.getItemMeta();
        if (meta != null) {
            if (meta instanceof SkullMeta) {
                SkullMeta sm = (SkullMeta) meta;
                if (PaperLib.isVersion(13)) {
                    sm.setOwningPlayer(player);
                } else {
                    Class<? extends SkullMeta> clazz = SkullMeta.class;
                    try {
                        Method m = clazz.getMethod("setOwner", String.class);
                        m.invoke(sm, player.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                skull.setItemMeta(sm);
            }
        }
        return skull;
    }

}
