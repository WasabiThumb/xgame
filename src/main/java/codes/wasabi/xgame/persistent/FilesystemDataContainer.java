package codes.wasabi.xgame.persistent;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xgame.util.StreamUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class FilesystemDataContainer implements DataContainer {

    public static final long TIMEOUT = 604800000L;

    private final Registry registry;
    private final UUID uuid;
    protected FilesystemDataContainer(Registry registry, UUID uuid) {
        this.registry = registry;
        this.uuid = uuid;
    }

    @Override
    public <T> @Nullable T get(@NotNull String key, @NotNull DataType<T> type) {
        Object ob = registry.get(uuid, key);
        Class<T> tp = type.getType();
        if (tp.isInstance(ob)) return tp.cast(ob);
        return null;
    }

    @Override
    public boolean has(@NotNull String key) {
        return registry.has(uuid, key);
    }

    @Override
    public <T> void set(@NotNull String key, @NotNull DataType<T> type, @Nullable T value) {
        registry.set(uuid, key, value);
    }

    @Override
    public void remove(@NotNull String key) {
        registry.remove(uuid, key);
    }

    public static class Registry {

        private final Map<UUID, Map<String, Object>> dataMap = new HashMap<>();
        private final Map<UUID, Long> timestampMap = new HashMap<>();
        private final ReentrantLock mapLock = new ReentrantLock();

        protected boolean has(UUID uuid, String key) {
            mapLock.lock();
            try {
                Map<String, Object> data = dataMap.get(uuid);
                if (data == null) return false;
                return data.containsKey(key);
            } finally {
                mapLock.unlock();
            }
        }

        protected void set(UUID uuid, String key, Object value) {
            mapLock.lock();
            try {
                Map<String, Object> data = dataMap.get(uuid);
                if (data == null) data = new HashMap<>();
                data.put(key, value);
                dataMap.put(uuid, data);
                timestampMap.put(uuid, System.currentTimeMillis());
            } finally {
                mapLock.unlock();
            }
        }

        protected Object get(UUID uuid, String key) {
            mapLock.lock();
            try {
                Map<String, Object> data = dataMap.get(uuid);
                if (data == null) return null;
                return data.get(key);
            } finally {
                mapLock.unlock();
            }
        }

        protected void remove(UUID uuid, String key) {
            mapLock.lock();
            try {
                Map<String, Object> data = dataMap.get(uuid);
                if (data == null) return;
                data.remove(key);
                if (data.size() < 1) {
                    dataMap.remove(uuid);
                    timestampMap.remove(uuid);
                } else {
                    dataMap.put(uuid, data);
                    timestampMap.put(uuid, System.currentTimeMillis());
                }
            } finally {
                mapLock.unlock();
            }
        }

        public void load() {
            mapLock.lock();
            try {
                dataMap.clear();
                XGame plugin = XGame.getInstance();
                File dataDir = plugin.getDataFolder();
                if (!dataDir.exists()) return;
                File pdcFile = new File(dataDir, "pdc.dat");
                if (!pdcFile.exists()) return;
                //
                try (FileInputStream fis = new FileInputStream(pdcFile)) {
                    try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                        int size = ois.readInt();
                        for (int i=0; i < size; i++) {
                            if (ois.readByte() == ((byte) 0)) continue;
                            long mostSig = ois.readLong();
                            long leastSig = ois.readLong();
                            UUID uuid = new UUID(mostSig, leastSig);
                            Map<String, Object> data = new HashMap<>();
                            int size1 = ois.readInt();
                            for (int z=0; z < size1; z++) {
                                int c = ois.readInt();
                                byte[] bytes = StreamUtil.readNBytes(ois, c);
                                String key = new String(bytes, StandardCharsets.UTF_8);
                                byte dataType = ois.readByte();
                                DataType<?> qual = DataType.fromCode(dataType);
                                if (qual == null) continue;
                                Object obj = qual.readData(ois);
                                data.put(key, obj);
                            }
                            dataMap.put(uuid, data);
                            timestampMap.put(uuid, ois.readLong());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                mapLock.unlock();
            }
        }

        public void save() {
            mapLock.lock();
            try {
                XGame plugin = XGame.getInstance();
                File dataDir = plugin.getDataFolder();
                if (!dataDir.exists()) {
                    if (!dataDir.mkdir()) plugin.getLogger().warning("Failed to create data folder from FilesystemDataContainer.Registry");
                }
                File pdcFile = new File(dataDir, "pdc.dat");
                if (!pdcFile.exists()) {
                    try {
                        if (!pdcFile.createNewFile()) throw new IOException();
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to create PDC file from FilesystemDataContainer.Registry");
                    }
                }
                //
                long now = System.currentTimeMillis();
                try (FileOutputStream fos = new FileOutputStream(pdcFile, false)) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                        oos.writeInt(dataMap.size());
                        for (UUID id : dataMap.keySet()) {
                            long ts = timestampMap.getOrDefault(id, now);
                            if ((now - ts) > TIMEOUT) {
                                oos.writeByte(0);
                                continue;
                            } else {
                                oos.writeByte(1);
                            }
                            oos.writeLong(id.getMostSignificantBits());
                            oos.writeLong(id.getLeastSignificantBits());
                            Map<String, Object> data = dataMap.get(id);
                            oos.writeInt(data.size());
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                String key = entry.getKey();
                                Object dt = entry.getValue();
                                DataType<?> type = DataType.fromObject(dt);
                                if (type == null) continue;
                                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                                oos.writeInt(keyBytes.length);
                                oos.write(keyBytes);
                                oos.writeByte(type.getCode());
                                type.writeDataGeneric(oos, dt);
                            }
                            oos.writeLong(ts);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                mapLock.unlock();
            }
        }

    }

}
