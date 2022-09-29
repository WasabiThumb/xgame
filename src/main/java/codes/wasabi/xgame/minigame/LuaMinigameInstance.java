package codes.wasabi.xgame.minigame;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xgame.resource.AssetSource;
import codes.wasabi.xgame.util.MetaSchematic;
import codes.wasabi.xgame.world.MinigameWorldRegion;
import codes.wasabi.xplug.XPlug;
import codes.wasabi.xplug.lib.luaj.vm2.*;
import codes.wasabi.xplug.lib.luaj.vm2.lib.ZeroArgFunction;
import codes.wasabi.xplug.lib.matlib.MaterialLib;
import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;
import codes.wasabi.xplug.lib.paperlib.PaperLib;
import codes.wasabi.xplug.library.enums;
import codes.wasabi.xplug.platform.spigot.base.SpigotLuaToolkit;
import codes.wasabi.xplug.platform.spigot.base.SpigotLuaTypeAdapter;
import codes.wasabi.xplug.struct.inventory.LuaItemStack;
import codes.wasabi.xplug.struct.world.LuaChunk;
import codes.wasabi.xplug.util.LuaOutputHandler;
import codes.wasabi.xplug.util.LuaSandbox;
import codes.wasabi.xplug.util.func.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

public class LuaMinigameInstance extends MinigameInstance {

