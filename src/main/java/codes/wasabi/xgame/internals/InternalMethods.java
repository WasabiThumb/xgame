package codes.wasabi.xgame.internals;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

import static codes.wasabi.xgame.internals.InternalClasses.*;

public final class InternalMethods {

    public static Method CB_CRAFT_MAGIC_NUMBERS_GET_BLOCK;
    public static Method CB_CRAFT_PLAYER_GET_HANDLE;
    public static Method CB_CRAFT_WORLD_GET_HANDLE;
    public static Method NMS_BLOCK_GET_BLOCK_DATA;
    public static Method NMS_BLOCK_FROM_LEGACY_DATA;
    public static Method NMS_WORLD_GET_CHUNK_PROVIDER;
    public static Method NMS_I_CHUNK_PROVIDER_GET_CHUNK_AT;
    public static Method NMS_I_CHUNK_PROVIDER_GET_LIGHT_ENGINE;
    public static Method NMS_CHUNK_SET_TYPE;
    public static Method NMS_CHUNK_GET_TYPE;
    public static Method NMS_CHUNK_GET_POS;
    public static Method NMS_LIGHT_ENGINE_CHECK_BLOCK;
    public static Method NMS_PLAYER_CONNECTION_SEND_PACKET;
    public static Method NMS_BLOCK_POSITION_GET_X;
    public static Method NMS_BLOCK_POSITION_GET_Z;

    public static void init() throws IllegalStateException {
        CB_CRAFT_MAGIC_NUMBERS_GET_BLOCK = getMethod(CB_CRAFT_MAGIC_NUMBERS, "getBlock", NMS_BLOCK, false, Material.class);
        CB_CRAFT_PLAYER_GET_HANDLE = getMethod(CB_CRAFT_PLAYER, "getHandle", NMS_ENTITY_PLAYER, false);
        CB_CRAFT_WORLD_GET_HANDLE = getMethod(CB_CRAFT_WORLD, "getHandle", NMS_WORLD_SERVER, false);
        NMS_BLOCK_GET_BLOCK_DATA = getMethod(NMS_BLOCK, "getBlockData", NMS_I_BLOCK_DATA, false);
        NMS_BLOCK_FROM_LEGACY_DATA = getMethod(NMS_BLOCK, "fromLegacyData", NMS_I_BLOCK_DATA, true, Integer.TYPE);
        NMS_WORLD_GET_CHUNK_PROVIDER = getMethod(NMS_WORLD, "getChunkProvider", NMS_I_CHUNK_PROVIDER, false);
        NMS_I_CHUNK_PROVIDER_GET_CHUNK_AT = getMethod(NMS_I_CHUNK_PROVIDER, "getChunkAt", NMS_CHUNK, false, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
        NMS_I_CHUNK_PROVIDER_GET_LIGHT_ENGINE = getMethod(NMS_I_CHUNK_PROVIDER, "getLightEngine", NMS_LIGHT_ENGINE, false);
        NMS_CHUNK_SET_TYPE = getMethod(NMS_CHUNK, "setType", NMS_I_BLOCK_DATA, false, NMS_BLOCK_POSITION, NMS_I_BLOCK_DATA, Boolean.TYPE);
        NMS_CHUNK_GET_TYPE = getMethod(NMS_CHUNK, "getType", NMS_I_BLOCK_DATA, false, NMS_BLOCK_POSITION);
        NMS_CHUNK_GET_POS = getMethod(NMS_CHUNK, "getPos", NMS_CHUNK_COORD_INT_PAIR, true);
        NMS_LIGHT_ENGINE_CHECK_BLOCK = getMethod(NMS_LIGHT_ENGINE, "checkBlock", null, false, NMS_BLOCK_POSITION);
        NMS_PLAYER_CONNECTION_SEND_PACKET = getMethod(NMS_PLAYER_CONNECTION, "sendPacket", null, false, NMS_PACKET);
        NMS_BLOCK_POSITION_GET_X = getMethod(NMS_BASE_BLOCK_POSITION, "getX", Integer.TYPE, false);
        NMS_BLOCK_POSITION_GET_Z = getMethod(NMS_BASE_BLOCK_POSITION, "getZ", Integer.TYPE, false);
    }

    private static @Nullable Method getMethod(Class<?> clazz, String preferredName, @Nullable Class<?> retType, boolean nullable, Class<?>... argTypes) throws IllegalStateException {
        if (clazz == null) {
            if (nullable) return null;
            throw new IllegalStateException("Class parameter is null!");
        }
        boolean _void = (retType == null);
        Method ret = null;
        for (Method m : clazz.getMethods()) {
            if (_void) {
                if (!m.getReturnType().equals(Void.TYPE)) continue;
            } else {
                if (!(retType.equals(m.getReturnType()) || retType.isAssignableFrom(m.getReturnType()))) continue;
            }
            Class<?>[] params = m.getParameterTypes();
            if (params.length != argTypes.length) continue;
            boolean all = true;
            for (int i=0; i < params.length; i++) {
                if (argTypes[i].equals(params[i])) continue;
                if (!params[i].isAssignableFrom(argTypes[i])) {
                    all = false;
                    break;
                }
            }
            if (!all) continue;
            ret = m;
            if (m.getName().equalsIgnoreCase(preferredName)) return ret;
        }
        if (ret != null) return ret;
        if (nullable) return null;
        throw new IllegalStateException("Failed to find method \"" + preferredName + "\" in class " + clazz.getName());
    }

}
