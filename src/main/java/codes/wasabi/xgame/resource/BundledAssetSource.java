package codes.wasabi.xgame.resource;

import codes.wasabi.xgame.XGame;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BundledAssetSource implements AssetSource {

    private final File jarFile;
    public BundledAssetSource() {
        try {
            jarFile = new File(XGame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!jarFile.exists()) throw new FileNotFoundException("File named by CodeSource not found");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public final File getJarFile() {
        return jarFile;
    }

    @Override
    public @Nullable InputStream read(String file) {
        return XGame.getInstance().getResource(file);
    }

    @Override
    public Set<String> list(String directory, EnumSet<AssetType> types, boolean deep) {
        directory = directory.replaceAll("/+$", "");
        if (directory.equals(".")) directory = "";
        Set<String> ret = new HashSet<>();
        boolean includeDirectories = types.contains(AssetType.DIRECTORY);
        boolean includeFiles = types.contains(AssetType.FILE);
        try (FileInputStream fis = new FileInputStream(jarFile)) {
            try (ZipInputStream zis = new ZipInputStream(fis)) {
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    String name = ze.getName();
                    String subName;
                    if (directory.length() < 1) {
                        subName = name;
                    } else if (name.startsWith(directory + "/")) {
                        subName = name.substring(directory.length() + 1);
                    } else {
                        continue;
                    }
                    if ((!deep) && subName.contains("/")) continue;
                    if (ze.isDirectory()) {
                        if (includeDirectories) ret.add(subName);
                    } else if (includeFiles) {
                        ret.add(subName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.unmodifiableSet(ret);
    }

}
