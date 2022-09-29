package codes.wasabi.xgame.minigame;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xgame.persistent.DataContainer;
import codes.wasabi.xgame.persistent.DataContainers;
import codes.wasabi.xgame.persistent.DataType;
import codes.wasabi.xgame.util.FastEditSession;
import codes.wasabi.xgame.util.MetaSchematic;
import codes.wasabi.xgame.util.PlayerState;
import codes.wasabi.xgame.world.MinigameWorldRegion;
import codes.wasabi.xplug.XPlug;
import codes.wasabi.xplug.lib.adventure.audience.Audience;
import codes.wasabi.xplug.lib.adventure.platform.bukkit.BukkitAudiences;
import codes.wasabi.xplug.lib.adventure.text.Component;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import codes.wasabi.xplug.lib.adventure.text.minimessage.MiniMessage;
import codes.wasabi.xplug.lib.adventure.text.serializer.legacy.LegacyComponentSerializer;
import codes.wasabi.xplug.lib.matlib.MaterialLib;
import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;
import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

// TODO: This class is kind of bloated... Refactor maybe?
public abstract class MinigameInstance implements Listener {

    private MinigameWorldRegion region = null;
    private BukkitTask tickTask = null;
    private final Set<Player> players = new HashSet<>();
    private final ReentrantLock playerLock = new ReentrantLock();
    private final Set<Chunk> modifiedChunks = new HashSet<>();
    private boolean isRunning = false;

    private <T> boolean safetyWrap(Supplier<T> supplier, @Nullable AtomicReference<T> atomic) {
        try {
            T value = supplier.get();
            if (atomic != null) atomic.set(value);
        } catch (Exception e) {
            e.printStackTrace();
            playerLock.lock();
            try {
                Component component = Component.empty()
                        .append(Component.text("* A fatal error (").color(NamedTextColor.RED))
                        .append(Component.text(e.getClass().getSimpleName()).color(NamedTextColor.DARK_RED))
                        .append(Component.text(") has occurred. For safety, the minigame will be ended prematurely.").color(NamedTextColor.RED));
                BukkitAudiences adventure = XPlug.getAdventure();
                for (Player p : players) {
                    Audience audience = adventure.player(p);
                    audience.sendMessage(component);
                }
            } finally {
                playerLock.unlock();
            }
            stop();
            return true;
        }
        return false;
    }

    private boolean safetyWrap(Runnable runnable) {
        return safetyWrap(() -> {
            runnable.run();
            return null;
        }, null);
    }

    public final boolean isRunning() {
        return isRunning;
    }

    protected final void addModifiedChunks(Collection<Chunk> chunks) {
        modifiedChunks.addAll(chunks);
    }

