package codes.wasabi.xgame.util;

import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PlayerState {

    public static PlayerState identity() {
        World baseWorld = null;
        for (World w : Bukkit.getWorlds()) {
            if (!w.getEnvironment().equals(World.Environment.NORMAL)) continue;
            baseWorld = w;
            if (w.getName().equalsIgnoreCase("world")) break;
        }
        return new PlayerState(
                20d,
                20,
                baseWorld != null ? baseWorld.getSpawnLocation() : null,
                new ItemStack[0],
                0,
                GameMode.SURVIVAL,
                Collections.emptyList()
        );
    }

    public static double getMaxHealth(Player player) {
        double maxHealth;
        if (PaperLib.isVersion(9)) {
            org.bukkit.attribute.AttributeInstance attr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                maxHealth = attr.getValue();
            } else {
                maxHealth = player.getMaxHealth();
            }
        } else {
            maxHealth = player.getMaxHealth();
        }
        return maxHealth;
    }

    public static PlayerState identity(Player player) {
        return new PlayerState(
                getMaxHealth(player),
                20,
                player.getLocation(),
                new ItemStack[0],
                0,
                GameMode.SURVIVAL,
                Collections.emptyList()
        );
    }

    public static PlayerState from(Player player) {
        return new PlayerState(
                player.getHealth(),
                player.getFoodLevel(),
                player.getLocation(),
                player.getInventory().getContents(),
                player.getTotalExperience(),
                player.getGameMode(),
                player.getActivePotionEffects()
        );
    }

    private double health;
    private int foodLevel;
    private Location location;
    private ItemStack[] inventory;
    private int exp;
    private GameMode gameMode;
    private final Collection<PotionEffect> effects;
    public PlayerState(double health, int foodLevel, @Nullable Location location, ItemStack[] inventory, int exp, GameMode gameMode, Collection<PotionEffect> effects) {
        this.health = health;
        this.foodLevel = foodLevel;
        this.location = location == null ? null : location.clone();
        this.inventory = Arrays.copyOf(inventory, inventory.length);
        this.exp = exp;
        this.gameMode = gameMode;
        this.effects = new ArrayList<>(effects);
    }

    public void apply(Player player) {
        player.setHealth(health);
        player.setFoodLevel(foodLevel);
        // if (location != null) PaperLib.teleportAsync(player, location);
        player.teleport(location);
        player.getInventory().setContents(inventory);
        player.setTotalExperience(exp);
        player.setGameMode(gameMode);
        for (PotionEffect pe : player.getActivePotionEffects()) {
            player.removePotionEffect(pe.getType());
        }
        player.addPotionEffects(effects);
    }

    public byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeDouble(health);
            oos.writeInt(foodLevel);
            if (location != null) {
                oos.writeByte(1);
                World w = location.getWorld();
                if (w != null) {
                    String name = w.getName();
                    byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
                    oos.writeInt(nameBytes.length);
                    oos.write(nameBytes);
                } else {
                    oos.writeInt(0);
                }
                oos.writeDouble(location.getX());
                oos.writeDouble(location.getY());
                oos.writeDouble(location.getZ());
                oos.writeFloat(location.getYaw());
                oos.writeFloat(location.getPitch());
            } else {
                oos.writeByte(0);
            }
            oos.writeInt(inventory.length);
            for (ItemStack is : inventory) {
                if (is == null) {
                    oos.writeInt(0);
                } else {
                    byte[] itemBytes = ItemSerializer.serialize(is);
                    oos.writeInt(itemBytes.length);
                    oos.write(itemBytes);
                }
            }
            oos.writeInt(exp);
            String name = gameMode.name();
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            oos.writeInt(nameBytes.length);
            oos.write(nameBytes);
            List<PotionEffect> pes = new ArrayList<>(effects);
            oos.writeInt(pes.size());
            for (PotionEffect pe : pes) {
                String typeName = pe.getType().getName();
                byte[] typeNameBytes = typeName.getBytes(StandardCharsets.UTF_8);
                oos.writeInt(typeNameBytes.length);
                oos.write(typeNameBytes);
                oos.writeInt(pe.getAmplifier());
                oos.writeInt(pe.getDuration());
                boolean hasIcon = true;
                if (PaperLib.isVersion(13)) {
                    hasIcon = pe.hasIcon();
                }
                oos.writeByte(
                        (pe.isAmbient() ? (byte) 1 : (byte) 0)
                        | (pe.hasParticles() ? (byte) 2 : (byte) 0)
                        | (hasIcon ? (byte) 4 : (byte) 0)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    public static @NotNull PlayerState deserialize(byte[] bytes) throws IOException {
        PlayerState ret;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            double health = ois.readDouble();
            int foodLevel = ois.readInt();
            Location loc = null;
            if ((ois.readByte()) != ((byte) 0)) {
                int nameLen = ois.readInt();
                World world = null;
                if (nameLen > 0) {
                    byte[] nameBytes = StreamUtil.readNBytes(ois, nameLen);
                    String name = new String(nameBytes, StandardCharsets.UTF_8);
                    world = Bukkit.getWorld(name);
                }
                loc = new Location(
                        world,
                        ois.readDouble(),
                        ois.readDouble(),
                        ois.readDouble(),
                        ois.readFloat(),
                        ois.readFloat()
                );
            }
            int size = ois.readInt();
            ItemStack[] inventory = new ItemStack[size];
            for (int i=0; i < size; i++) {
                int count = ois.readInt();
                if (count < 1) {
                    inventory[i] = null;
                } else {
                    byte[] dt = StreamUtil.readNBytes(ois, count);
                    inventory[i] = ItemSerializer.deserialize(dt);
                }
            }
            int exp = ois.readInt();
            GameMode gm = GameMode.SURVIVAL;
            int len = ois.readInt();
            byte[] nameBytes = StreamUtil.readNBytes(ois, len);
            String name = new String(nameBytes, StandardCharsets.UTF_8);
            try {
                gm = GameMode.valueOf(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<PotionEffect> fx = new ArrayList<>();
            len = ois.readInt();
            for (int q=0; q < len; q++) {
                int typeNameLen = ois.readInt();
                byte[] typeNameBytes = StreamUtil.readNBytes(ois, typeNameLen);
                String typeName = new String(typeNameBytes, StandardCharsets.UTF_8);
                PotionEffectType pet = PotionEffectType.getByName(typeName);
                int amplifier = ois.readInt();
                int duration = ois.readInt();
                byte flags = ois.readByte();
                if (pet == null) continue;
                PotionEffect pe;
                if (PaperLib.isVersion(13)) {
                    pe = new PotionEffect(
                            pet,
                            duration,
                            amplifier,
                            ((flags & (byte) 1) == ((byte) 1)),
                            ((flags & (byte) 2) == ((byte) 2)),
                            ((flags & (byte) 4) == ((byte) 4))
                    );
                } else {
                    pe = new PotionEffect(
                            pet,
                            duration,
                            amplifier,
                            ((flags & (byte) 1) == ((byte) 1)),
                            ((flags & (byte) 2) == ((byte) 2))
                    );
                }
                fx.add(pe);
            }
            ret = new PlayerState(health, foodLevel, loc, inventory, exp, gm, fx);
        }
        return ret;
    }

    public static @Nullable PlayerState deserializeOrNull(byte[] bytes) {
        if (bytes == null) return null;
        try {
            return deserialize(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location, boolean applyAngles) {
        Location loc = location.clone();
        if ((!applyAngles) && this.location != null) {
            loc.setYaw(this.location.getYaw());
            loc.setPitch(this.location.getPitch());
        }
        this.location = loc;
    }

    public void setLocation(Location location) {
        setLocation(location, true);
    }

    public ItemStack[] getInventory() {
        return Arrays.copyOf(this.inventory, this.inventory.length);
    }

    public void setInventory(ItemStack[] inventory) {
        this.inventory = Arrays.copyOf(inventory, inventory.length);
    }

    public int getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Collection<PotionEffect> getEffects() {
        return Collections.unmodifiableCollection(this.effects);
    }

    public void setEffects(Collection<PotionEffect> effects) {
        this.effects.clear();
        this.effects.addAll(effects);
    }

}
