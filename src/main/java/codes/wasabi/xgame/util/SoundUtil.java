package codes.wasabi.xgame.util;

import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

// TODO: Replace this class with version-independent mappings
public final class SoundUtil {

    public static void clickSound(Player ply) {
        if (PaperLib.isVersion(11)) {
            ply.playSound(ply.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1f, 1f);
        } else if (PaperLib.isVersion(9)) {
            ply.playSound(ply.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else {
            ply.playSound(ply.getLocation(), Sound.valueOf("CLICK"), 1f, 1f);
        }
    }

    public static void pageTurnSound(Player ply) {
        if (PaperLib.isVersion(14)) {
            ply.playSound(ply.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER, 1f, 1f);
        } else if (PaperLib.isVersion(11)) {
            ply.playSound(ply.getLocation(), Sound.BLOCK_PISTON_EXTEND, SoundCategory.MASTER, 1f, 1f);
        } else if (PaperLib.isVersion(9)) {
            ply.playSound(ply.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1f, 1f);
        } else {
            ply.playSound(ply.getLocation(), Sound.valueOf("PISTON_EXTEND"), 1f, 1f);
        }
    }

    public static void pigDeathSound(Player ply) {
        if (PaperLib.isVersion(11)) {
            ply.playSound(ply.getLocation(), Sound.ENTITY_PIG_DEATH, SoundCategory.MASTER, 1f, 1f);
        } else if (PaperLib.isVersion(9)) {
            ply.playSound(ply.getLocation(), Sound.ENTITY_PIG_DEATH, 1f, 1f);
        } else {
            ply.playSound(ply.getLocation(), Sound.valueOf("PIG_DEATH"), 1f, 1f);
        }
    }

    public static void slimeSound(Player ply) {
        if (PaperLib.isVersion(13)) {
            ply.playSound(ply.getLocation(), Sound.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.MASTER, 1f, 1f);
        } else if (PaperLib.isVersion(11)) {
            ply.playSound(ply.getLocation(), Sound.valueOf("BLOCK_SLIME_PLACE"), SoundCategory.MASTER, 1f, 1f);
        } else if (PaperLib.isVersion(9)) {
            ply.playSound(ply.getLocation(), Sound.valueOf("BLOCK_SLIME_PLACE"), 1f, 1f);
        } else {
            ply.playSound(ply.getLocation(), Sound.valueOf("SLIME_WALK"), 1f, 1f);
        }
    }

    public static void experienceSound(Player ply) {
        if (PaperLib.isVersion(11)) {
            ply.playSound(ply.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1f, 1f);
        } else if (PaperLib.isVersion(9)) {
            ply.playSound(ply.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        } else {
            ply.playSound(ply.getLocation(), Sound.valueOf("ORB_PICKUP"), 1f, 1f);
        }
    }

}
