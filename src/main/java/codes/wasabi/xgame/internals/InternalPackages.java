package codes.wasabi.xgame.internals;

import org.bukkit.Bukkit;

public final class InternalPackages {

    public static String CRAFTBUKKIT;
    public static String NMS;

    public static void init() throws IllegalStateException {
        String packageName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        CRAFTBUKKIT = new String(new char[] { 'o', 'r', 'g', '.', 'b', 'u', 'k', 'k', 'i', 't', '.', 'c', 'r', 'a', 'f', 't', 'b', 'u', 'k', 'k', 'i', 't', '.'}) + packageName;
        NMS = new String(new char[] { 'n', 'e', 't', '.', 'm', 'i', 'n', 'e', 'c', 'r', 'a', 'f', 't' });
        try {
            Class.forName(NMS + ".server.level.WorldServer");
        } catch (ClassNotFoundException | LinkageError e) {
            try {
                Class.forName(NMS + ".server.level.ServerLevel");
            } catch (ClassNotFoundException | LinkageError e1) {
                NMS += ".server." + packageName;
                try {
                    Class.forName(NMS + ".WorldServer");
                } catch (ClassNotFoundException | LinkageError e2) {
                    throw new IllegalStateException(e1);
                }
            }
        }
        try {
            Class.forName(CRAFTBUKKIT + ".CraftServer");
        } catch (ClassNotFoundException | LinkageError e) {
            throw new IllegalStateException(e);
        }
    }

}
