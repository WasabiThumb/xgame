package codes.wasabi.xgame.util;

import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;
import codes.wasabi.xplug.lib.matlib.struct.applicator.block.BlockMaterialApplicator;
import codes.wasabi.xplug.lib.matlib.struct.applicator.block.DataBlockMaterialApplicator;
import codes.wasabi.xplug.lib.paperlib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static codes.wasabi.xgame.internals.InternalClasses.*;
import static codes.wasabi.xgame.internals.InternalUtil.*;
import static codes.wasabi.xgame.internals.InternalMethods.*;
import static codes.wasabi.xgame.internals.InternalConstructors.*;
import static codes.wasabi.xgame.internals.InternalFields.*;

// Adapted from https://www.spigotmc.org/threads/how-to-set-blocks-incredibly-fast.476097/
public class FastEditSession {

    private final World world;
    private final Object nmsWorld;
    private final Map<Object, ModMapEntry> modMap = new HashMap<>();

    public FastEditSession(World world) {
        this.world = world;
        Object craftWorld = CB_CRAFT_WORLD.cast(world);
        nmsWorld = invokeMethod(craftWorld, CB_CRAFT_WORLD_GET_HANDLE);
    }

    private static class ModMapEntry {
        public Object iBlockData;
        public int chunkX;
        public int chunkZ;
    }

    public void clear() {
        modMap.clear();
    }

    @SuppressWarnings("unused")
    public void setBlock(int x, int y, int z, Material material) {
        setBlock(x, y, z, material, (byte) 0);
    }

    public void setBlock(int x, int y, int z, MetaMaterial material) {
        Consumer<Block> apply = material.getBlockApplicator();
        if (apply instanceof DataBlockMaterialApplicator) {
            DataBlockMaterialApplicator data = (DataBlockMaterialApplicator) apply;
            setBlock(x, y, z, data.getMaterial(), data.getData());
        } else if (apply instanceof BlockMaterialApplicator) {
            BlockMaterialApplicator root = (BlockMaterialApplicator) apply;
            setBlock(x, y, z, root.getMaterial(), (byte) 0);
        } else {
            setBlock(x, y, z, material.getBukkitMaterial(), (byte) 0);
        }
    }

    public void setBlock(int x, int y, int z, MetaMaterial material, byte data) {
        Consumer<Block> apply = material.getBlockApplicator();
        if (apply instanceof BlockMaterialApplicator) {
            BlockMaterialApplicator root = (BlockMaterialApplicator) apply;
            setBlock(x, y, z, root.getMaterial(), data);
        } else {
            setBlock(x, y, z, material.getBukkitMaterial(), data);
        }
    }

    public void setBlock(int x, int y, int z, Material material, byte data) {
        Object blockPosition = construct(NMS_BLOCK_POSITION_NEW, x, y, z);
        Object nmsBlock = invokeMethod(null, CB_CRAFT_MAGIC_NUMBERS_GET_BLOCK, material);
        Object iBlockData;
        if ((!PaperLib.isVersion(13)) && data != ((byte) 0)) {
            iBlockData = invokeMethod(nmsBlock, NMS_BLOCK_FROM_LEGACY_DATA, (int) data);
        } else {
            iBlockData = invokeMethod(nmsBlock, NMS_BLOCK_GET_BLOCK_DATA);
        }
        ModMapEntry mme = new ModMapEntry();
        mme.iBlockData = iBlockData;
        mme.chunkX = (x >> 4);
        mme.chunkZ = (z >> 4);
        modMap.put(blockPosition, mme);
    }

