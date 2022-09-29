package codes.wasabi.xgame;


import codes.wasabi.xgame.ext.Externals;
import codes.wasabi.xgame.ext.worldedit.WorldEditInterface;
import codes.wasabi.xgame.ext.worldedit.WorldEditSelection;
import codes.wasabi.xgame.gui.HomeMenu;
import codes.wasabi.xgame.gui.InventoryMenu;
import codes.wasabi.xgame.gui.PartyMenu;
import codes.wasabi.xgame.minigame.Minigame;
import codes.wasabi.xgame.minigame.Minigames;
import codes.wasabi.xgame.minigame.Party;
import codes.wasabi.xgame.resource.AssetSource;
import codes.wasabi.xgame.resource.AssetType;
import codes.wasabi.xgame.util.MetaSchematic;
import codes.wasabi.xgame.util.SoundUtil;
import codes.wasabi.xplug.XPlug;
import codes.wasabi.xplug.lib.adventure.audience.Audience;
import codes.wasabi.xplug.lib.adventure.text.Component;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import codes.wasabi.xplug.lib.adventure.text.format.TextDecoration;
import codes.wasabi.xplug.lib.adventure.text.serializer.legacy.LegacyComponentSerializer;
import codes.wasabi.xplug.lib.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final Map<UUID, Party> invites = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Audience audience = XPlug.getAdventure().sender(sender);
        String op = (args.length < 1 ? "help" : args[0]).toLowerCase(Locale.ROOT);
        switch (op) {
            case "schem": {
                if (!(sender instanceof Player)) {
                    audience.sendMessage(Component.text("* This sub-command must be ran by a player!").color(NamedTextColor.RED));
                    return true;
                }
                Player ply = (Player) sender;
                if (args.length > 1 && args[1].equalsIgnoreCase("save")) {
                    if (Externals.hasWorldEditInterface()) {
                        WorldEditInterface we = Externals.getWorldEditInterfaceAssert();
                        WorldEditSelection selection = we.getSelection(ply);
                        if (selection == null) {
                            audience.sendMessage(Component.text("* You haven't selected anything!").color(NamedTextColor.RED));
                            return true;
                        }
                        String name = "map";
                        if (args.length > 2) {
                            name = args[2];
                        }
                        boolean base = false;
                        if (args.length > 3) {
                            base = args[3].equalsIgnoreCase("true");
                        }
                        audience.sendMessage(Component.empty()
                                .append(Component.text("* Saving selection as ").color(NamedTextColor.GREEN))
                                .append(Component.text(name + ".mschem" + (base ? ".base" : "")).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                        );
                        MetaSchematic schem = MetaSchematic.createEmpty();
                        schem.addRegion(selection.getOrigin(), selection.getMins(), selection.getMaxs());
                        String finalName = name;
                        boolean finalBase = base;
                        Executors.newSingleThreadExecutor().execute(() -> {
                            try {
                                if (finalBase) {
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    schem.serialize(bos);
                                    byte[] bytes = bos.toByteArray();
                                    AssetSource.COPY.writeBytes("schem/" + finalName + ".mschem.base", Base64.getEncoder().encode(bytes));
                                } else {
                                    AssetSource.COPY.writeSchematic("schem/" + finalName + ".mschem", schem);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                audience.sendMessage(Component.text("* Failed").color(NamedTextColor.RED));
                                return;
                            }
                            audience.sendMessage(Component.text("* Saved").color(NamedTextColor.GREEN));
                        });
                    } else {
                        audience.sendMessage(Component.text("* WorldEdit does not appear to be installed!").color(NamedTextColor.RED));
                    }
                } else if (args.length > 1 && args[1].equalsIgnoreCase("paste")) {
                    String name = "map";
                    if (args.length > 2) {
                        name = args[2];
                    }
                    audience.sendMessage(Component.empty()
                            .append(Component.text("* Loading schematic ").color(NamedTextColor.GREEN))
                            .append(Component.text(name + ".mschem").color(NamedTextColor.GOLD))
                    );
                    String finalName = name;
                    Executors.newSingleThreadExecutor().execute(() -> {
                        MetaSchematic schem = AssetSource.COPY.readSchematic("schem/" + finalName + (finalName.endsWith(".mschem") ? "" : ".mschem"));
                        if (schem == null) {
                            audience.sendMessage(Component.text("* Schematic not found or is corrupted!").color(NamedTextColor.RED));
                            return;
                        }
                        audience.sendMessage(Component.text("* Loaded schematic, pasting...").color(NamedTextColor.GREEN));
                        Bukkit.getScheduler().runTask(XGame.getInstance(), () -> {
                            schem.apply(ply.getLocation());
                            audience.sendMessage(Component.text("* Pasted!").color(NamedTextColor.GREEN));
                        });
                    });
                }
                break;
            }
            case "play": {
                if (!(sender instanceof Player)) {
                    audience.sendMessage(Component.text("* This sub-command must be ran by a player!").color(NamedTextColor.RED));
                    return true;
                }
                Player ply = (Player) sender;
                InventoryMenu.openMenu(HomeMenu.class, ply);
                break;
            }
            case "invite": {
                if (!(sender instanceof Player)) {
                    audience.sendMessage(Component.text("* This sub-command must be ran by a player!").color(NamedTextColor.RED));
                    return true;
                }
                Player ply = (Player) sender;
                Party party = Minigames.getParty(ply);
                if (party == null) {
                    audience.sendMessage(Component.text("* You aren't in a lobby!").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length > 1) {
                    String arg = args[1];
                    Player subject = Bukkit.getPlayer(arg);
                    if (subject == null) {
                        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
                        PlainTextComponentSerializer plainText = PlainTextComponentSerializer.plainText();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            Component displayName = legacy.deserialize(p.getDisplayName());
                            if (plainText.serialize(displayName).equalsIgnoreCase(arg)) {
                                subject = p;
                                break;
                            }
                        }
                    }
                    if (subject == null) {
                        audience.sendMessage(Component.text("* Couldn't find that player").color(NamedTextColor.RED));
                        return true;
                    }
                    invites.put(subject.getUniqueId(), party);
                    XPlug.getAdventure().player(subject).sendMessage(Component.empty()
                            .append(Component.text("* ").color(NamedTextColor.GREEN))
                            .append(LegacyComponentSerializer.legacySection().deserialize(ply.getName()).colorIfAbsent(NamedTextColor.DARK_AQUA))
                            .append(Component.text(" has invited you to their ").color(NamedTextColor.GREEN))
                            .append(Component.text(party.getMinigame().getName()).color(NamedTextColor.DARK_AQUA))
                            .append(Component.text(" lobby").color(NamedTextColor.GREEN))
                            .append(Component.newline())
                            .append(Component.text("Use ").color(NamedTextColor.GREEN))
                            .append(Component.text("/xgame join").color(NamedTextColor.DARK_AQUA))
                            .append(Component.text(" to join").color(NamedTextColor.GREEN))
                    );
                    SoundUtil.experienceSound(subject);
                } else {
                    audience.sendMessage(Component.text("* You need to specify a player to invite!").color(NamedTextColor.RED));
                }
                break;
            }
            case "join":
                if (!(sender instanceof Player)) {
                    audience.sendMessage(Component.text("* This sub-command must be ran by a player!").color(NamedTextColor.RED));
                    return true;
                }
                Player ply = (Player) sender;
                UUID uuid = ply.getUniqueId();
                Party party = invites.remove(uuid);
                if (party != null) {
                    if (!party.validate()) {
                        audience.sendMessage(Component.text("* This lobby is no longer accepting players").color(NamedTextColor.RED));
                    } else if (party.getPlayers().size() >= party.getMinigame().getMaxPlayers()) {
                        audience.sendMessage(Component.text("* This lobby is full!").color(NamedTextColor.RED));
                    } else {
                        party.addPlayer(ply);
                        (new PartyMenu(party)).open(ply);
                        audience.sendMessage(Component.text("* Joined party").color(NamedTextColor.GREEN));
                    }
                } else {
                    audience.sendMessage(Component.text("* You haven't been invited to a lobby!").color(NamedTextColor.RED));
                }
                break;
            case "reload":
                Minigames.reloadLuaAsync(audience, () -> audience.sendMessage(Component.text("* Reloaded!").color(NamedTextColor.GREEN)));
                break;
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return Arrays.asList("help", "info", "schem", "play", "reload", "invite", "join");
        }
        String op = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            if (op.equalsIgnoreCase("schem")) {
                return Arrays.asList("save", "paste");
            } else if (op.equalsIgnoreCase("play")) {
                return Minigames.getAll().stream().map((Minigame mg) -> mg.getName().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_")).collect(Collectors.toList());
            } else if (op.equalsIgnoreCase("invite")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
            return null;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        if (args.length == 3) {
            if (op.equalsIgnoreCase("schem")) {
                List<String> names = new ArrayList<>();
                for (String s : AssetSource.COPY.list("schem", AssetType.FILES, false)) {
                    if (s.endsWith(".mschem")) {
                        String s1 = s.substring(0, s.length() - 7);
                        if (!names.contains(s1)) {
                            names.add(s1);
                            continue;
                        }
                    }
                    names.add(s);
                }
                return names;
            }
            return null;
        }
        if (args.length == 4) {
            if (op.equalsIgnoreCase("schem") && sub.equalsIgnoreCase("sub")) {
                return Arrays.asList("false", "true");
            }
        }
        return null;
    }

}
