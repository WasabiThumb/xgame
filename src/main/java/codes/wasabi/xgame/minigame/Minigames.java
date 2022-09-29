package codes.wasabi.xgame.minigame;

import codes.wasabi.xgame.resource.AssetSource;
import codes.wasabi.xgame.resource.AssetType;
import codes.wasabi.xplug.lib.adventure.audience.Audience;
import codes.wasabi.xplug.lib.adventure.text.Component;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class Minigames {

    private static final Set<Minigame> set = new HashSet<>();
    private static final Set<Party> parties = new HashSet<>();

    public static Set<Minigame> getAll() {
        return Collections.unmodifiableSet(set);
    }

    public static @Nullable Minigame getByName(String name) {
        for (Minigame mg : set) {
            if (mg.getName().equals(name)) return mg;
        }
        return null;
    }

    public static void add(Minigame mg) {
        set.add(mg);
    }

    public static void addLua(String fileName) {
        String path = "lua/" + fileName;
        add(new LuaMinigame(path));
    }

    public static boolean remove(Minigame mg) {
        if (set.remove(mg)) {
            parties.remove(mg);
            return true;
        }
        return false;
    }

    public static Set<Party> getParties() {
        parties.removeIf((Party p) -> !p.validate());
        return Collections.unmodifiableSet(parties);
    }

    public static Set<Party> getParties(Minigame mg) {
        parties.removeIf((Party p) -> !p.validate());
        return parties.stream().filter((Party p) -> p.getMinigame().equals(mg)).collect(Collectors.toSet());
    }

    public static @Nullable Party getParty(Player owner) {
        UUID uuid = owner.getUniqueId();
        parties.removeIf((Party p) -> !p.validate());
        for (Party p : parties) {
            if (p.getPlayers().stream().anyMatch((Player ply) -> ply.getUniqueId().equals(uuid))) {
                return p;
            }
        }
        return null;
    }

    public static @NotNull Party getOrCreateParty(Player owner, Minigame mg) {
        UUID uuid = owner.getUniqueId();
        parties.removeIf((Party p) -> !p.validate());
        for (Party p : parties) {
            if (p.getPlayers().stream().anyMatch((Player ply) -> ply.getUniqueId().equals(uuid))) {
                return p;
            }
        }
        Party party = new Party(mg, owner);
        parties.add(party);
        return party;
    }

    public static void reloadLua(@Nullable Audience progressListener) {
        reloadLua0(progressListener);
    }

    public static void reloadLuaAsync(@Nullable Audience progressListener, @Nullable Runnable callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            reloadLua0(progressListener);
            if (callback != null) callback.run();
        });
    }

    private static void reloadLua0(@Nullable Audience progressListener) {
        boolean listening = (progressListener != null);
        Set<String> projectFiles = new HashSet<>();
        for (Minigame mg : set) {
            if (mg instanceof LuaMinigame) {
                LuaMinigame lmg = (LuaMinigame) mg;
                projectFiles.add(lmg.getProjectFile());
                if (listening) progressListener.sendMessage(Component.empty()
                        .append(Component.text("* Reloading minigame ").color(NamedTextColor.GREEN))
                        .append(Component.text(lmg.getName()).color(NamedTextColor.GOLD))
                        .append(Component.text(" (").color(NamedTextColor.GREEN))
                        .append(Component.text(lmg.getProjectFile()).color(NamedTextColor.GOLD))
                        .append(Component.text(")").color(NamedTextColor.GREEN))
                );
                lmg.reload();
            }
        }
        boolean any = (listening && (projectFiles.size() > 0));
        for (String s : AssetSource.COPY.list("lua", AssetType.FILES, false)) {
            if (!s.endsWith(".lua")) return;
            String file = "lua/" + s;
            if (!projectFiles.contains(file)) {
                any = true;
                if (listening) progressListener.sendMessage(Component.empty()
                        .append(Component.text("* Loading minigame from new file ").color(NamedTextColor.GREEN))
                        .append(Component.text(file).color(NamedTextColor.GOLD))
                );
                addLua(s);
            }
        }
        if (listening && (!any)) {
            progressListener.sendMessage(Component.text("* No applicable minigames found").color(NamedTextColor.GRAY));
        }
    }

}