    public void clearChunk(int chunkX, int chunkZ) {
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        Object nmsBlock = invokeMethod(null, CB_CRAFT_MAGIC_NUMBERS_GET_BLOCK, Material.AIR);
        Object iBlockData = invokeMethod(nmsBlock, NMS_BLOCK_GET_BLOCK_DATA);
        int minY = 0;
        int maxY = world.getMaxHeight();
        if (PaperLib.isVersion(17)) {
            minY = world.getMinHeight();
        }
        for (int y=minY; y < maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block.getType().equals(Material.AIR)) continue;
                    Object blockPosition = construct(NMS_BLOCK_POSITION_NEW, block.getX(), block.getY(), block.getZ());
                    ModMapEntry mme = new ModMapEntry();
                    mme.iBlockData = iBlockData;
                    mme.chunkX = chunkX;
                    mme.chunkZ = chunkZ;
                    modMap.put(blockPosition, mme);
                }
            }
        }
    }

    public void apply() {
        Map<Object, int[]> nmsChunks = new HashMap<>();
        Object chunkProvider = invokeMethod(nmsWorld, NMS_WORLD_GET_CHUNK_PROVIDER);
        for (Map.Entry<Object, ModMapEntry> entry : modMap.entrySet()) {
            Object blockPosition = entry.getKey();
            ModMapEntry mme = entry.getValue();
            Object nmsChunk = invokeMethod(chunkProvider, NMS_I_CHUNK_PROVIDER_GET_CHUNK_AT, mme.chunkX, mme.chunkZ, true);
            nmsChunks.put(nmsChunk, new int[]{ mme.chunkX, mme.chunkZ });
            invokeMethod(nmsChunk, NMS_CHUNK_SET_TYPE, blockPosition, mme.iBlockData, false);
        }
        //
        Object lightEngine = invokeMethod(chunkProvider, NMS_I_CHUNK_PROVIDER_GET_LIGHT_ENGINE);
        for (Object blockPos : modMap.keySet()) {
            invokeMethod(lightEngine, NMS_LIGHT_ENGINE_CHECK_BLOCK, blockPos);
        }
        //
        for (Map.Entry<Object, int[]> entry : nmsChunks.entrySet()) {
            Object nmsChunk = entry.getKey();
            int[] coord = entry.getValue();
            int x = coord[0];
            int z = coord[1];
            //
            Object[] packets;
            if (PaperLib.isVersion(18)) {
                packets = new Object[2];
                packets[0] = construct(NMS_PACKET_PLAY_OUT_UNLOAD_CHUNK_NEW_1_9_4, x, z);
                packets[1] = construct(NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_18, nmsChunk, lightEngine, null, null, true);
            } else if (PaperLib.isVersion(17)) {
                packets = new Object[3];
                packets[0] = construct(NMS_PACKET_PLAY_OUT_UNLOAD_CHUNK_NEW_1_9_4, x, z);
                packets[1] = construct(NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_9_4, nmsChunk, 65535);
                Object chunkPos = invokeMethod(nmsChunk, NMS_CHUNK_GET_POS);
                packets[2] = construct(NMS_PACKET_PLAY_OUT_LIGHT_UPDATE_NEW_1_17, chunkPos, lightEngine, null, null, true);
            } else if (PaperLib.isVersion(14)) {
                packets = new Object[3];
                packets[0] = construct(NMS_PACKET_PLAY_OUT_UNLOAD_CHUNK_NEW_1_9_4, x, z);
                packets[1] = construct(NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_9_4, nmsChunk, 65535);
                Object chunkPos = invokeMethod(nmsChunk, NMS_CHUNK_GET_POS);
                packets[2] = construct(NMS_PACKET_PLAY_OUT_LIGHT_UPDATE_NEW_1_14, chunkPos, lightEngine);
            } else if (PaperLib.isVersion(9, 4)) {
                packets = new Object[2];
                packets[0] = construct(NMS_PACKET_PLAY_OUT_UNLOAD_CHUNK_NEW_1_9_4, x, z);
                packets[1] = construct(NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_9_4, nmsChunk, 65535);
            } else {
                packets = new Object[1];
                packets[0] = construct(NMS_PACKET_PLAY_OUT_MAP_CHUNK_NEW_1_8, nmsChunk, true, 65535);
            }
            for (Player ply : world.getPlayers()) {
                int dist = Bukkit.getViewDistance() + 1;
                Chunk bukkitChunk = ply.getLocation().getChunk();
                int chunkX = bukkitChunk.getX();
                int chunkZ = bukkitChunk.getZ();
                if (x < chunkX - dist ||
                        x > chunkX + dist ||
                        z < chunkZ - dist ||
                        z > chunkZ + dist) continue;
                Object craftPlayer = CB_CRAFT_PLAYER.cast(ply);
                Object nmsPlayer = invokeMethod(craftPlayer, CB_CRAFT_PLAYER_GET_HANDLE);
                Object playerConnection = getField(nmsPlayer, NMS_ENTITY_PLAYER_CONNECTION);
                for (Object packet : packets) {
                    invokeMethod(playerConnection, NMS_PLAYER_CONNECTION_SEND_PACKET, NMS_PACKET.cast(packet));
                }
            }
        }
        //
        clear();
    }

}
