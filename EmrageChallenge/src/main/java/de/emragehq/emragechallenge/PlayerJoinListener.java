package de.emragehq.emragechallenge;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class PlayerJoinListener implements Listener {
    private final Plugin plugin;

    public PlayerJoinListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (((Main) plugin).isChallengeStarted() && !event.getPlayer().equals(((Main) plugin).getChallengePlayer())) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }
}