    public final void start(MinigameWorldRegion region, Collection<Player> players) {
        if (isRunning) return;
        isRunning = true;
        this.region = region;
        playerLock.lock();
        try {
            this.players.clear();
        } finally {
            playerLock.unlock();
        }
        modifiedChunks.clear();
        Consumer<MinigameWorldRegion> body = ((MinigameWorldRegion mwr) -> {
            AtomicReference<MetaSchematic> mmAtomic = new AtomicReference<>();
            if (safetyWrap(this::getSchematic, mmAtomic)) return;
            MetaSchematic mm = mmAtomic.get();
            AtomicReference<Location> schemPosAtomic = new AtomicReference<>();
            if (safetyWrap(this::getSchematicOrigin, schemPosAtomic)) return;
            Location schemPos = schemPosAtomic.get();
            if (mm != null) {
                if (schemPos != null) {
                    modifiedChunks.addAll(mm.apply(schemPos));
                } else {
                    modifiedChunks.addAll(mm.apply(mwr.getCenterBlock().getLocation()));
                }
            }
            //
            AtomicReference<GameMode> gmAtomic = new AtomicReference<>();
            if (safetyWrap(this::getDefaultGameMode, gmAtomic)) return;
            GameMode gm = gmAtomic.get();
            playerLock.lock();
            try {
                for (Player ply : players) {
                    PlayerState curState = PlayerState.from(ply);
                    UUID uuid = ply.getUniqueId();
                    for (Entity e : ply.getWorld().getEntities()) {
                        if (e instanceof Tameable) {
                            Tameable t = (Tameable) e;
                            AnimalTamer owner = t.getOwner();
                            if (owner == null) continue;
                            if (!owner.getUniqueId().equals(uuid)) continue;
                            if (PaperLib.isVersion(13)) {
                                if (e instanceof Mob) {
                                    ((Mob) e).setTarget(null);
                                }
                            } else {
                                if (e instanceof Creature) {
                                    ((Creature) e).setTarget(null);
                                }
                            }
                            if (PaperLib.isVersion(12)) {
                                if (t instanceof Sittable) {
                                    ((Sittable) t).setSitting(true);
                                }
                            } else {
                                if (t instanceof Wolf) {
                                    ((Wolf) t).setSitting(true);
                                } else if (t instanceof Ocelot) {
                                    try {
                                        Method m = Ocelot.class.getMethod("setSitting", Boolean.TYPE);
                                        m.invoke(t, true);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    DataContainer pdc = DataContainers.get(ply);
                    pdc.set("serial_state", DataType.BYTE_ARRAY, curState.serialize());
                    PlayerState newState = PlayerState.identity(ply);
                    AtomicReference<Location> spawnPointAtomic = new AtomicReference<>();
                    if (safetyWrap(() -> getSpawnPoint(ply), spawnPointAtomic)) return;
                    newState.setLocation(spawnPointAtomic.get(), false);
                    newState.setGameMode(gm);
                    newState.apply(ply);
                    this.players.add(ply);
                }
            } finally {
                playerLock.unlock();
            }
            //
            XGame plugin = XGame.getInstance();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!isRunning) return;
                Bukkit.getPluginManager().registerEvents(this, XGame.getInstance());
                if (safetyWrap(this::onStart)) return;
                tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    boolean shouldStop;
                    playerLock.lock();
                    try {
                        Set<Player> newPlayers = new HashSet<>(this.players);
                        for (Player p : this.players) {
                            if (!(p.isOnline() && p.getWorld().getName().equals(region.getWorld().getName()))) {
                                newPlayers.remove(p);
                                try {
                                    safetyWrap(() -> onLeave(p));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                DataContainer pdc = DataContainers.get(p);
                                byte[] bytes = pdc.get("serial_state", DataType.BYTE_ARRAY);
                                PlayerState state = PlayerState.deserializeOrNull(bytes);
                                if (state != null) state.apply(p);
                            }
                        }
                        this.players.clear();
                        this.players.addAll(newPlayers);
                        shouldStop = this.players.size() < getMinPlayers();
                    } finally {
                        playerLock.unlock();
                    }
                    if (shouldStop) {
                        this.stop();
                    } else {
                        safetyWrap(this::onTick);
                    }
                }, 0L, 1L);
            });
        });
        // It is highly suggested that the region be acquired before calling this method
        if (!region.isAcquired()) {
            region.acquire(body);
        } else {
            body.accept(region);
        }
    }

    public final void stop() {
        if (!isRunning) return;
        isRunning = false;
        HandlerList.unregisterAll(this);
        if (tickTask != null) tickTask.cancel();
        try {
            onStop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FastEditSession session = new FastEditSession(region.getWorld());
            for (Chunk c : modifiedChunks) {
                session.clearChunk(c.getX(), c.getZ());
                for (Entity e : c.getEntities()) {
                    if (!(e instanceof Player)) e.remove();
                }
            }
            session.apply();
            region.unacquire();
            playerLock.lock();
            try {
                for (Player player : players) {
                    DataContainer pdc = DataContainers.get(player);
                    byte[] stateData = pdc.get("serial_state", DataType.BYTE_ARRAY);
                    PlayerState state = PlayerState.deserializeOrNull(stateData);
                    if (state == null) state = PlayerState.identity();
                    state.apply(player);
                }
                players.clear();
            } finally {
                playerLock.unlock();
            }
        }
    }

    public String getName() {
        return "Untitled";
    }

    public String getDescription() {
        return "";
    }

    public MetaMaterial getIcon() {
        return MaterialLib.getMaterial("STONE");
    }

    public final MinigameWorldRegion getRegion() {
        return region;
    }

    public final Set<Player> getPlayers() {
        playerLock.lock();
        try {
            return Collections.unmodifiableSet(new HashSet<>(players));
        } finally {
            playerLock.unlock();
        }
    }

    protected abstract void onStart();

    protected abstract void onTick();

    protected abstract void onStop();

    protected abstract void onLeave(Player ply);

    protected boolean onDamage(Player ply, @Nullable Entity attacker, double damage) {
        return false;
    }

    protected boolean onPreDeath(Player ply, @Nullable Entity attacker) {
        return false;
    }

    protected boolean onMove(Player ply, Location from, Location to) {
        return false;
    }

    protected boolean onChat(Player ply, String message) {
        return false;
    }

    protected boolean onPlace(Player ply, Block block) {
        return false;
    }

    protected boolean onBreak(Player ply, Block block) {
        return false;
    }

    protected boolean onInteract(Player ply, @Nullable ItemStack item, boolean leftClick, @Nullable Entity entity) {
        return false;
    }

    public abstract int getMinPlayers();

    public abstract int getMaxPlayers();

    public abstract @NotNull Location getSpawnPoint(Player player);

    public @Nullable Location getRespawnPoint(Player player) {
        return null;
    }

    public @Nullable MetaSchematic getSchematic() {
        return null;
    }

    public @Nullable Location getSchematicOrigin() {
        return null;
    }

    public abstract @NotNull GameMode getDefaultGameMode();

    public final void broadcast(Component message) {
        if (!isRunning) return;
        BukkitAudiences adventure = XPlug.getAdventure();
        playerLock.lock();
        try {
            for (Player p : players) {
                adventure.player(p).sendMessage(message);
            }
        } finally {
            playerLock.unlock();
        }
    }

    public final void broadcast(String miniMessage) {
        broadcast(MiniMessage.miniMessage().deserialize(miniMessage));
    }

    private boolean playerIsRelevant(Player ply) {
        if (!ply.getWorld().getName().equals(region.getWorld().getName())) return false;
        UUID uuid = ply.getUniqueId();
        playerLock.lock();
        try {
            if (players.stream().anyMatch((Player p) -> p.getUniqueId().equals(uuid))) return true;
        } finally {
            playerLock.unlock();
        }
        return false;
    }

    private <T extends PlayerEvent & Cancellable> void handleInteract(T event) {
        Player ply = event.getPlayer();
        ItemStack item;
        boolean leftClick;
        Entity entity;
        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent pie = (PlayerInteractEvent) event;
            item = pie.getItem();
            Action action = pie.getAction();
            if (action.equals(Action.PHYSICAL)) return;
            leftClick = action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK);
            entity = null;
        } else if (event instanceof PlayerInteractEntityEvent) {
            PlayerInteractEntityEvent pie = (PlayerInteractEntityEvent) event;
            PlayerInventory inv = ply.getInventory();
            if (PaperLib.isVersion(9)) {
                EquipmentSlot es = pie.getHand();
                if (PaperLib.isVersion(16, 1)) {
                    item = inv.getItem(es);
                } else {
                    switch (es) {
                        case HAND:
                            item = inv.getItemInMainHand();
                            break;
                        case OFF_HAND:
                            item = inv.getItemInOffHand();
                            break;
                        default:
                            item = null;
                    }
                }
            } else {
                try {
                    Class<? extends PlayerInventory> clazz = inv.getClass();
                    Method m = clazz.getMethod("getItemInHand");
                    item = (ItemStack) m.invoke(inv);
                } catch (Exception e) {
                    item = null;
                    e.printStackTrace();
                }
            }
            entity = pie.getRightClicked();
            leftClick = false;
        } else {
            return;
        }
        if ((item != null) && item.getType().equals(Material.AIR)) item = null;
        if (onInteract(event.getPlayer(), item, leftClick, entity)) event.setCancelled(true);
    }

    @EventHandler
    public final void onInteract(PlayerInteractEvent event) {
        handleInteract(event);
    }

    @EventHandler
    public final void onInteract(PlayerInteractEntityEvent event) {
        handleInteract(event);
    }

    @EventHandler
    public final void onDamage(EntityDamageEvent event) {
        if (!isRunning) return;
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player ply = (Player) entity;
            if (!playerIsRelevant(ply)) return;
            double finalDamage = event.getFinalDamage();
            Entity attacker = null;
            if (event instanceof EntityDamageByEntityEvent) {
                attacker = ((EntityDamageByEntityEvent) event).getDamager();
            }
            if (attacker instanceof Projectile) {
                Projectile proj = (Projectile) attacker;
                ProjectileSource ps = proj.getShooter();
                if (ps != null) {
                    if (ps instanceof Entity) attacker = (Entity) ps;
                }
            }
            Entity finalAttacker = attacker;
            AtomicBoolean cancelled = new AtomicBoolean(false);
            if (safetyWrap(() -> cancelled.set(onDamage(ply, finalAttacker, finalDamage)))) return;
            if (cancelled.get()) {
                event.setCancelled(true);
                return;
            }
            if ((ply.getHealth() - finalDamage) <= 0d) {
                if (safetyWrap(() -> cancelled.set(this.onPreDeath(ply, finalAttacker)))) return;
                if (!cancelled.get()) {
                    Component message = Component.empty()
                            .append(LegacyComponentSerializer.legacySection().deserialize(ply.getDisplayName()).colorIfAbsent(NamedTextColor.RED));
                    String attribution = " at the hands of ";
                    switch (event.getCause().name()) {
                        case "FALL":
                            message = message.append(Component.text(" hit the ground too hard").color(NamedTextColor.RED));
                            attribution = " while trying to escape ";
                            break;
                        case "FIRE":
                        case "LAVA":
                            message = message.append(Component.text(" died of a phlogiston overdose").color(NamedTextColor.RED));
                            attribution = " while trying to escape ";
                            break;
                        case "VOID":
                        case "CUSTOM":
                            message = message.append(Component.text(" was consumed by the void").color(NamedTextColor.RED));
                            attribution = " in the wake of ";
                            break;
                        case "SUICIDE":
                            message = message.append(Component.text(" took their own life").color(NamedTextColor.RED));
                            attribution = " in order to escape ";
                            break;
                        case "SUFFOCATION":
                        case "CRAMMING":
                            message = message.append(Component.text(" died of claustrophobia").color(NamedTextColor.RED));
                            attribution = " brought on by ";
                            break;
                        case "DROWNING":
                            message = message.append(Component.text(" forgot to breathe").color(NamedTextColor.RED));
                            attribution = " while swimming away from ";
                            break;
                        case "STARVATION":
                            message = message.append(Component.text(" forgot to eat").color(NamedTextColor.RED));
                            attribution = " due to stress from ";
                            break;
                        case "ENTITY_EXPLOSION":
                        case "BLOCK_EXPLOSION":
                            message = message.append(Component.text(" blew up").color(NamedTextColor.RED));
                            attribution = " during their battle with ";
                            break;
                        case "PROJECTILE":
                            message = message.append(Component.text(" was filled with arrows").color(NamedTextColor.RED));
                            attribution = " by ";
                            break;
                        default:
                            message = message.append(Component.text(" died").color(NamedTextColor.RED));
                    }
                    if (attacker != null) {
                        if (attacker instanceof Player) {
                            Component pName = LegacyComponentSerializer.legacySection().deserialize(((Player) attacker).getDisplayName());
                            message = message.append(Component.text(attribution).color(NamedTextColor.RED)).append(pName.colorIfAbsent(NamedTextColor.RED));
                        }
                    }
                    PlayerState ps = PlayerState.identity(ply);
                    ps.setGameMode(GameMode.SPECTATOR);
                    Location spawn = getRespawnPoint(ply);
                    if (spawn == null) spawn = getSpawnPoint(ply);
                    ps.setLocation(spawn, false);
                    ps.apply(ply);
                    BukkitAudiences adventure = XPlug.getAdventure();
                    playerLock.lock();
                    try {
                        for (Player p : players) adventure.player(p).sendMessage(message);
                    } finally {
                        playerLock.unlock();
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public final void onMove(PlayerMoveEvent event) {
        if (event instanceof PlayerTeleportEvent) return;
        Player ply = event.getPlayer();
        if (playerIsRelevant(ply)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to == null) to = ply.getLocation();
            AtomicReference<Boolean> atomic = new AtomicReference<>(false);
            Location finalTo = to;
            if (safetyWrap(() -> onMove(ply, from, finalTo), atomic)) return;
            if (atomic.get()) event.setCancelled(true);
        }
    }

    @EventHandler
    public final void onChat(AsyncPlayerChatEvent event) {
        Player ply = event.getPlayer();
        if (playerIsRelevant(ply)) {
            AtomicReference<Boolean> atomic = new AtomicReference<>(false);
            if (safetyWrap(() -> onChat(ply, event.getMessage()), atomic)) return;
            if (atomic.get()) event.setCancelled(true);
        }
    }

    @EventHandler
    public final void onPlace(BlockPlaceEvent event) {
        Player ply = event.getPlayer();
        if (playerIsRelevant(ply)) {
            AtomicReference<Boolean> atomic = new AtomicReference<>(false);
            if (safetyWrap(() -> onPlace(ply, event.getBlock()), atomic)) return;
            if (atomic.get()) event.setCancelled(true);
        }
    }

    @EventHandler
    public final void onBreak(BlockBreakEvent event) {
        Player ply = event.getPlayer();
        if (playerIsRelevant(ply)) {
            AtomicReference<Boolean> atomic = new AtomicReference<>(false);
            if (safetyWrap(() -> onBreak(ply, event.getBlock()), atomic)) return;
            if (atomic.get()) event.setCancelled(true);
        }
    }

}
