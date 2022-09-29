package codes.wasabi.xgame.gui;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xgame.minigame.Minigame;
import codes.wasabi.xgame.minigame.MinigameStage;
import codes.wasabi.xgame.minigame.Party;
import codes.wasabi.xgame.minigame.PartyListener;
import codes.wasabi.xgame.util.DisplayItem;
import codes.wasabi.xgame.util.SkullUtil;
import codes.wasabi.xgame.util.SoundUtil;
import codes.wasabi.xplug.XPlug;
import codes.wasabi.xplug.lib.adventure.text.Component;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import codes.wasabi.xplug.lib.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PartyMenu extends InventoryMenu implements PartyListener {

    private static String getLeaderName(Party party) {
        Player leader = party.getLeader();
        if (leader == null) return "Unknown";
        return leader.getName();
    }

    private static int getInventoryHeight(Party party) {
        Minigame mg = party.getMinigame();
        int maxPlayers = mg.getMaxPlayers();
        int numRows = (int) Math.ceil(Math.max(maxPlayers, 1) / 9d);
        return numRows + 2;
    }

    private final Party party;
    private Player ply;
    public PartyMenu(Party party) {
        super(getLeaderName(party) + "'s Lobby", getInventoryHeight(party));
        this.party = party;
    }

    /*
    - - - - - - - - - *
    - - - - - - - - -
    - - - - - - - - -
    0 1 2 3 4 5 6 7 8
    -   -   -   -   -
     */
    // leave, ready, force start

    private final Map<Integer, Player> playerMap = new HashMap<>();
    private void populate() {
        clear();
        Minigame mg = party.getMinigame();
        int maxPlayers = mg.getMaxPlayers();
        int numRows = ((int) Math.ceil(Math.max(maxPlayers, 1) / 9d)) + 2;
        ItemStack padding = DisplayItem.builder("BLACK_STAINED_GLASS_PANE").name(" ").color(NamedTextColor.BLACK).build();
        for (int x=0; x < 9; x++) {
            if (x == 0 || x == 2 || x == 6 || x == 8) continue;
            setItem(x, numRows - 1, padding);
        }
        setItem(0, numRows - 1, DisplayItem.builder("NETHER_STAR").name("Info").color(NamedTextColor.DARK_AQUA)
                .addLore("Game Name: " + mg.getName())
                .addLore("Minimum Players: " + mg.getMinPlayers())
                .addLore("XGame Version: " + XGame.getInstance().getDescription().getVersion())
                .addLore("XPlug Version: " + XPlug.getInstance().getDescription().getVersion())
                .build()
        );
        setItem(2, numRows - 1, DisplayItem.builder("BARRIER").name("Leave").color(NamedTextColor.RED).build());
        if (party.isReady(ply)) {
            setItem(6, numRows - 1, DisplayItem.builder("LIME_DYE").name("Ready").color(NamedTextColor.GREEN).build());
        } else {
            setItem(6, numRows - 1, DisplayItem.builder("GRAY_DYE").name("Not Ready").color(NamedTextColor.GRAY).build());
        }
        UUID leader = party.getLeaderUUID();
        boolean isLeader = leader.equals(ply.getUniqueId());
        if (isLeader) {
            setItem(
                    4,
                    numRows - 1,
                    DisplayItem
                            .builder("LEVER")
                            .name("Force Start")
                            .color(NamedTextColor.AQUA)
                            .addLore("Force the game to start")
                            .addLore("even if not all players are ready.")
                            .addLore("This is only available")
                            .addLore("to the lobby owner (you!)")
                            .build()
            );
        }
        boolean priv = party.isPrivate();
        setItem(8, numRows - 1, DisplayItem
                .builder(priv ? "IRON_DOOR" : "OAK_DOOR")
                .name(priv ? "Private Lobby" : "Public Lobby")
                .color(priv ? NamedTextColor.RED : NamedTextColor.GREEN)
                .enchanted(!isLeader)
                .build()
        );
        Iterator<Player> iterator = party.getPlayers().iterator();
        playerMap.clear();
        for (int i=0; i < maxPlayers; i++) {
            if (iterator.hasNext()) {
                Player p = iterator.next();
                DisplayItem.Builder builder = DisplayItem.builder("PLAYER_HEAD")
                        .name(LegacyComponentSerializer.legacySection().deserialize(p.getDisplayName()));
                if (p.getUniqueId().equals(leader)) {
                    builder.addLore(Component.text("Lobby Owner").color(NamedTextColor.GOLD));
                }
                if (party.isReady(p)) {
                    builder.addLore(Component.text("Ready").color(NamedTextColor.GREEN));
                } else {
                    builder.addLore(Component.text("Not Ready").color(NamedTextColor.GRAY));
                }
                if (p.getUniqueId().equals(ply.getUniqueId())) {
                    builder.addLore(Component.text("You").color(NamedTextColor.GRAY));
                } else if (isLeader) {
                    playerMap.put(i, p);
                    builder.addLore(Component.text("Right click to kick").color(NamedTextColor.GRAY));
                }
                setItem(i, SkullUtil.applyPlayer(builder.build(), p));
            } else {
                setItem(i, DisplayItem.builder("SKELETON_SKULL").name("Empty Slot").color(NamedTextColor.GRAY).build());
            }
        }
    }

    @Override
    protected void onOpen(Player ply) {
        this.ply = ply;
        populate();
        party.addListener(this);
    }

    @Override
    protected void onClose(Player ply) {
        if (getInventory().getViewers().size() < 1) party.removeListener(this);
    }

    @Override
    protected void onTick(Player ply) {

    }

    @Override
    protected boolean onClick(Player ply, int x, int y, int slot, @Nullable ItemStack item, ClickType type) {
        int height = getInventoryHeight(party);
        if (y == (height - 1)) {
            if (x == 0) {
                SoundUtil.slimeSound(ply);
                Bukkit.getScheduler().runTaskLater(XGame.getInstance(), () -> {
                    SoundUtil.experienceSound(ply);
                    XPlug.getAdventure().player(ply).sendMessage(Component.text(";)").color(NamedTextColor.GOLD));
                }, 10L);
            } else if (x == 2) {
                party.removePlayer(ply);
                close();
            } else if (x == 6) {
                SoundUtil.clickSound(ply);
                party.setReady(ply, !party.isReady(ply));
            } else if (x == 4 && party.getLeaderUUID().equals(ply.getUniqueId())) {
                if (party.startGame()) {
                    SoundUtil.clickSound(ply);
                } else {
                    SoundUtil.pigDeathSound(ply);
                }
            } else if (x == 8 && party.getLeaderUUID().equals(ply.getUniqueId())) {
                party.setPrivate(!party.isPrivate());
                SoundUtil.clickSound(ply);
            }
        } else if (y < (height - 2)) {
            if (party.getLeaderUUID().equals(ply.getUniqueId()) && type.isRightClick()) {
                Player subject = playerMap.get(slot);
                if (subject != null) {
                    party.removePlayer(subject);
                    SoundUtil.clickSound(ply);
                }
            }
        }
        return true;
    }

    @Override
    public void addPlayer(Player ply) {
        populate();
    }

    @Override
    public void removePlayer(Player ply) {
        populate();
    }

    @Override
    public void changeLeader(Player ply) {
        populate();
    }

    @Override
    public void changeStage(MinigameStage stage) {
        if (stage.equals(MinigameStage.READY) || stage.equals(MinigameStage.ACTIVE)) {
            close();
            return;
        }
        populate();
    }

    @Override
    public void changeReadyState(Player ply, boolean ready) {
        populate();
    }

    @Override
    public void changePrivacy(boolean isPrivate) {
        populate();
    }

    @Override
    public void notEnoughPlayers() {
        populate();
    }

}
