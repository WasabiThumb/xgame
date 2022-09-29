package codes.wasabi.xgame.util.schem;

import codes.wasabi.xgame.util.FastEditSession;
import codes.wasabi.xgame.util.MetaSchematic;
import codes.wasabi.xgame.util.StreamUtil;
import codes.wasabi.xplug.lib.matlib.MaterialLib;
import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseMetaSchematic<T> implements MetaSchematic {

    private final Map<Vector, MetaMaterial> materialMap = new HashMap<>();
    private final Map<Vector, T> dataMap = new HashMap<>();
    private final ReentrantLock mapLock = new ReentrantLock();

    @Override
    public void clear() {
        materialMap.clear();
        dataMap.clear();
    }

    protected abstract @Nullable T getData(Block block);

    protected abstract void writeDataSection(ObjectOutputStream oos, @Nullable T data) throws IOException;

    protected abstract @Nullable T readDataSection(ObjectInputStream ois) throws IOException;

    protected abstract void applyData(FastEditSession session, int x, int y, int z, MetaMaterial material, @Nullable T data);

    protected boolean shouldCoallateData() {
        return false;
    }

    protected void batchDataUpdate(World world, Map<Vector, T> dataMap, int dataUpdatesPerTick) {
    }

    @Override
    public void addBlock(Location origin, Block block) {
        Vector vector = new Vector(
                block.getX() - origin.getBlockX(),
                block.getY() - origin.getBlockY(),
                block.getZ() - origin.getBlockZ()
        );
        MetaMaterial mm = MaterialLib.getMaterial(block);
        if (mm == null) mm = Objects.requireNonNull(MaterialLib.getMaterial("AIR"));
        mapLock.lock();
        try {
            materialMap.put(vector, mm);
            T data = getData(block);
            if (data != null) dataMap.put(vector, data);
        } finally {
            mapLock.unlock();
        }
    }

    @Override
    public Collection<Chunk> apply(Location origin, int dataUpdatesPerTick) {
        World world = origin.getWorld();
        if (world == null) return Collections.emptyList();
        Set<Chunk> ret = new HashSet<>();
        mapLock.lock();
        try {
            boolean coallate = shouldCoallateData();
            Map<Vector, T> map = new HashMap<>();
            FastEditSession session = new FastEditSession(world);
            for (Vector vector : materialMap.keySet()) {
                Location loc = origin.clone().add(vector);
                ret.add(loc.getChunk());
                MetaMaterial mm = materialMap.get(vector);
                T data = dataMap.get(vector);
                if (coallate && (data != null)) map.put(loc.toVector().toBlockVector(), data);
                applyData(session, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), mm, data);
            }
            session.apply();
            if (coallate) batchDataUpdate(world, map, dataUpdatesPerTick);
        } finally {
            mapLock.unlock();
        }
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public void serialize(OutputStream os) throws IOException {
        mapLock.lock();
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
                Map<String, Integer> palette = new HashMap<>();
                int i = 0;
                for (MetaMaterial mm : materialMap.values()) {
                    String name = mm.getName().toUpperCase(Locale.ROOT);
                    if (!palette.containsKey(name)) {
                        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
                        oos.writeInt(nameBytes.length);
                        oos.write(nameBytes);
                        palette.put(name, i);
                        i++;
                    }
                }
                oos.writeInt(-1);
                oos.writeInt(materialMap.size());
                String lastMaterial = "AIR";
                T lastData = null;
                for (Map.Entry<Vector, MetaMaterial> entry : materialMap.entrySet()) {
                    Vector vector = entry.getKey();
                    oos.writeInt(vector.getBlockX());
                    oos.writeInt(vector.getBlockY());
                    oos.writeInt(vector.getBlockZ());
                    String material = entry.getValue().getName().toUpperCase(Locale.ROOT);
                    T data = dataMap.get(entry.getKey());
                    if (Objects.equals(material, lastMaterial) && Objects.equals(data, lastData)) {
                        oos.writeInt(-1);
                        continue;
                    }
                    lastMaterial = material;
                    lastData = data;
                    oos.writeInt(palette.getOrDefault(material, 0));
                    writeDataSection(oos, data);
                }
            }
        } finally {
            mapLock.unlock();
        }
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        mapLock.lock();
        try {
            materialMap.clear();
            dataMap.clear();
            //
            MetaMaterial lastMetaMaterial = MaterialLib.getMaterial("AIR");
            T lastData = null;
            try (ObjectInputStream ois = new ObjectInputStream(is)) {
                Map<Integer, MetaMaterial> palette = new HashMap<>();
                int i = 0;
                int len;
                while ((len = ois.readInt()) != -1) {
                    byte[] bytes = StreamUtil.readNBytes(ois, len);
                    String name = new String(bytes, StandardCharsets.UTF_8);
                    MetaMaterial mm = MaterialLib.getMaterial(name);
                    palette.put(i, mm);
                    i++;
                }
                int count = ois.readInt();
                for (int z=0; z < count; z++) {
                    Vector vector = new Vector(
                            ois.readInt(),
                            ois.readInt(),
                            ois.readInt()
                    );
                    int paletteIndex = ois.readInt();
                    MetaMaterial mm;
                    T data;
                    if (paletteIndex < 0) {
                        mm = lastMetaMaterial;
                        data = lastData;
                    } else {
                        mm = palette.get(paletteIndex);
                        if (mm == null) mm = MaterialLib.getMaterial("AIR");
                        materialMap.put(vector, mm);
                        data = readDataSection(ois);
                        if (data != null) {
                            dataMap.put(vector, data);
                        }
                        lastMetaMaterial = mm;
                        lastData = data;
                    }
                    materialMap.put(vector, mm);
                    if (data != null) dataMap.put(vector, data);
                }
            }
        } finally {
            mapLock.unlock();
        }
    }

}
