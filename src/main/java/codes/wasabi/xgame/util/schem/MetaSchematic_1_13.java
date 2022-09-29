package codes.wasabi.xgame.util.schem;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xgame.util.FastEditSession;
import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class MetaSchematic_1_13 extends BaseMetaSchematic<BlockData> {

    private final Class<? extends BlockData> baseDataClass = Material.AIR.createBlockData().getClass();

    @Override
    protected @Nullable BlockData getData(Block block) {
        return block.getBlockData();
    }

    @Override
    protected void writeDataSection(ObjectOutputStream oos, @Nullable BlockData data) throws IOException {
        oos.writeByte(0);
        if (data == null || data.getClass().equals(baseDataClass)) {
            oos.writeInt(0);
        } else {
            String dataString = data.getAsString();
            byte[] dataBytes = dataString.getBytes(StandardCharsets.UTF_8);
            oos.writeInt(dataBytes.length);
            oos.write(dataBytes);
        }
    }

    @Override
    protected @Nullable BlockData readDataSection(ObjectInputStream ois) throws IOException {
        ois.skipBytes(1);
        int len = ois.readInt();
        if (len < 1) return null;
        byte[] bytes = new byte[len];
        int ct = 0;
        while (ct < len) {
            int read = ois.read(bytes, ct, len - ct);
            if (read < 0) throw new IOException("Unexpected end of stream");
            ct += read;
        }
        String dataString = new String(bytes, StandardCharsets.UTF_8);
        BlockData bd;
        try {
            bd = Bukkit.createBlockData(dataString);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return bd;
    }

    @Override
    protected void applyData(FastEditSession session, int x, int y, int z, MetaMaterial material, @Nullable BlockData data) {
        session.setBlock(x, y, z, material);
    }

    @Override
    protected boolean shouldCoallateData() {
        return true;
    }

    @Override
    protected void batchDataUpdate(World world, Map<Vector, BlockData> dataMap, int dataUpdatesPerTick) {
        Set<Vector> keys = new HashSet<>(dataMap.keySet());
        if (keys.size() < 1) return;
        final AtomicReference<BukkitTask> atomic = new AtomicReference<>(null);
        atomic.set(Bukkit.getScheduler().runTaskTimer(XGame.getInstance(), () -> {
            Set<Vector> newKeys = new HashSet<>();
            int count = 0;
            for (Vector v : keys) {
                if (count < dataUpdatesPerTick) {
                    BlockData bd = dataMap.get(v);
                    Location loc = v.toLocation(world);
                    Block block = loc.getBlock();
                    BlockData currentData = block.getBlockData();
                    if (currentData.getClass().equals(bd.getClass())) {
                        block.setBlockData(bd);
                    }
                } else {
                    newKeys.add(v);
                }
                count++;
            }
            if (count == 0) {
                BukkitTask self = atomic.get();
                if (self != null) self.cancel();
            }
            keys.clear();
            keys.addAll(newKeys);
        }, 0L, 1L));
    }

}
