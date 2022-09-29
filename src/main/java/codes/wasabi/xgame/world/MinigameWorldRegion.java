package codes.wasabi.xgame.world;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class MinigameWorldRegion {

    private final World world;
    private final int minChunkX;
    private final int minChunkZ;
    private final int maxChunkX;
    private final int maxChunkZ;
    private boolean acquired = false;

    protected MinigameWorldRegion(@NotNull World world, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
        this.world = world;
        this.minChunkX = Math.min(minChunkX, maxChunkX);
        this.minChunkZ = Math.min(minChunkZ, maxChunkZ);
        this.maxChunkX = Math.max(minChunkX, maxChunkX);
        this.maxChunkZ = Math.max(minChunkZ, maxChunkZ);
    }

    public final boolean isAcquired() {
        return acquired;
    }

    public final @NotNull World getWorld() {
        return world;
    }

    public final @NotNull Chunk getMinChunk() {
        return world.getChunkAt(minChunkX, minChunkZ);
    }

    public final @NotNull Chunk getMaxChunk() {
        return world.getChunkAt(maxChunkX, maxChunkZ);
    }

    public final @NotNull Chunk getCenterChunk() {
        int cX = (int) Math.floor(getSizeX() / 2d);
        int cZ = (int) Math.floor(getSizeZ() / 2d);
        return world.getChunkAt(cX, cZ);
    }

    public final @NotNull Block getCenterBlock() {
        return getCenterChunk().getBlock(7, 80, 7);
    }

    @Contract("null -> false")
    public final boolean containsChunk(@Nullable Chunk chunk) {
        if (chunk == null) return false;
        World wld = chunk.getWorld();
        if (!wld.getName().equals(world.getName())) return false;
        int cX = chunk.getX();
        if (cX < minChunkX || cX > maxChunkX) return false;
        int cZ = chunk.getZ();
        return cZ >= minChunkZ && cZ <= maxChunkZ;
    }

    public final @NotNull Iterator<Chunk> getChunkIterator() {
        return new Iterator<Chunk>() {
            private int curX = minChunkX;
            private int curZ = minChunkZ;

            @Override
            public boolean hasNext() {
                return (curZ <= maxChunkZ);
            }

            @Override
            public Chunk next() {
                Chunk ret = world.getChunkAt(curX, curZ);
                curX++;
                if (curX > maxChunkX) {
                    curX = minChunkX;
                    curZ++;
                }
                return ret;
            }
        };
    }

    public final int getSizeX() {
        return maxChunkX - minChunkX + 1;
    }

    public final int getSizeZ() {
        return maxChunkZ - minChunkZ + 1;
    }

    public final int getNumChunks() {
        return getSizeX() * getSizeZ();
    }

    public final void acquire(Consumer<MinigameWorldRegion> callback) {
        if (acquired) return;
        acquired = true;
        final XGame plugin = XGame.getInstance();
        final Iterator<Chunk> iterator = getChunkIterator();
        final AtomicReference<BukkitTask> atomic = new AtomicReference<>();
        atomic.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int count = 0;
            // FastEditSession session = new FastEditSession(world);
            while (iterator.hasNext() && count < 32) {
                Chunk chunk = iterator.next();
                // boolean generated = PaperLib.isChunkGenerated(chunk.getWorld(), chunk.getX(), chunk.getZ());
                // if (!chunk.isLoaded()) chunk.load();
                if (PaperLib.isVersion(15)) {
                    chunk.addPluginChunkTicket(plugin);
                } else if (PaperLib.isVersion(14)) {
                    chunk.setForceLoaded(true);
                }
                // if (generated) session.clearChunk(chunk.getX(), chunk.getZ());
                count++;
            }
            // session.apply();
            if (!iterator.hasNext()) {
                atomic.get().cancel();
                callback.accept(MinigameWorldRegion.this);
            }
        }, 0L, 1L));
    }

    public final void unacquire() {
        if (!acquired) return;
        try {
            boolean fifteen = PaperLib.isVersion(15);
            if (fifteen || PaperLib.isVersion(14)) {
                XGame plugin = XGame.getInstance();

                for (Iterator<Chunk> iterator = getChunkIterator(); iterator.hasNext(); ) {
                    Chunk chunk = iterator.next();
                    if (fifteen) {
                        chunk.removePluginChunkTicket(plugin);
                    } else {
                        chunk.setForceLoaded(false);
                    }
                }
            }
        } finally {
            acquired = false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(world.getName(), minChunkX, minChunkZ, maxChunkX, maxChunkZ);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof MinigameWorldRegion) {
            MinigameWorldRegion other = (MinigameWorldRegion) obj;
            if (other.minChunkX == this.minChunkX
                    && other.minChunkZ == this.minChunkZ
                    && other.maxChunkX == this.maxChunkX
                    && other.maxChunkZ == this.maxChunkZ
                    && other.world.getName().equals(this.world.getName())
            ){
                return true;
            }
        }
        return super.equals(obj);
    }

}
