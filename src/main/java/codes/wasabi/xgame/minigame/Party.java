package codes.wasabi.xgame.minigame;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xgame.world.MinigameWorldRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Party implements Listener {

    private final Minigame mg;
    private UUID leader;
    private final Set<Player> members = new HashSet<>();
    private final Set<UUID> ready = new HashSet<>();
    private boolean tasksRunning = false;
    private final Set<PartyListener> listeners = new HashSet<>();
    private MinigameStage stage = MinigameStage.INACTIVE;
    private boolean firstValidation = true;
    private boolean isPrivate = true;
    public Party(Minigame mg, Player leader) {
        this.mg = mg;
        this.leader = leader.getUniqueId();
        addPlayer(leader);
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        if (isPrivate != this.isPrivate) {
            this.isPrivate = isPrivate;
            for (PartyListener pl : listeners) pl.changePrivacy(isPrivate);
        }
    }

    public void addPlayer(Player ply) {
        if (members.add(ply)) {
            listeners.add(new PartyListener.Member(ply));
            for (PartyListener pl : listeners) pl.addPlayer(ply);
            validate();
        }
    }

    public void removePlayer(Player ply) {
        UUID uuid = ply.getUniqueId();
        if (members.removeIf((Player p) -> p.getUniqueId().equals(uuid))) {
            PartyListener remove = null;
            for (PartyListener pl : listeners) {
                if (pl instanceof PartyListener.Member) {
                    PartyListener.Member plp = (PartyListener.Member) pl;
                    if (plp.getPlayer().getUniqueId().equals(uuid)) {
                        remove = plp;
                        continue;
                    }
                }
                pl.removePlayer(ply);
            }
            if (remove != null) listeners.remove(remove);
            if (validate()) readyCheck();
        }
    }

    private void setStage(MinigameStage stage) {
        this.stage = stage;
        for (PartyListener pl : listeners) pl.changeStage(stage);
    }

    public void setReady(Player ply, boolean ready) {
        UUID uuid = ply.getUniqueId();
        if (ready) {
            if (this.ready.add(uuid)) {
                for (PartyListener pl : listeners) pl.changeReadyState(ply, true);
                readyCheck();
            }
        } else {
            if (this.ready.remove(uuid)) {
                for (PartyListener pl : listeners) pl.changeReadyState(ply, false);
            }
        }
    }

    private void readyCheck() {
        if (members.stream().allMatch((Player p) -> this.ready.contains(p.getUniqueId()))) {
            startGame();
        }
    }

    public void setReady(Player ply) {
        setReady(ply, true);
    }

    public boolean isReady(Player ply) {
        return this.ready.contains(ply.getUniqueId());
    }

    public boolean startGame() {
        if (!stage.equals(MinigameStage.INACTIVE)) return false;
        if (members.size() < mg.getMinPlayers()) {
            for (PartyListener pl : listeners) pl.notEnoughPlayers();
            return false;
        }
        setStage(MinigameStage.REGION);
        final List<Player> finalPlayers = new ArrayList<>(members);
        MinigameInstance instance = mg.newInstance();
        XGame.getWorldManager().acquireNewRegion((MinigameWorldRegion mwr) -> {
            setStage(MinigameStage.READY);
            instance.start(mwr, finalPlayers);
            setStage(MinigameStage.ACTIVE);
        });
        return true;
    }

    public boolean addListener(PartyListener listener) {
        if (listeners.add(listener)) {
            listener.changeStage(stage);
            return true;
        }
        return false;
    }

    public boolean removeListener(PartyListener listener) {
        return listeners.remove(listener);
    }

    public @NotNull Minigame getMinigame() {
        return mg;
    }

    public @NotNull UUID getLeaderUUID() {
        return this.leader;
    }

    public Player getLeader() {
        return members.stream().filter((Player p) -> p.getUniqueId().equals(leader)).findFirst().orElse(null);
    }

    public @NotNull Set<Player> getPlayers() {
        return Collections.unmodifiableSet(members);
    }

    public boolean validate() {
        if (validate0()) {
            if (!tasksRunning) {
                Bukkit.getPluginManager().registerEvents(this, XGame.getInstance());
                tasksRunning = true;
            }
            return true;
        } else {
            if (tasksRunning) {
                HandlerList.unregisterAll(this);
                tasksRunning = false;
            }
            return false;
        }
    }

    private boolean validate0() {
        if (stage.equals(MinigameStage.ACTIVE)) return false;
        if (!firstValidation) {
            if (!tasksRunning) return false;
        }
        firstValidation = false;
        Player leader = getLeader();
        if (leader != null && leader.isOnline()) {
            return true;
        } else {
            members.remove(leader);
            for (Player ply : members) {
                if (ply.isOnline()) {
                    this.leader = ply.getUniqueId();
                    for (PartyListener pl : listeners) pl.changeLeader(ply);
                    return true;
                }
            }
            return false;
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        for (Player member : members) {
            if (member.getUniqueId().equals(uuid)) {
                validate();
                return;
            }
        }
    }

}
