package codes.wasabi.xgame.persistent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DataContainer {

    <T> @Nullable T get(@NotNull String key, @NotNull DataType<T> type);

    @Contract("_, _, !null -> !null")
    default <T> @Nullable T getOrDefault(@NotNull String key, @NotNull DataType<T> type, @Nullable T defaultValue) {
        T ret = get(key, type);
        if (ret == null) ret = defaultValue;
        return ret;
    }

    boolean has(@NotNull String key);

    default boolean has(@NotNull String key, @NotNull DataType<?> type) {
        return (get(key, type) != null);
    }

    <T> void set(@NotNull String key, @NotNull DataType<T> type, @Nullable T value);

    void remove(@NotNull String key);

}
