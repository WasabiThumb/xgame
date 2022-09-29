package codes.wasabi.xgame.internals;

import java.lang.reflect.Constructor;
import java.util.BitSet;

import static codes.wasabi.xgame.internals.InternalClasses.*;

public final class InternalConstructors {

    public static Constructor<?> NMS_BLOCK_POSITION_NEW;
    public static Constructor<?> NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_8;
    public static Constructor<?> NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_9_4;
    public static Constructor<?> NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_18;
    public static Constructor<?> NMS_PACKET_PLAY_OUT_LIGHT_UPDATE_NEW_1_14;
    public static Constructor<?> NMS_PACKET_PLAY_OUT_LIGHT_UPDATE_NEW_1_17;
    public static Constructor<?> NMS_PACKET_PLAY_OUT_UNLOAD_CHUNK_NEW_1_9_4;

    public static void init() throws IllegalStateException {
        NMS_BLOCK_POSITION_NEW = getConstructor(NMS_BLOCK_POSITION, false, Integer.TYPE, Integer.TYPE, Integer.TYPE);
        NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_8 = getConstructor(NMS_PACKET_PLAY_OUT_MAP_CHUNK, true, NMS_CHUNK, Boolean.TYPE, Integer.TYPE);
        NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_9_4 = getConstructor(NMS_PACKET_PLAY_OUT_MAP_CHUNK, true, NMS_CHUNK, Integer.TYPE);
        NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_18 = getConstructor(NMS_PACKET_PLAY_OUT_MAP_CHUNK, true, NMS_CHUNK, NMS_LIGHT_ENGINE, BitSet.class, BitSet.class, Boolean.TYPE);
        NMS_PACKET_PLAY_OUT_LIGHT_UPDATE_NEW_1_14 = getConstructor(NMS_PACKET_PLAY_OUT_LIGHT_UPDATE, true, NMS_CHUNK_COORD_INT_PAIR, NMS_LIGHT_ENGINE, Boolean.TYPE);
        NMS_PACKET_PLAY_OUT_LIGHT_UPDATE_NEW_1_17 = getConstructor(NMS_PACKET_PLAY_OUT_LIGHT_UPDATE, true, NMS_CHUNK_COORD_INT_PAIR, NMS_LIGHT_ENGINE, BitSet.class, BitSet.class, Boolean.TYPE);
        NMS_PACKET_PLAY_OUT_UNLOAD_CHUNK_NEW_1_9_4 = getConstructor(NMS_PACKET_PLAY_OUT_UNLOAD_CHUNK, true, Integer.TYPE, Integer.TYPE);
    }

    private static Constructor<?> getConstructor(Class<?> clazz, boolean nullable, Class<?>... params) throws IllegalStateException {
        if (clazz == null) {
            if (nullable) return null;
            throw new IllegalStateException("Class parameter is null!");
        }
        try {
            return clazz.getConstructor(params);
        } catch (Exception e) {
            if (nullable) return null;
            throw new IllegalStateException(e);
        }
    }

}
