package codes.wasabi.xgame.world;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xgame.util.IntLongConverter;
import codes.wasabi.xgame.util.SpiralCoordinateIterator;
import codes.wasabi.xgame.world.generator.VoidWorldGenerator;
import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class MinigameWorldManager implements Listener {

    private final String worldName;
    private final Set<String> noGenerateList = new HashSet<>();
    private final Map<Long, MinigameWorldRegion> regionMap = new HashMap<>();
    private boolean closed = false;

    public MinigameWorldManager(String worldName) {
        this.worldName = worldName;
        Bukkit.getPluginManager().registerEvents(this, XGame.getInstance());
    }

    public final String getWorldName() {
        return worldName;
    }

    public final World getWorld() {
        checkClosed();
        World world = Bukkit.getWorld(worldName);
        if (world != null) return world;
        noGenerateList.add(worldName);
        return Objects.requireNonNull(WorldCreator.name(worldName)
                .type(WorldType.NORMAL)
                .generator(VoidWorldGenerator.INSTANCE)
                .createWorld());
    }

    public final MinigameWorldRegion getRegion(int x, int z) {
        return getRegion(IntLongConverter.intToLong(x, z));
    }

    public final MinigameWorldRegion getRegion(long key) {
        World world = getWorld();
        MinigameWorldRegion region = regionMap.get(key);
        if (region == null) {
            int[] comp = IntLongConverter.longToInt(key);
            int x = comp[0];
            int z = comp[1];
            region = new MinigameWorldRegion(
                    world,
                    x * 31,
                    z * 31,
                    x * 31 + 30,
                    z * 31 + 30
            );
            regionMap.put(key, region);
        }
        return region;
    }

    public final @NotNull MinigameWorldRegion findNonAcquiredRegion() {
        checkClosed();
        Iterator<Long> iterator = new SpiralCoordinateIterator();
        while (true) {
            long key = iterator.next();
            MinigameWorldRegion region = getRegion(key);
            if (!region.isAcquired()) return region;
        }
    }

    public final void acquireNewRegion(Consumer<MinigameWorldRegion> callback) {
        MinigameWorldRegion region = findNonAcquiredRegion();
        region.acquire(callback);
    }

    public void close() {
        checkClosed();
        HandlerList.unregisterAll(this);
        regionMap.clear();
        noGenerateList.clear();
        closed = true;
    }

    public final boolean isClosed() {
        return closed;
    }

    @EventHandler
    public void onInit(WorldInitEvent event) {
        World world = event.getWorld();
        String name = world.getName();
        XGame.getInstance().getLogger().info("World init: " + name);
        if (noGenerateList.remove(name)) {
            XGame.getInstance().getLogger().info("Setting world to not keep in memory");
            world.setKeepSpawnInMemory(false);
            world.setTime(1200L);
            if (PaperLib.isVersion(13)) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            } else {
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setGameRuleValue("doWeatherCycle", "false");
                world.setGameRuleValue("doMobSpawning", "false");
            }
        }
    }

    private Method cancelUnloadEventMethod = null;
    private boolean getCancelUnloadEventMethod = true;
    @EventHandler
    public void onUnload(ChunkUnloadEvent event) {
        // ChunkUnloadEvent cannot be cancelled in 1.14+, instead we use setForceLoaded (1.14) or addPluginTicket (1.15) in MinigameWorldRegion#acquire()
        if (PaperLib.isVersion(14)) return;
        Chunk chunk = event.getChunk();
        for (MinigameWorldRegion region : regionMap.values()) {
            if (region.containsChunk(chunk)) {
                if (getCancelUnloadEventMethod) {
                    Class<? extends ChunkUnloadEvent> clazz = event.getClass();
                    try {
                        cancelUnloadEventMethod = clazz.getMethod("setCancelled", Boolean.TYPE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getCancelUnloadEventMethod = false;
                }
                if (cancelUnloadEventMethod != null) {
                    try {
                        cancelUnloadEventMethod.invoke(event, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
        }
    }

    private void checkClosed() {
        if (closed) throw new IllegalStateException("Cannot use this MinigameWorldManager after it has been closed!");
    }

}
