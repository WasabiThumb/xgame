package codes.wasabi.xgame.util.schem;

import codes.wasabi.xgame.util.FastEditSession;
import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;
import codes.wasabi.xplug.lib.matlib.util.DataUtil;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class MetaSchematic_1_8 extends BaseMetaSchematic<Byte> {

    @Override
    protected @Nullable Byte getData(Block block) {
        byte dt = DataUtil.getData(block);
        if (dt == ((byte) 0)) return null;
        return dt;
    }

    @Override
    protected void writeDataSection(ObjectOutputStream oos, @Nullable Byte data) throws IOException {
        oos.writeByte(data == null ? ((byte) 0) : data);
        oos.writeInt(0);
    }

    @Override
    protected @Nullable Byte readDataSection(ObjectInputStream ois) throws IOException {
        byte b = ois.readByte();
        int count = ois.readInt();
        if (count > 0) ois.skipBytes(count);
        if (b == ((byte) 0)) return null;
        return b;
    }

    @Override
    protected void applyData(FastEditSession session, int x, int y, int z, MetaMaterial material, @Nullable Byte data) {
        if (data != null) {
            if (data != ((byte) 0)) {
                session.setBlock(x, y, z, material, data);
                return;
            }
        }
        session.setBlock(x, y, z, material);
    }

}
