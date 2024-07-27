package de.emragehq.emragechallenge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommandExecutor implements CommandExecutor {
    private final Main plugin;

    public StartCommandExecutor(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (plugin.isChallengeStarted()) {
                player.sendMessage("A challenge is already in progress.");
                return true;
            }
            plugin.setChallengeStarted(true, player);
            player.sendMessage("Challenge started!");
            return true;
        }
        return false;
    }
}