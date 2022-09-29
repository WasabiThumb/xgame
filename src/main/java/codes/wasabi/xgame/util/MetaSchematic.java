package codes.wasabi.xgame.util;

import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.Collection;

public interface MetaSchematic {

    static MetaSchematic createEmpty() {
        if (PaperLib.isVersion(13)) {
            return new codes.wasabi.xgame.util.schem.MetaSchematic_1_13();
        } else {
            return new codes.wasabi.xgame.util.schem.MetaSchematic_1_8();
        }
    }

    static MetaSchematic read(InputStream is) throws IOException {
        MetaSchematic empty = createEmpty();
        empty.deserialize(is);
        return empty;
    }

    static MetaSchematic fromBytes(byte[] bytes) throws IllegalArgumentException {
        MetaSchematic empty = createEmpty();
        empty.deserializeBytes(bytes);
        return empty;
    }

    int DEFAULT_DATA_UPDATES_PER_TICK = 80;

    void clear();

    default void addRegion(Location origin, Vector mins, Vector maxs) {
        World world = origin.getWorld();
        if (world == null) return;
        BlockVector ov = origin.toVector().toBlockVector();
        int minX = Math.min(mins.getBlockX(), maxs.getBlockX());
        int minY = Math.min(mins.getBlockY(), maxs.getBlockY());
        int minZ = Math.min(mins.getBlockZ(), maxs.getBlockZ());
        int maxX = Math.max(mins.getBlockX(), maxs.getBlockX());
        int maxY = Math.max(mins.getBlockY(), maxs.getBlockY());
        int maxZ = Math.max(mins.getBlockZ(), maxs.getBlockZ());
        for (int dx = minX; dx < (maxX + 1); dx++) {
            for (int dy = minY; dy < (maxY + 1); dy++) {
                for (int dz = minZ; dz < (maxZ + 1); dz++) {
                    Block b = ov.clone().add(new Vector(dx, dy, dz)).toLocation(world).getBlock();
                    addBlock(origin, b);
                }
            }
        }
    }

    default void addRegion(Location origin, Block mins, Block maxs) {
        if (origin.getWorld() == null) {
            origin = origin.clone();
            origin.setWorld(mins.getWorld());
        }
        addRegion(origin, mins.getLocation().toVector(), maxs.getLocation().toVector());
    }

    void addBlock(Location origin, Block block);

    Collection<Chunk> apply(Location origin, int dataUpdatesPerTick);

    default Collection<Chunk> apply(Location origin) {
        return apply(origin, DEFAULT_DATA_UPDATES_PER_TICK);
    }

    void serialize(OutputStream os) throws IOException;

    default byte[] serializeBytes() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            serialize(bos);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return bos.toByteArray();
    }

    void deserialize(InputStream is) throws IOException;

    default void deserializeBytes(byte[] bytes) throws IllegalArgumentException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            deserialize(bis);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
