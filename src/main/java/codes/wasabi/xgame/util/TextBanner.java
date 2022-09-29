package codes.wasabi.xgame.util;

import java.util.Arrays;
import java.util.List;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import codes.wasabi.xplug.lib.matlib.MaterialLib;
import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

public class TextBanner {
    private static final List<Character> reverseChars = Arrays.asList('h', 'q', 's');

    private static MetaMaterial getMaterial(DyeColor dyeColor) {
        String col = dyeColor.name();
        if (col.equalsIgnoreCase("SILVER")) col = "LIGHT_GRAY";
        return MaterialLib.getMaterial(col + "_BANNER");
    }
    
    public static ItemStack create(char letter, DyeColor background, DyeColor foreground, int count) {
        MetaMaterial mm = getMaterial(reverseChars.contains(letter) ? foreground : background);
        ItemStack banner = DisplayItem.create(mm, String.valueOf(letter), NamedTextColor.WHITE, count, null, false);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        if (meta == null) return banner;
        char query = Character.toLowerCase(letter);
        switch (query) {
            case '0':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNLEFT));
                break;
            case '1':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_CENTER));
                meta.addPattern(new Pattern(foreground, PatternType.SQUARE_TOP_LEFT));
                meta.addPattern(new Pattern(background, PatternType.CURLY_BORDER));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                break;
            case '2':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(background, PatternType.RHOMBUS_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNLEFT));
                break;
            case '3':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(background, PatternType.CURLY_BORDER));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                break;
            case '4':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(background, PatternType.HALF_HORIZONTAL_MIRROR));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                break;
            case '5':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(background, PatternType.RHOMBUS_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNRIGHT));
                break;
            case '6':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(background, PatternType.HALF_HORIZONTAL));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                break;
            case '7':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNLEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                break;
            case '8':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                break;
            case '9':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(background, PatternType.HALF_HORIZONTAL_MIRROR));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                break;
            case 'a':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                break;
            case 'b':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(background, PatternType.CURLY_BORDER));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                break;
            case 'c':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(background, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                break;
            case 'd':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(background, PatternType.CURLY_BORDER));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                break;
            case 'e':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                break;
            case 'f':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(background, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                break;
            case 'g':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(background, PatternType.HALF_HORIZONTAL));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                break;
            case 'h':
                meta.addPattern(new Pattern(background, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(background, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                break;
            case 'i':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_CENTER));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                break;
            case 'j':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(background, PatternType.HALF_HORIZONTAL));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                break;
            case 'k':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNRIGHT));
                meta.addPattern(new Pattern(background, PatternType.HALF_HORIZONTAL));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNLEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                break;
            case 'l':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                break;
            case 'm':
                meta.addPattern(new Pattern(foreground, PatternType.TRIANGLE_TOP));
                meta.addPattern(new Pattern(background, PatternType.TRIANGLES_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                break;
            case 'n':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(background, PatternType.TRIANGLE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNRIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                break;
            case 'o':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                break;
            case 'p':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(background, PatternType.HALF_HORIZONTAL_MIRROR));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                break;
            case 'q':
                meta.addPattern(new Pattern(background, PatternType.RHOMBUS_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.SQUARE_BOTTOM_RIGHT));
                break;
            case 'r':
                meta.addPattern(new Pattern(foreground, PatternType.HALF_HORIZONTAL));
                meta.addPattern(new Pattern(background, PatternType.STRIPE_CENTER));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNRIGHT));
                break;
            case 's':
                meta.addPattern(new Pattern(background, PatternType.RHOMBUS_MIDDLE));
                meta.addPattern(new Pattern(background, PatternType.STRIPE_MIDDLE));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNRIGHT));
                break;
            case 't':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_CENTER));
                break;
            case 'u':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                break;
            case 'v':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNLEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(background, PatternType.TRIANGLE_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNLEFT));
                break;
            case 'w':
                meta.addPattern(new Pattern(foreground, PatternType.TRIANGLE_BOTTOM));
                meta.addPattern(new Pattern(background, PatternType.TRIANGLES_BOTTOM));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_LEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_RIGHT));
                break;
            case 'x':
                meta.addPattern(new Pattern(foreground, PatternType.CROSS));
                break;
            case 'y':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNRIGHT));
                meta.addPattern(new Pattern(background, PatternType.HALF_HORIZONTAL_MIRROR));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNLEFT));
                break;
            case 'z':
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_TOP));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_DOWNLEFT));
                meta.addPattern(new Pattern(foreground, PatternType.STRIPE_BOTTOM));
        }
        meta.addPattern(new Pattern(background, PatternType.BORDER));
        banner.setItemMeta(meta);
        return banner;
    }

}
