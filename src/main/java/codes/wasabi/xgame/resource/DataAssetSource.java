package codes.wasabi.xgame.resource;

import codes.wasabi.xgame.XGame;
import codes.wasabi.xgame.util.MetaSchematic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class DataAssetSource implements AssetSource {

    private final File dataFolder;
    public DataAssetSource() {
        dataFolder = XGame.getInstance().getDataFolder();
    }

    @Override
    public @Nullable InputStream read(String file) {
        if (!dataFolder.exists()) {
            return null;
        }
        File f = new File(dataFolder, file);
        if (!f.exists()) {
            return null;
        }
        try {
            return Files.newInputStream(f.toPath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public @NotNull OutputStream write(String file) throws IOException {
        File f = new File(dataFolder, file);
        File parent = f.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) throw new IOException("Failed to create new directory \"" + parent.getAbsolutePath() + "\"");
        }
        if (!f.exists()) {
            if (!f.createNewFile()) throw new IOException("Failed to create new empty file \"" + f.getAbsolutePath() + "\"");
        }
        return Files.newOutputStream(f.toPath());
    }

    @Override
    public void writeBytes(String file, byte[] bytes) throws IOException {
        AssetSource.super.writeBytes(file, bytes);
    }

    @Override
    public void writeString(String file, String string, Charset charset) throws IOException {
        AssetSource.super.writeString(file, string, charset);
    }

    @Override
    public void writeString(String file, String string) throws IOException {
        AssetSource.super.writeString(file, string);
    }

    @Override
    public void writeSchematic(String file, MetaSchematic schematic) throws IOException {
        AssetSource.super.writeSchematic(file, schematic);
    }

    @Override
    public Set<String> list(String directory, EnumSet<AssetType> types, boolean deep) {
        directory = directory.replaceAll("/+$", "");
        if (directory.equals(".")) directory = "";
        boolean includeDirectories = types.contains(AssetType.DIRECTORY);
        boolean includeFiles = types.contains(AssetType.FILE);
        Set<String> ret = new HashSet<>();
        if (dataFolder.exists()) {
            File dir = dataFolder;
            if (directory.length() > 0) {
                dir = new File(dataFolder, directory);
            }
            if (dir.isDirectory()) recursiveSearch(dir, includeDirectories, includeFiles, deep, ret, "");
        }
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public boolean has(String file) {
        File f = new File(dataFolder, file);
        return f.exists();
    }

    private static void recursiveSearch(@NotNull File parent, boolean includeDirectories, boolean includeFiles, boolean deep, Set<String> set, String prefix) {
        String[] list = parent.list();
        if (list == null) return;
        for (String name : list) {
            File subject = new File(parent, name);
            if (subject.isDirectory()) {
                if (includeDirectories) {
                    set.add(prefix + subject.getName());
                }
                if (deep) {
                    recursiveSearch(subject, includeDirectories, includeFiles, true, set, prefix + subject.getName() + "/");
                }
            } else if (subject.isFile()) {
                if (includeFiles) {
                    set.add(prefix + subject.getName());
                }
            }
        }
    }

}
