package codes.wasabi.xgame.minigame;

import codes.wasabi.xgame.resource.AssetSource;
import codes.wasabi.xplug.lib.matlib.struct.MetaMaterial;

import java.util.Objects;

public class LuaMinigame implements Minigame {

    private final String projectFile;
    private String code;
    private MinigameInstance instance;
    public LuaMinigame(String projectFile) {
        this.projectFile = projectFile;
        reload();
    }

    public void reload() {
        String c = AssetSource.COPY.readString(projectFile);
        code = (c == null ? "" : c);
        pushInstance();
    }

    private void pushInstance() {
        instance = new LuaMinigameInstance(code);
    }

    public final String getProjectFile() {
        return projectFile;
    }

    @Override
    public String getName() {
        return instance.getName();
    }

    @Override
    public String getDescription() {
        return instance.getDescription();
    }

    @Override
    public MetaMaterial getIcon() {
        return instance.getIcon();
    }

    @Override
    public int getMinPlayers() {
        return instance.getMinPlayers();
    }

    @Override
    public int getMaxPlayers() {
        return instance.getMaxPlayers();
    }

    @Override
    public MinigameInstance newInstance() {
        MinigameInstance ret = instance;
        pushInstance();
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof LuaMinigame) {
            LuaMinigame other = (LuaMinigame) obj;
            if (other.projectFile.equalsIgnoreCase(this.projectFile)) return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.projectFile);
    }

}
