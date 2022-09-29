package codes.wasabi.xgame.resource;

import codes.wasabi.xgame.util.MetaSchematic;
import codes.wasabi.xgame.util.StreamUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;

public interface AssetSource {

    BundledAssetSource BUNDLED = new BundledAssetSource();
    DataAssetSource DATA = new DataAssetSource();
    CopyAssetSource COPY = new CopyAssetSource();

    static @NotNull InputStream throwIfNull(@Nullable InputStream stream) throws IOException {
        if (stream == null) throw new IOException("Stream is null");
        return stream;
    }

    static @NotNull OutputStream throwIfNull(@Nullable OutputStream stream) throws IOException {
        if (stream == null) throw new IOException("Stream is null");
        return stream;
    }

    @Nullable InputStream read(String file);

    default byte @Nullable [] readBytes(String file) {
        try (InputStream is = throwIfNull(read(file))) {
            return StreamUtil.readAllBytes(is);
        } catch (IOException e) {
            return null;
        }
    }

    default @Nullable String readString(String file, Charset charset) {
        byte[] bytes = readBytes(file);
        if (bytes == null) return null;
        return new String(bytes, charset);
    }

    default @Nullable String readString(String file) {
        return readString(file, StandardCharsets.UTF_8);
    }

    default @Nullable MetaSchematic readSchematic(String file) {
        try (InputStream is = throwIfNull(read(file))) {
            return MetaSchematic.read(is);
        } catch (IOException e) {
            return null;
        }
    }

    default @NotNull OutputStream write(String file) throws UnsupportedOperationException, IOException {
        throw new UnsupportedOperationException("This AssetSource is read-only");
    }

    default void writeBytes(String file, byte[] bytes) throws UnsupportedOperationException, IOException {
        try (OutputStream os = throwIfNull(write(file))) {
            os.write(bytes);
            os.flush();
        }
    }

    default void writeString(String file, String string, Charset charset) throws UnsupportedOperationException, IOException {
        writeBytes(file, string.getBytes(charset));
    }

    default void writeString(String file, String string) throws UnsupportedOperationException, IOException {
        writeBytes(file, string.getBytes(StandardCharsets.UTF_8));
    }

    default void writeSchematic(String file, MetaSchematic schematic) throws UnsupportedOperationException, IOException {
        try (OutputStream os = throwIfNull(write(file))) {
            schematic.serialize(os);
            os.flush();
        }
    }

    Set<String> list(String directory, EnumSet<AssetType> types, boolean deep);

    default Set<String> list(String directory) {
        return list(directory, AssetType.ALL, true);
    }

    default boolean has(String file) {
        InputStream is = read(file);
        if (is == null) return false;
        try {
            is.close();
        } catch (Exception ignored) { }
        return true;
    }

}
