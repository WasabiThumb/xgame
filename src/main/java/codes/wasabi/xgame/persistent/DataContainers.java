package codes.wasabi.xgame.persistent;

import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.entity.Entity;

public final class DataContainers {

    private static boolean registryLoaded = false;
    private static FilesystemDataContainer.Registry registry;

    public static DataContainer get(Entity entity) {
        if (PaperLib.isVersion(14)) {
            return new codes.wasabi.xgame.persistent.NativeDataContainer(entity.getPersistentDataContainer());
        } else {
            if (!registryLoaded) load();
            return new FilesystemDataContainer(registry, entity.getUniqueId());
        }
    }

    public static void load() {
        if (!PaperLib.isVersion(14)) {
            registry = new FilesystemDataContainer.Registry();
            registry.load();
            registryLoaded = true;
        }
    }

    public static void save() {
        if (!registryLoaded) return;
        if (!PaperLib.isVersion(14)) {
            registry.save();
        }
    }

}