    private static int projectId = 0;
    private static Method createGlobalsMethod = null;
    private static Globals createGlobals(LuaSandbox sandbox) {
        projectId++;
        String project = "xgame-" + projectId;
        if (createGlobalsMethod == null) {
            try {
                createGlobalsMethod = LuaSandbox.class.getDeclaredMethod("createGlobals", String.class, LuaOutputHandler.class);
                createGlobalsMethod.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LuaOutputHandler out = LuaOutputHandler.ofAudience("XGame", XPlug.getAdventure().console());
        try {
            return (Globals) createGlobalsMethod.invoke(sandbox, project, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to create new globals");
    }

    private final LuaValue struct;
    public LuaMinigameInstance(String code) {
        LuaSandbox sandbox = XGame.getSandbox();
        Globals globals = createGlobals(sandbox);
        struct = LuaValue.tableOf();
        struct.set("IsRunning", new GetterFunction(this::isRunning));
        struct.set("End", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                LuaMinigameInstance.this.stop();
                return LuaValue.NIL;
            }
        });
        struct.set("GetRegion", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                final MinigameWorldRegion region = getRegion();
                LuaTable ret = LuaValue.tableOf();
                SpigotLuaToolkit slt = SpigotLuaToolkit.getInstance();
                final SpigotLuaTypeAdapter adapter = slt.getTypeAdapter();
                ret.set("GetWorld", new GetterFunction(() -> adapter.convertWorld(region.getWorld())));
                ret.set("GetCenterChunk", new GetterFunction(() -> adapter.convertChunk(region.getCenterChunk())));
                ret.set("GetCenterBlock", new GetterFunction(() -> adapter.convertBlock(region.getCenterBlock())));
                ret.set("GetMinChunk", new GetterFunction(() -> adapter.convertChunk(region.getMinChunk())));
                ret.set("GetMaxChunk", new GetterFunction(() -> adapter.convertChunk(region.getMaxChunk())));
                ret.set("SizeX", new GetterFunction(region::getSizeX));
                ret.set("SizeZ", new GetterFunction(region::getSizeZ));
                ret.set("GetChunks", new GetterFunction(() -> {
                    List<LuaValue> list = new ArrayList<>();
                    for (Iterator<Chunk> iterator = region.getChunkIterator(); iterator.hasNext(); ) {
                        Chunk c = iterator.next();
                        LuaChunk lc = adapter.convertChunk(c);
                        list.add(lc.getLuaValue());
                    }
                    return LuaTable.listOf(list.toArray(new LuaValue[0]));
                }));
                return ret;
            }
        });
        struct.set("GetPlayers", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                Set<Player> players = getPlayers();
                int size = players.size();
                LuaValue[] values = new LuaValue[size];
                SpigotLuaToolkit slt = SpigotLuaToolkit.getInstance();
                final SpigotLuaTypeAdapter adapter = slt.getTypeAdapter();
                Iterator<Player> iter = players.iterator();
                for (int i=0; i < size; i++) {
                    if (iter.hasNext()) {
                        values[i] = adapter.convertPlayer(players.iterator().next()).getLuaValue();
                    } else {
                        values[i] = LuaValue.NIL;
                    }
                }
                return LuaTable.listOf(values);
            }
        });
        struct.set("Broadcast", new OneArgMetaFunction() {
            @Override
            protected LuaValue call(LuaTable luaTable, LuaValue luaValue) {
                broadcast(luaValue.tojstring());
                return LuaValue.NIL;
            }
        });
        struct.set("PasteSchematic", new TwoArgMetaFunction() {
            @Override
            protected LuaValue call(LuaTable luaTable, LuaValue luaValue, LuaValue luaValue1) {
                String name = luaValue.tojstring();
                Location l = SpigotLuaToolkit.getAdapter().convertLocation(luaValue1);
                if (l == null) throw new LuaError("Argument #2 is not a location!");
                MetaSchematic mm = AssetSource.COPY.readSchematic("schem/" + name + ".mschem");
                if (mm == null) return LuaValue.FALSE;
                Collection<Chunk> modified = mm.apply(l);
                addModifiedChunks(modified);
                return LuaValue.valueOf(modified.size() > 0);
            }
        });
        struct.set("BroadcastSound", new VarArgMetaFunction() {
            @Override
            protected Varargs call(LuaTable luaTable, Varargs varargs) {
                // sounds have major changes at 1.8, 1.12 and 1.13
                Sound snd = null;
                int narg = varargs.narg();
                for (int i=0; i < narg; i++) {
                    String s = varargs.tojstring(i + 1).toUpperCase(Locale.ENGLISH);
                    try {
                        snd = Sound.valueOf(s);
                        break;
                    } catch (Exception ignored) { }
                }
                if (snd == null) return LuaValue.varargsOf(new LuaValue[]{ LuaValue.FALSE });
                boolean newMethod = PaperLib.isVersion(11);
                for (Player p : getPlayers()) {
                    if (newMethod) {
                        p.playSound(p.getLocation(), snd, SoundCategory.MASTER, 1f, 1f);
                    } else {
                        p.playSound(p.getLocation(), snd, 1f, 1f);
                    }
                }
                return LuaValue.varargsOf(new LuaValue[]{ LuaValue.TRUE });
            }
        });
        // TODO : Add into XPlug eventually
        struct.set("GiveEffect", new VarArgMetaFunction() {
            @Override
            protected Varargs call(LuaTable luaTable, Varargs varargs) {
                Player ply = SpigotLuaToolkit.getAdapter().convertPlayer(varargs.arg(1));
                if (ply == null) return LuaValue.NIL;
                int duration = Math.max(varargs.toint(2), 1);
                int amplifier = Math.max(varargs.toint(3), 1);
                PotionEffectType type = null;
                for (int i=4; i <= varargs.narg(); i++) {
                    String s = varargs.tojstring(i);
                    try {
                        type = PotionEffectType.getByName(s);
                    } catch (Exception ignored) { }
                }
                if (type == null) return LuaValue.NIL;
                PotionEffect effect = new PotionEffect(type, duration, amplifier, false, false);
                ply.addPotionEffect(effect);
                return LuaValue.NIL;
            }
        });
        struct.set("CreateExplosion", new TwoArgMetaFunction() {
            @Override
            protected LuaValue call(LuaTable luaTable, LuaValue luaValue, LuaValue luaValue1) {
                Location loc = SpigotLuaToolkit.getAdapter().convertLocation(luaValue);
                if (loc == null) return LuaValue.NIL;
                float mag = (luaValue1.isnumber() ? luaValue1.tofloat() : 4f);
                World w = loc.getWorld();
                if (w != null) w.createExplosion(loc, mag, false);
                return LuaValue.NIL;
            }
        });
        globals.set("MG", struct);
        try {
            globals.load(code).call();
        } catch (LuaError le) {
            XGame.getInstance().getLogger().warning("LUA Error when creating new minigame instance");
            le.printStackTrace();
        }
    }

    private @Nullable LuaFunction getFunction(String key) {
        LuaValue lv = struct.get(key);
        if (lv == null) return null;
        if (lv.isfunction()) {
            return lv.checkfunction();
        }
        return null;
    }

    private void tryCallFunction(String key) {
        LuaFunction lf = getFunction(key);
        if (lf != null) lf.call(struct);
    }

    private LuaValue getField(String key, LuaValue defaultValue) {
        LuaValue lv = struct.get(key);
        if (lv == null || lv.isnil()) {
            lv = defaultValue;
        }
        return lv;
    }

    @Override
    public String getName() {
        return getField("Name", LuaValue.valueOf("Untitled")).tojstring();
    }

    @Override
    public String getDescription() {
        return getField("Description", LuaValue.valueOf("")).tojstring();
    }

    @Override
    public MetaMaterial getIcon() {
        String materialName = getField("Icon", LuaValue.valueOf("STONE")).tojstring();
        MetaMaterial mm = MaterialLib.getMaterial(materialName);
        if (mm == null) mm = super.getIcon();
        return mm;
    }

    @Override
    protected void onStart() {
        tryCallFunction("Start");
    }

    @Override
    protected void onTick() {
        tryCallFunction("Tick");
    }

    @Override
    protected void onStop() {
        tryCallFunction("Stop");
    }

    @Override
    protected void onLeave(Player ply) {
        LuaFunction lf = getFunction("Leave");
        if (lf != null) lf.call(struct, SpigotLuaToolkit.getAdapter().convertPlayer(ply).getLuaValue());
    }

    @Override
    public int getMinPlayers() {
        return Math.max(getField("MinPlayers", LuaValue.ONE).toint(), 1);
    }

    @Override
    public int getMaxPlayers() {
        return Math.max(getField("MaxPlayers", LuaValue.ONE).toint(), getMinPlayers());
    }

    @Override
    public @NotNull Location getSpawnPoint(Player player) {
        LuaFunction lf = getFunction("GetSpawn");
        if (lf == null) return getRegion().getCenterBlock().getLocation();
        SpigotLuaTypeAdapter adapter = SpigotLuaToolkit.getAdapter();
        LuaValue lv = lf.call(struct, adapter.convertPlayer(player).getLuaValue());
        Location loc = adapter.convertLocation(lv);
        if (loc != null) return loc;
        return getRegion().getCenterBlock().getLocation();
    }

    @Override
    public @Nullable Location getRespawnPoint(Player player) {
        LuaFunction lf = getFunction("GetSpectatorSpawn");
        if (lf == null) return null;
        SpigotLuaTypeAdapter adapter = SpigotLuaToolkit.getAdapter();
        LuaValue lv = lf.call(struct, adapter.convertPlayer(player).getLuaValue());
        return adapter.convertLocation(lv);
    }

    @Override
    public @Nullable MetaSchematic getSchematic() {
        LuaValue lv = getField("Schematic", LuaValue.NIL);
        if (!lv.isstring()) return null;
        String name = lv.tojstring();
        return AssetSource.COPY.readSchematic("schem/" + name + ".mschem");
    }

    @Override
    public @Nullable Location getSchematicOrigin() {
        LuaFunction lf = getFunction("GetSchematicPos");
        if (lf == null) return null;
        LuaValue lv = lf.call(struct);
        Location loc = SpigotLuaToolkit.getAdapter().convertLocation(lv);
        if (loc != null) loc.setWorld(getRegion().getWorld());
        return loc;
    }

    @Override
    public @NotNull GameMode getDefaultGameMode() {
        LuaValue lv = getField("GameMode", LuaValue.valueOf(enums.GM_ADVENTURE));
        int code = lv.toint();
        return SpigotLuaToolkit.getAdapter().convertGameMode(code);
    }

    @Override
    protected boolean onDamage(Player ply, @Nullable Entity attacker, double damage) {
        LuaFunction lf = getFunction("OnDamage");
        if (lf == null) return false;
        SpigotLuaTypeAdapter adapter = SpigotLuaToolkit.getAdapter();
        Varargs ret = lf.invoke(LuaValue.varargsOf(new LuaValue[]{
                struct,
                adapter.convertPlayer(ply).getLuaValue(),
                attacker == null ? LuaValue.NIL : adapter.convertEntity(attacker).getLuaValue(),
                LuaValue.valueOf(damage)
        }));
        return ret.toboolean(1);
    }

    @Override
    protected boolean onPreDeath(Player ply, @Nullable Entity attacker) {
        LuaFunction lf = getFunction("OnDeath");
        if (lf == null) return false;
        SpigotLuaTypeAdapter adapter = SpigotLuaToolkit.getAdapter();
        LuaValue lv = lf.call(
                struct,
                adapter.convertPlayer(ply).getLuaValue(),
                attacker == null ? LuaValue.NIL : adapter.convertEntity(attacker).getLuaValue()
        );
        return lv.toboolean();
    }

    @Override
    protected boolean onMove(Player ply, Location from, Location to) {
        LuaFunction lf = getFunction("OnMove");
        if (lf == null) return false;
        SpigotLuaTypeAdapter adapter = SpigotLuaToolkit.getAdapter();
        Varargs varargs = lf.invoke(LuaValue.varargsOf(new LuaValue[]{
                struct,
                adapter.convertPlayer(ply).getLuaValue(),
                adapter.convertLocation(from).getLuaValue(),
                adapter.convertLocation(to).getLuaValue()
        }));
        return varargs.toboolean(1);
    }

    @Override
    protected boolean onChat(Player ply, String message) {
        LuaFunction lf = getFunction("OnChat");
        if (lf == null) return false;
        SpigotLuaTypeAdapter adapter = SpigotLuaToolkit.getAdapter();
        LuaValue lv = lf.call(struct, adapter.convertPlayer(ply).getLuaValue(), LuaValue.valueOf(message));
        return lv.toboolean();
    }

    @Override
    protected boolean onPlace(Player ply, Block block) {
        LuaFunction lf = getFunction("OnPlace");
        if (lf == null) return false;
        SpigotLuaTypeAdapter adapter = SpigotLuaToolkit.getAdapter();
        LuaValue lv = lf.call(struct, adapter.convertPlayer(ply).getLuaValue(), adapter.convertBlock(block).getLuaValue());
        return lv.toboolean();
    }

    @Override
    protected boolean onBreak(Player ply, Block block) {
        LuaFunction lf = getFunction("OnBreak");
        if (lf == null) return false;
        SpigotLuaTypeAdapter adapter = SpigotLuaToolkit.getAdapter();
        LuaValue lv = lf.call(struct, adapter.convertPlayer(ply).getLuaValue(), adapter.convertBlock(block).getLuaValue());
        return lv.toboolean();
    }

    @Override
    protected boolean onInteract(Player ply, @Nullable ItemStack item, boolean leftClick, @Nullable Entity entity) {
        LuaFunction lf = getFunction("OnInteract");
        if (lf == null) return false;
        SpigotLuaTypeAdapter adapter = SpigotLuaToolkit.getAdapter();
        LuaValue is;
        if (item == null) {
            is = LuaValue.NIL;
        } else {
            LuaItemStack lis = adapter.convertItemStack(item);
            if (lis == null) {
                is = LuaValue.NIL;
            } else {
                is = lis.getLuaValue();
            }
        }
        Varargs varargs = lf.invoke(LuaValue.varargsOf(new LuaValue[]{
                struct,
                adapter.convertPlayer(ply).getLuaValue(),
                is,
                LuaValue.valueOf(leftClick),
                (entity == null ? LuaValue.NIL : adapter.convertEntity(entity).getLuaValue())
        }));
        return varargs.toboolean(1);
    }

}
