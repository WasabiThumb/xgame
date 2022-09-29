package codes.wasabi.xgame.resource;

import java.util.EnumSet;

public enum AssetType {
    FILE, DIRECTORY;

    public static EnumSet<AssetType> FILES = EnumSet.of(FILE);
    public static EnumSet<AssetType> DIRECTORIES = EnumSet.of(DIRECTORY);
    public static EnumSet<AssetType> ALL = EnumSet.allOf(AssetType.class);

}
