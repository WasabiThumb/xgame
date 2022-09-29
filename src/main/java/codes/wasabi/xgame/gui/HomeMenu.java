package codes.wasabi.xgame.gui;

import codes.wasabi.xgame.minigame.Minigame;
import codes.wasabi.xgame.minigame.Minigames;
import codes.wasabi.xgame.minigame.Party;
import codes.wasabi.xgame.util.DisplayItem;
import codes.wasabi.xgame.util.SoundUtil;
import codes.wasabi.xgame.util.TextBanner;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeMenu extends InventoryMenu {

    public HomeMenu() {
        super("XGame", 5);
    }

    private int pageNum = 0;
    private Map<Integer, Minigame> mgMap = new HashMap<>();
    private void populate() {
        clear();
        char[] characters = "xgame".toCharArray();
        ItemStack padding = DisplayItem.builder("BLACK_STAINED_GLASS_PANE").name(" ").color(NamedTextColor.BLACK).build();
        for (int i=0; i < 9; i++) {
            setItem(i, 4, padding);
            if (1 < i && i < 7) continue;
            setItem(i, padding);
        }
        for (int i=0; i < characters.length; i++) {
            setItem(2 + i, TextBanner.create(characters[i], DyeColor.WHITE, DyeColor.BLACK, 1));
        }
        setItem(4, 4, DisplayItem.builder("PUMPKIN_PIE")
                .name("Refresh")
                .color(NamedTextColor.GREEN)
                .addLore("Refresh the list of minigames")
                .addLore("and other data")
                .build()
        );
        List<Minigame> mgs = new ArrayList<>(Minigames.getAll());
        int count = mgs.size();
        int maxPage = Math.max((count - 1) / 27, 0);
        pageNum = Math.max(Math.min(pageNum, maxPage), 0);
        int startIndex = pageNum * 27;
        int ct = Math.min(count - startIndex, 27);
        mgMap.clear();
        for (int i=0; i < ct; i++) {
            Minigame mg = mgs.get(startIndex + i);
            setItem(9 + i, DisplayItem.builder(mg.getIcon())
                    .name(mg.getName())
                    .color(NamedTextColor.GOLD)
                    .addLore(mg.getDescription())
                    .build()
            );
            mgMap.put(i, mg);
        }
        if (pageNum > 0) {
            setItem(0, 4, DisplayItem.builder("ARROW")
                    .name("Previous Page")
                    .color(NamedTextColor.GOLD)
                    .build()
            );
        } else if (pageNum < maxPage) {
            setItem(8, 4, DisplayItem.builder("ARROW")
                    .name("Next Page")
                    .color(NamedTextColor.GOLD)
                    .build()
            );
        }
    }

    @Override
    protected void onOpen(Player ply) {
        Party party = Minigames.getParty(ply);
        if (party != null) {
            switchTo(PartyMenu.class, ply, party);
        }
        populate();
    }

    @Override
    protected void onClose(Player ply) {

    }

    @Override
    protected void onTick(Player ply) {

    }

    @Override
    protected boolean onClick(Player ply, int x, int y, int slot, @Nullable ItemStack item, ClickType type) {
        if (0 < y && y < 4) {
            Minigame mg = mgMap.get(slot - 9);
            if (mg != null) {
                SoundUtil.clickSound(ply);
                switchTo(PartiesMenu.class, ply, mg);
            }
        } else if (y == 4) {
            if (x == 4) {
                populate();
                SoundUtil.clickSound(ply);
            } else if (item != null && item.getType().equals(Material.ARROW)) {
                if (x == 0) {
                    pageNum--;
                } else if (x == 8) {
                    pageNum++;
                } else {
                    return true;
                }
                populate();
                SoundUtil.pageTurnSound(ply);
            }
        }
        return true;
    }

}
