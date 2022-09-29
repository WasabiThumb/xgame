package codes.wasabi.xgame.gui;

import codes.wasabi.xgame.minigame.Minigame;
import codes.wasabi.xgame.minigame.Minigames;
import codes.wasabi.xgame.minigame.Party;
import codes.wasabi.xgame.util.DisplayItem;
import codes.wasabi.xgame.util.SkullUtil;
import codes.wasabi.xgame.util.SoundUtil;
import codes.wasabi.xgame.util.TextBanner;
import codes.wasabi.xplug.XPlug;
import codes.wasabi.xplug.lib.adventure.text.Component;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import codes.wasabi.xplug.lib.adventure.text.serializer.legacy.LegacyComponentSerializer;
import codes.wasabi.xplug.lib.matlib.MaterialLib;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class PartiesMenu extends InventoryMenu {

    private final Minigame mg;
    private final ItemStack[] banners;
    private int titleOffset = 0;

    public PartiesMenu(Minigame mg) {
        super(mg.getName(), 5);
        this.mg = mg;
        //
        String rawName = mg.getName();
        int pad = Math.max(9 - rawName.length(), 2);
        StringBuilder sb = new StringBuilder(rawName);
        for (int q=0; q < pad; q++) sb.append(" ");
        //
        char[] nameCharacters = sb.toString().toCharArray();
        ItemStack[] nameItems = new ItemStack[nameCharacters.length];
        for (int i=0; i < nameCharacters.length; i++) {
            char c = nameCharacters[i];
            if (Character.isWhitespace(c)) {
                nameItems[i] = null;
            } else {
                nameItems[i] = TextBanner.create(c, DyeColor.WHITE, DyeColor.BLACK, 1);
            }
        }
        banners = nameItems;
    }

    private int pageNum = 0;
    private final Map<Integer, Party> partyMap = new HashMap<>();
    private void populate() {
        clear();
        List<Party> parties = Minigames.getParties().stream().filter((Party p) -> Objects.equals(p.getMinigame(), mg) && p.getPlayers().size() < mg.getMaxPlayers() && (!p.isPrivate())).collect(Collectors.toCollection(ArrayList::new));
        int count = parties.size();
        int maxPage = Math.max((count - 1) / 27, 0);
        pageNum = Math.max(Math.min(pageNum, maxPage), 0);
        int startIndex = pageNum * 27;
        partyMap.clear();
        for (int i=0; i < 27 && (startIndex + i) < count; i++) {
            Party party = parties.get(startIndex + i);
            Player ply = party.getLeader();
            setItem(9 + i, SkullUtil.applyPlayer(
                    DisplayItem.create(
                            MaterialLib.getMaterial("PLAYER_HEAD"),
                            Component.empty().append(
                                    LegacyComponentSerializer.legacySection().deserialize(ply.getDisplayName()).colorIfAbsent(NamedTextColor.WHITE)
                            ).append(
                                    Component.text("'s Lobby").color(NamedTextColor.GOLD)
                            ),
                            1,
                            Collections.singletonList(
                                    Component.text(party.getPlayers().size() + "/" + mg.getMaxPlayers()).color(NamedTextColor.DARK_AQUA)
                            ),
                            false
                    ),
                    ply
            ));
            partyMap.put(i, party);
        }
        ItemStack padding = DisplayItem.builder("BLACK_STAINED_GLASS_PANE").name(" ").color(NamedTextColor.BLACK).build();
        for (int x=0; x < 9; x++) {
            if (x == 3 || x == 5) continue;
            setItem(x, 4, padding);
        }
        if (pageNum > 0) {
            setItem(0, 4, DisplayItem.builder("ARROW").name("Previous Page").build());
        }
        if (pageNum < maxPage) {
            setItem(8, 4, DisplayItem.builder("ARROW").name("Next Page").build());
        }
        setItem(3, 4, DisplayItem.builder("PUMPKIN_PIE").name("Refresh").color(NamedTextColor.GREEN).build());
        setItem(4, 4, DisplayItem.builder("BARRIER").name("Back").color(NamedTextColor.RED).build());
        setItem(5, 4, DisplayItem.builder("EMERALD").name("New Party").color(NamedTextColor.GREEN).build());
    }

    @Override
    protected void onOpen(Player ply) {
        populate();
    }

    @Override
    protected void onClose(Player ply) {

    }

    private int tickCounter = 10;
    @Override
    protected void onTick(Player ply) {
        int bannerSize = banners.length;
        if (bannerSize < 1) return;
        if (tickCounter >= 5) {
            for (int q=0; q < 9; q++) {
                int d = Math.max(q + titleOffset, 0) % bannerSize;
                setItem(q, banners[d]);
            }
            //
            tickCounter = 0;
            titleOffset++;
        } else {
            tickCounter++;
        }
    }

    @Override
    protected boolean onClick(Player ply, int x, int y, int slot, @Nullable ItemStack item, ClickType type) {
        if (0 < y && y < 4) {
            Party party = partyMap.get(slot - 9);
            if (party != null) {
                if (!party.validate()) {
                    SoundUtil.pigDeathSound(ply);
                    XPlug.getAdventure().player(ply).sendMessage(Component.text("* This lobby can no longer be joined!").color(NamedTextColor.RED));
                    populate();
                    return true;
                }
                if (party.getPlayers().size() >= mg.getMaxPlayers()) {
                    SoundUtil.pigDeathSound(ply);
                    XPlug.getAdventure().player(ply).sendMessage(Component.text("* The lobby is full!").color(NamedTextColor.RED));
                    populate();
                    return true;
                }
                SoundUtil.clickSound(ply);
                switchTo(PartyMenu.class, ply, party);
            }
        } else if (y == 4) {
            if (x == 3) {
                populate();
                SoundUtil.clickSound(ply);
            } else if (x == 4) {
                SoundUtil.clickSound(ply);
                switchTo(HomeMenu.class, ply);
            } else if (x == 5) {
                SoundUtil.clickSound(ply);
                Party party = Minigames.getOrCreateParty(ply, mg);
                switchTo(PartyMenu.class, ply, party);
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
