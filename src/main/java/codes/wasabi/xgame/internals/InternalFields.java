package codes.wasabi.xgame.internals;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static codes.wasabi.xgame.internals.InternalClasses.*;

public final class InternalFields {

    public static Field NMS_ENTITY_PLAYER_CONNECTION;

    public static void init() throws IllegalStateException {
        NMS_ENTITY_PLAYER_CONNECTION = getField(NMS_ENTITY_PLAYER, "connection", NMS_PLAYER_CONNECTION, false);
    }

    private static Field getField(Class<?> clazz, String preferredName, Class<?> type, boolean nullable) throws IllegalStateException {
        if (clazz == null) {
            if (nullable) return null;
            throw new IllegalStateException("Class parameter is null!");
        }
        Field ret = null;
        for (Field f : clazz.getFields()) {
            if (!Modifier.isPublic(f.getModifiers())) continue;
            if (!type.equals(f.getType())) {
                if (!type.isAssignableFrom(f.getType())) continue;
            }
            ret = f;
            if (f.getName().equalsIgnoreCase(preferredName)) return ret;
        }
        if (ret != null) return ret;
        if (nullable) return null;
        throw new IllegalStateException("Unable to find field \"" + preferredName + "\" in class " + clazz.getName());
    }

}
