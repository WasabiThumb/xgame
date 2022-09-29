package codes.wasabi.xgame.resource;

import codes.wasabi.xgame.util.StreamUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class CopyAssetSource implements AssetSource {

    @Override
    public @Nullable InputStream read(String file) {
        InputStream is = AssetSource.DATA.read(file);
        if (is != null) return is;
        is = AssetSource.BUNDLED.read(file);
        boolean b64 = false;
        if (is == null) {
            b64 = true;
            is = AssetSource.BUNDLED.read(file + ".base");
            if (is == null) return null;
        }
        InputStream ret = null;
        try {
            byte[] bytes = StreamUtil.readAllBytes(is);
            if (b64) {
                bytes = Base64.getDecoder().decode(bytes);
            }
            ret = new ByteArrayInputStream(bytes);
            AssetSource.DATA.writeBytes(file, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public @NotNull OutputStream write(String file) throws IOException {
        return AssetSource.DATA.write(file);
    }

    @Override
    public Set<String> list(String directory, EnumSet<AssetType> types, boolean deep) {
        Set<String> ret = new HashSet<>();
        ret.addAll(AssetSource.BUNDLED.list(directory, types, deep));
        ret.addAll(AssetSource.DATA.list(directory, types, deep));
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public boolean has(String file) {
        return AssetSource.DATA.has(file) || AssetSource.BUNDLED.has(file);
    }

}
