package codes.wasabi.xgame.minigame;

import codes.wasabi.xgame.util.SoundUtil;
import codes.wasabi.xplug.XPlug;
import codes.wasabi.xplug.lib.adventure.audience.Audience;
import codes.wasabi.xplug.lib.adventure.text.Component;
import codes.wasabi.xplug.lib.adventure.text.format.NamedTextColor;
import codes.wasabi.xplug.lib.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public interface PartyListener {

    void addPlayer(Player ply);

    void removePlayer(Player ply);

    void changeLeader(Player ply);

    void changeStage(MinigameStage stage);

    void changeReadyState(Player ply, boolean ready);

    void changePrivacy(boolean isPrivate);

    void notEnoughPlayers();

    class Member implements PartyListener {

        private final org.bukkit.entity.Player player;
        private final Audience audience;
        public Member(org.bukkit.entity.Player player) {
            this.player = player;
            this.audience = XPlug.getAdventure().player(player);
        }

        public org.bukkit.entity.Player getPlayer() {
            return this.player;
        }

        private Component getDisplayName(org.bukkit.entity.Player ply) {
            return LegacyComponentSerializer.legacySection().deserialize(ply.getDisplayName()).colorIfAbsent(NamedTextColor.GRAY);
        }

        @Override
        public void addPlayer(org.bukkit.entity.Player ply) {
            audience.sendMessage(Component.empty()
                    .append(Component.text("* ").color(NamedTextColor.GREEN))
                    .append(getDisplayName(ply))
                    .append(Component.text(" joined the lobby").color(NamedTextColor.GREEN))
            );
        }

        @Override
        public void removePlayer(org.bukkit.entity.Player ply) {
            audience.sendMessage(Component.empty()
                    .append(Component.text("* ").color(NamedTextColor.RED))
                    .append(getDisplayName(ply))
                    .append(Component.text(" left the lobby").color(NamedTextColor.RED))
            );
        }

        @Override
        public void changeLeader(org.bukkit.entity.Player ply) {
            audience.sendMessage(Component.empty()
                    .append(Component.text("* ").color(NamedTextColor.GRAY))
                    .append(getDisplayName(ply))
                    .append(Component.text(" is now the lobby leader").color(NamedTextColor.GRAY))
            );
        }

        @Override
        public void changeStage(MinigameStage stage) {
            switch (stage) {
                case REGION:
                    audience.sendMessage(Component.text("* Preparing chunks...").color(NamedTextColor.YELLOW));
                    break;
                case READY:
                    audience.sendMessage(Component.text("* Starting game...").color(NamedTextColor.YELLOW));
                    break;
                case ACTIVE:
                    audience.sendMessage(Component.text("* Game started").color(NamedTextColor.GREEN));
                    break;
            }
        }

        @Override
        public void changeReadyState(org.bukkit.entity.Player ply, boolean ready) {
            audience.sendMessage(Component.empty()
                    .append(Component.text("* ").color(NamedTextColor.GRAY))
                    .append(getDisplayName(ply))
                    .append(Component.text(" is ").color(NamedTextColor.GRAY))
                    .append(ready ? Component.text("READY").color(NamedTextColor.GREEN) : Component.text("NOT READY").color(NamedTextColor.RED))
            );
        }

        @Override
        public void changePrivacy(boolean isPrivate) {
            SoundUtil.clickSound(player);
            audience.sendMessage(Component.empty()
                    .append(Component.text("* Lobby set to ").color(NamedTextColor.GRAY))
                    .append(
                            isPrivate ?
                                    Component.text("PRIVATE").color(NamedTextColor.RED)
                                    : Component.text("PUBLIC").color(NamedTextColor.GREEN)
                    )
            );
        }

        @Override
        public void notEnoughPlayers() {
            SoundUtil.clickSound(player);
            audience.sendMessage(Component.text("* Not enough players to start game!").color(NamedTextColor.RED));
        }

    }

}
