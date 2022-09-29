package codes.wasabi.xgame.gui;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xplug.XPlug;
import codes.wasabi.xplug.lib.adventure.text.Component;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class InventoryMenu implements InventoryHolder, Listener {

    public static void openMenu(Class<? extends InventoryMenu> clazz, Player ply) {
        InventoryMenu instance;
        try {
            instance = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            XPlug.getAdventure().player(ply).sendMessage(Component.empty()
                    .append(Component.text("* Failed to open a GUI page (").color(NamedTextColor.RED))
                    .append(Component.text(e.getClass().getSimpleName()).color(NamedTextColor.DARK_RED))
                    .append(Component.text("), see console for more info").color(NamedTextColor.RED))
            );
            return;
        }
        instance.open(ply);
    }

    private final Inventory iv;
    private final int height;
    private final UUID identifier;
    public InventoryMenu(String name, int height) {
        this.identifier = UUID.randomUUID();
        this.iv = Bukkit.createInventory(this, height * 9, name);
        this.height = height;
    }

    public final int getHeight() {
        return height;
    }

    public final void open(Player ply) {
        ply.openInventory(iv);
        boolean wasRunning = tasksRunning;
        this.update();
        if (!wasRunning) onOpen(ply);
    }

    public final void close(Player ply) {
        UUID uuid = ply.getUniqueId();
        if (iv.getViewers().stream().anyMatch((viewer) -> viewer.getUniqueId().equals(uuid))) {
            ply.closeInventory();
        }
    }

    public final void close() {
        for (HumanEntity he : new ArrayList<>(iv.getViewers())) {
            he.closeInventory();
        }
    }

    protected abstract void onOpen(Player ply);

    protected abstract void onClose(Player ply);

    protected abstract void onTick(Player ply);

    protected boolean onClick(Player ply, int x, int y, int slot, @Nullable ItemStack item, ClickType type) {
        return true;
    }

    protected void switchTo(Class<? extends InventoryMenu> clazz, Player ply, Object... args) {
        Class<?>[] argClasses = new Class<?>[args.length];
        for (int i=0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }
        close(ply);
        InventoryMenu im;
        try {
            Constructor<?>[] con = clazz.getConstructors();
            Constructor<?> found = null;
            for (Constructor<?> c : con) {
                Class<?>[] param = c.getParameterTypes();
                if (param.length == argClasses.length) {
                    boolean allMatch = true;
                    for (int i=0; i < argClasses.length; i++) {
                        if (!(Objects.equals(param[i], argClasses[i]) || param[i].isAssignableFrom(argClasses[i]))) {
                            allMatch = false;
                            break;
                        }
                    }
                    if (allMatch) {
                        found = c;
                        break;
                    }
                }
            }
            im = (InventoryMenu) Objects.requireNonNull(found).newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
            XPlug.getAdventure().player(ply).sendMessage(Component.empty()
                    .append(Component.text("* Failed to open a GUI page (").color(NamedTextColor.RED))
                    .append(Component.text(e.getClass().getSimpleName()).color(NamedTextColor.DARK_RED))
                    .append(Component.text("), see console for more info").color(NamedTextColor.RED))
            );
            return;
        }
        im.open(ply);
    }

    public int getMaxPlayers() {
        return 1;
    }

    public boolean shouldTickAsynchronously() {
        return false;
    }

    private boolean tasksRunning = false;
    private BukkitTask tickTask = null;
    private void update() {
        if (tasksRunning) {
            if (iv.getViewers().size() < 1) {
                HandlerList.unregisterAll(this);
                if (tickTask != null) tickTask.cancel();
                tasksRunning = false;
            }
        } else {
            if (iv.getViewers().size() > 0) {
                Bukkit.getPluginManager().registerEvents(this, XGame.getInstance());
                Runnable runnable = (() -> {
                    for (HumanEntity he : iv.getViewers()) {
                        if (he instanceof Player) onTick((Player) he);
                    }
                });
                if (shouldTickAsynchronously()) {
                    tickTask = Bukkit.getScheduler().runTaskTimerAsynchronously(XGame.getInstance(), runnable, 0L, 1L);
                } else {
                    tickTask = Bukkit.getScheduler().runTaskTimer(XGame.getInstance(), runnable, 0L, 1L);
                }
                tasksRunning = true;
            }
        }
    }

    @NotNull
    @Override
    public final Inventory getInventory() {
        return iv;
    }

    public final void clear() {
        iv.clear();
    }

    public final void setItem(int slot, @Nullable ItemStack is) {
        iv.setItem(slot, is);
    }

    public final void setItem(int x, int y, @Nullable ItemStack is) {
        iv.setItem((y * 9) + x, is);
    }

    private boolean isEventRelevant(InventoryEvent event) {
        Inventory iv = event.getInventory();
        InventoryHolder holder = iv.getHolder();
        if (holder == null) return false;
        if (holder instanceof InventoryMenu) {
            InventoryMenu qual = (InventoryMenu) holder;
            return qual.identifier.equals(this.identifier);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public final void onOpenEvent(InventoryOpenEvent event) {
        if (!isEventRelevant(event)) return;
        HumanEntity he = event.getPlayer();
        if (he instanceof Player) {
            int maxPlayers = getMaxPlayers();
            List<HumanEntity> viewers = iv.getViewers();
            int numViewers = viewers.size();
            int remove = (numViewers - maxPlayers) + 1;
            int removed = 0;
            for (int i=0; i < numViewers && removed < remove; i++) {
                HumanEntity toRemove = viewers.get(i);
                if (toRemove == null) continue;
                if (toRemove.getUniqueId().equals(he.getUniqueId())) return;
                if (!(toRemove instanceof Player)) continue;
                toRemove.closeInventory();
                XPlug.getAdventure().sender(toRemove).sendMessage(Component.text("* You were kicked from the GUI, as only " + maxPlayers + " players can use it at the same time.").color(NamedTextColor.RED));
                removed++;
            }
            onOpen((Player) he);
        }
        Bukkit.getScheduler().runTask(XGame.getInstance(), this::update);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public final void onCloseEvent(InventoryCloseEvent event) {
        if (!isEventRelevant(event)) return;
        HumanEntity he = event.getPlayer();
        if (he instanceof Player) {
            onClose((Player) he);
        }
        Bukkit.getScheduler().runTask(XGame.getInstance(), this::update);
    }

    @EventHandler
    public final void onClickEvent(InventoryClickEvent event) {
        if (!isEventRelevant(event)) return;
        HumanEntity he = event.getWhoClicked();
        if (he instanceof Player) {
            Player ply = (Player) he;
            int slot = event.getSlot();
            int x = slot % 9;
            int y = (slot - x) / 9;
            if (onClick(ply, x, y, slot, iv.getItem(slot), event.getClick())) event.setCancelled(true);
        }
    }

    @EventHandler
    public final void onDragEvent(InventoryDragEvent event) {
        if (isEventRelevant(event)) event.setCancelled(true);
    }

}
