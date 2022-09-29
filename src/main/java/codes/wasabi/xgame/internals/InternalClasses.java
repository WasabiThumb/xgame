package codes.wasabi.xgame.internals;

import static codes.wasabi.xgame.internals.InternalPackages.*;

public final class InternalClasses {

    public static Class<?> CB_CRAFT_PLAYER;
    public static Class<?> CB_CRAFT_MAGIC_NUMBERS;
    public static Class<?> CB_CRAFT_WORLD;
    public static Class<?> NMS_WORLD;
    public static Class<?> NMS_WORLD_SERVER;
    public static Class<?> NMS_CHUNK;
    public static Class<?> NMS_CHUNK_COORD_INT_PAIR;
    public static Class<?> NMS_BLOCK_POSITION;
    public static Class<?> NMS_BASE_BLOCK_POSITION;
    public static Class<?> NMS_I_BLOCK_DATA;
    public static Class<?> NMS_I_CHUNK_PROVIDER;
    public static Class<?> NMS_LIGHT_ENGINE;
    public static Class<?> NMS_ENTITY_PLAYER;
    public static Class<?> NMS_BLOCK;
    public static Class<?> NMS_PACKET_PLAY_OUT_UNLOAD_CHUNK;
    public static Class<?> NMS_PACKET_PLAY_OUT_MAP_CHUNK;
    public static Class<?> NMS_PACKET_PLAY_OUT_LIGHT_UPDATE;
    public static Class<?> NMS_PLAYER_CONNECTION;
    public static Class<?> NMS_PACKET;

    public static void init() throws IllegalStateException {
        CB_CRAFT_PLAYER = getClass(CRAFTBUKKIT, "entity.CraftPlayer", "CraftPlayer");
        CB_CRAFT_MAGIC_NUMBERS = getClass(CRAFTBUKKIT, "util.CraftMagicNumbers", "CraftMagicNumbers");
        CB_CRAFT_WORLD = getClass(CRAFTBUKKIT, "world.CraftWorld", "CraftWorld");
        NMS_WORLD = getClass(NMS, "world.level.World", "World", "world.level.Level");
        NMS_WORLD_SERVER = getClass(NMS, "server.level.WorldServer", "WorldServer", "server.level.ServerLevel");
        NMS_CHUNK = getClass(NMS, "world.level.chunk.Chunk", "Chunk", "world.level.chunk.LevelChunk");
        NMS_CHUNK_COORD_INT_PAIR = getClass(NMS, "world.level.ChunkCoordIntPair", "ChunkCoordIntPair", "world.level.ChunkPos");
        NMS_BLOCK_POSITION = getClass(NMS, "core.BlockPosition", "BlockPosition", "core.BlockPos");
        NMS_BASE_BLOCK_POSITION = getClass(NMS, "core.BaseBlockPosition", "BaseBlockPosition", "core.Vec3i");
        NMS_BLOCK = getClass(NMS, "world.level.block.Block", "Block");
        NMS_I_BLOCK_DATA = getClass(NMS, "world.level.block.state.IBlockData", "IBlockData", "world.level.block.state.BlockState");
        NMS_I_CHUNK_PROVIDER = getClass(NMS, "world.level.chunk.IChunkProvider", "IChunkProvider", "world.level.chunk.ChunkSource");
        NMS_LIGHT_ENGINE = getClass(NMS, "world.level.lighting.LightEngine", "LightEngine", "world.level.lighting.LevelLightEngine");
        NMS_ENTITY_PLAYER = getClass(NMS, "server.level.EntityPlayer", "EntityPlayer", "server.level.ServerPlayer");
        NMS_PACKET_PLAY_OUT_UNLOAD_CHUNK = getClass(NMS, true, "network.protocol.game.PacketPlayOutUnloadChunk", "PacketPlayOutUnloadChunk", "network.protocol.game.ClientboundForgetLevelChunkPacket");
        NMS_PACKET_PLAY_OUT_MAP_CHUNK = getClass(NMS, true, "network.protocol.game.ClientboundLevelChunkWithLightPacket", "network.protocol.game.PacketPlayOutMapChunk", "network.protocol.game.ClientboundLevelChunkPacket", "PacketPlayOutMapChunk");
        NMS_PACKET_PLAY_OUT_LIGHT_UPDATE = getClass(NMS, true, "network.protocol.game.PacketPlayOutLightUpdate", "PacketPlayOutLightUpdate", "network.protocol.game.ClientboundLightUpdatePacket");
        NMS_PLAYER_CONNECTION = getClass(NMS, "server.network.PlayerConnection", "PlayerConnection", "server.network.ServerGamePacketListenerImpl");
        NMS_PACKET = getClass(NMS, "network.protocol.Packet", "Packet");
    }

    private static Class<?> getClass(String pkg, boolean nullable, String... potentialNames) throws IllegalStateException, IllegalArgumentException {
        Throwable lastErr = null;
        for (String name : potentialNames) {
            String cName = pkg + "." + name;
            try {
                return Class.forName(cName);
            } catch (ClassNotFoundException | LinkageError e) {
                lastErr = e;
            }
        }
        if (nullable) return null;
        if (lastErr != null) throw new IllegalStateException(lastErr);
        throw new IllegalArgumentException("No names given!");
    }

    private static Class<?> getClass(String pkg, String... potentialNames) throws IllegalStateException, IllegalArgumentException {
        return getClass(pkg, false, potentialNames);
    }

}
