package codes.wasabi.xgame;

import codes.wasabi.xgame.ext.Externals;
import codes.wasabi.xgame.internals.*;
import codes.wasabi.xgame.minigame.Minigames;
import codes.wasabi.xgame.persistent.DataContainers;
import codes.wasabi.xgame.world.MinigameWorldManager;
import codes.wasabi.xplug.XPlug;
import codes.wasabi.xplug.lib.adventure.audience.Audience;
import codes.wasabi.xplug.lib.adventure.text.Component;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import codes.wasabi.xplug.util.LuaSandbox;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class XGame extends JavaPlugin {

    private static XGame instance;

    public static XGame getInstance() {
        return instance;
    }

    public static MinigameWorldManager getWorldManager() {
        return instance.worldManager;
    }

    public static LuaSandbox getSandbox() {
        return instance.sandbox;
    }

    private Logger logger;
    private LuaSandbox sandbox;
    private CommandManager cmd;
    private MinigameWorldManager worldManager = null;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        logger.info("Initializing internals");
        try {
            InternalPackages.init();
            InternalClasses.init();
            InternalMethods.init();
            InternalConstructors.init();
            InternalFields.init();
        } catch (IllegalStateException e) {
            logger.warning("Failed! Plugin cannot continue with startup");
            throw e;
        }
        logger.info("Loading PDC");
        DataContainers.load();
        logger.info("Initializing externals");
        Externals.init();
        logger.info("Starting world manager");
        worldManager = new MinigameWorldManager("minigames");
        logger.info("Loading LUA Sandbox");
        sandbox = XPlug.getSandbox();
        logger.info("Loading LUA Minigames");
        Audience console = XPlug.getAdventure().console();
        Minigames.reloadLuaAsync(console, () -> console.sendMessage(Component.text("Loaded!").color(NamedTextColor.GREEN)));
        logger.info("Registering commands");
        cmd = new CommandManager();
        PluginCommand pc = Bukkit.getPluginCommand("xgame");
        if (pc != null) {
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);
        } else {
            logger.warning("Failed to find main plugin command");
        }
    }

    @Override
    public void onDisable() {
        if (worldManager != null) {
            logger.info("Closing world manager");
            worldManager.close();
        }
        logger.info("Saving PDC");
        DataContainers.save();
    }

}
