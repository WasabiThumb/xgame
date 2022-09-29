package codes.wasabi.xgame.persistent;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NativeDataContainer implements DataContainer {

    private final PersistentDataContainer pdc;
    protected NativeDataContainer(PersistentDataContainer pdc) {
        this.pdc = pdc;
    }

    private PersistentDataType<?,?> getNativeType(DataType<?> type) {
        return (PersistentDataType<?, ?>) type.getBukkitDataType();
    }

    private NamespacedKey getKey(String key) {
        if (PaperLib.isVersion(17)) {
            return NamespacedKey.fromString(key, XGame.getInstance());
        } else {
            return new NamespacedKey(XGame.getInstance(), key);
        }
    }

    @Override
    public <T> @Nullable T get(@NotNull String key, @NotNull DataType<T> type) {
        Object ob = pdc.get(getKey(key), getNativeType(type));
        if (ob == null) return null;
        return type.getType().cast(ob);
    }

    @Override
    public boolean has(@NotNull String key) {
        if (PaperLib.isVersion(16, 1)) {
            return pdc.getKeys().contains(getKey(key));
        } else {
            return has(key, DataType.BYTE);
        }
    }

    @Override
    public boolean has(@NotNull String key, @NotNull DataType<?> type) {
        return pdc.has(getKey(key), getNativeType(type));
    }

    @Override
    public <T> void set(@NotNull String key, @NotNull DataType<T> type, @Nullable T value) {
        if (value == null) {
            remove(key);
            return;
        }
        switch (type.getName()) {
            case "BYTE":
                pdc.set(getKey(key), PersistentDataType.BYTE, (Byte) value);
                break;
            case "SHORT":
                pdc.set(getKey(key), PersistentDataType.SHORT, (Short) value);
                break;
            case "INTEGER":
                pdc.set(getKey(key), PersistentDataType.INTEGER, (Integer) value);
                break;
            case "LONG":
                pdc.set(getKey(key), PersistentDataType.LONG, (Long) value);
                break;
            case "FLOAT":
                pdc.set(getKey(key), PersistentDataType.FLOAT, (Float) value);
                break;
            case "DOUBLE":
                pdc.set(getKey(key), PersistentDataType.DOUBLE, (Double) value);
                break;
            case "STRING":
                pdc.set(getKey(key), PersistentDataType.STRING, (String) value);
                break;
            case "BYTE_ARRAY":
                pdc.set(getKey(key), PersistentDataType.BYTE_ARRAY, (byte[]) value);
                break;
            case "INTEGER_ARRAY":
                pdc.set(getKey(key), PersistentDataType.INTEGER_ARRAY, (int[]) value);
                break;
            case "LONG_ARRAY":
                pdc.set(getKey(key), PersistentDataType.LONG_ARRAY, (long[]) value);
        }
    }

    @Override
    public void remove(@NotNull String key) {
        pdc.remove(getKey(key));
    }

}
