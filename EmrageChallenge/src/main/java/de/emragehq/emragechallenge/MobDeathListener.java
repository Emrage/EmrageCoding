package de.emragehq.emragechallenge;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

public class MobDeathListener implements Listener {
    private final Plugin plugin;

    public MobDeathListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().getScoreboardTags().contains("ChallengeMob")) {
            World world = event.getEntity().getWorld();
            world.getWorldBorder().setSize(60000);
        }
    }
}