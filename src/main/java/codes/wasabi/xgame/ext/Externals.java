package codes.wasabi.xgame.ext;

import codes.wasabi.xgame.ext.worldedit.WorldEditInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Externals {

    private static boolean HAS_WORLDEDIT = false;
    private static WorldEditInterface WORLDEDIT = null;

    public static void init() {
        try {
            WORLDEDIT = new codes.wasabi.xgame.ext.worldedit.basic.BasicWorldEditInterface();
            HAS_WORLDEDIT = true;
        } catch (Throwable ignored) { }
    }

    public static boolean hasWorldEditInterface() {
        return HAS_WORLDEDIT;
    }

    public static @Nullable WorldEditInterface getWorldEditInterface() {
        return WORLDEDIT;
    }

    public static @NotNull WorldEditInterface getWorldEditInterfaceAssert() throws IllegalStateException {
        if (WORLDEDIT != null) return WORLDEDIT;
        throw new IllegalStateException("No WorldEdit interface available");
    }

}
