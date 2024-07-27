package de.emragehq.emragechallenge;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class PlayerMoveListener implements Listener {
    private final Plugin plugin;
    private final Logger logger;

    public PlayerMoveListener(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!((Main) plugin).isChallengeStarted()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR || !player.equals(((Main) plugin).getChallengePlayer())) {
            return;
        }

        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();

        if (fromChunk.equals(toChunk)) {
            return;
        }

        if (((Main) plugin).isDebugMessages()) {
            logger.info(((Main) plugin).getMessage("player-moved")
                .replace("{player}", player.getName())
                .replace("{fromChunk}", fromChunk.toString())
                .replace("{toChunk}", toChunk.toString()));
        }

        Location chunkCenter = new Location(player.getWorld(), toChunk.getX() * 16 + 8, 0, toChunk.getZ() * 16 + 8);
        setWorldBorder(player.getWorld(), chunkCenter);

        EntityType randomMob = getRandomMob();
        if (randomMob == null) {
            return;
        }

        Location spawnLocation = getSpawnLocation(chunkCenter);
        if (spawnLocation == null) {
            return;
        }

        spawnMob(player, spawnLocation, randomMob);
    }

    private void setWorldBorder(World world, Location center) {
        world.getWorldBorder().setCenter(center);
        world.getWorldBorder().setSize(16);
    }

    private EntityType getRandomMob() {
        List<String> excludedEntities = ((Main) plugin).getExcludedEntities();
        EntityType[] mobs = Stream.of(EntityType.values())
                .filter(EntityType::isAlive)
                .filter(EntityType::isSpawnable)
                .filter(type -> !excludedEntities.contains(type.name()))
                .toArray(EntityType[]::new);

        if (mobs.length == 0) {
            logger.warning(((Main) plugin).getMessage("no-valid-mobs"));
            return null;
        }

        return mobs[(int) (Math.random() * mobs.length)];
    }

    private Location getSpawnLocation(Location chunkCenter) {
        Location highestBlock = chunkCenter.getWorld().getHighestBlockAt(chunkCenter).getLocation();
        if (highestBlock == null) {
            logger.warning(((Main) plugin).getMessage("no-highest-block")
                .replace("{location}", chunkCenter.toString()));
            return null;
        }

        return highestBlock.add(0, 2, 0);
    }

    private void spawnMob(Player player, Location spawnLocation, EntityType randomMob) {
        World world = player.getWorld();
        final LivingEntity mob;

        try {
            mob = (LivingEntity) world.spawnEntity(spawnLocation, randomMob);
            mob.setGlowing(true);
            player.sendMessage(((Main) plugin).getMessage("mob-spawned")
                .replace("{mob}", randomMob.name()));
            playSound(world, spawnLocation, ((Main) plugin).getSpawnSound());
            logDebug(((Main) plugin).getMessage("mob-spawned")
                .replace("{mob}", randomMob.name())
                .replace("{location}", spawnLocation.toString()));
        } catch (Exception e) {
            logger.severe("Failed to spawn entity: " + e.getMessage());
            return;
        }

        applyTemporaryInvulnerability(mob, randomMob);
        removeGlowingEffectLater(mob, randomMob);
        registerMobDeathListener(mob);
    }

    private void playSound(World world, Location location, String soundName) {
        if (((Main) plugin).isSoundsEnabled()) {
            world.playSound(location, Sound.valueOf(soundName), 1.0f, 1.0f);
        }
    }

    private void logDebug(String message) {
        if (((Main) plugin).isDebugMessages()) {
            logger.info(message);
        }
    }

    private void applyTemporaryInvulnerability(LivingEntity mob, EntityType randomMob) {
        mob.setInvulnerable(true);
        logDebug(((Main) plugin).getMessage("invulnerable-set")
            .replace("{mob}", randomMob.name()));

        new BukkitRunnable() {
            @Override
            public void run() {
                mob.setInvulnerable(false);
                logDebug(((Main) plugin).getMessage("invulnerable-removed")
                    .replace("{mob}", randomMob.name()));
            }
        }.runTaskLater(plugin, 20L);
    }

    private void removeGlowingEffectLater(LivingEntity mob, EntityType randomMob) {
        new BukkitRunnable() {
            @Override
            public void run() {
                mob.setGlowing(false);
                playSound(mob.getWorld(), mob.getLocation(), ((Main) plugin).getGlowRemoveSound());
                logDebug(((Main) plugin).getMessage("glowing-removed")
                    .replace("{mob}", randomMob.name()));
            }
        }.runTaskLater(plugin, ((Main) plugin).getGlowingEffectDuration());
    }

    private void registerMobDeathListener(LivingEntity mob) {
        mob.setRemoveWhenFarAway(false);
        mob.setPersistent(true);
        mob.addScoreboardTag("ChallengeMob");
        plugin.getServer().getPluginManager().registerEvents(new MobDeathListener(plugin), plugin);
    }
}