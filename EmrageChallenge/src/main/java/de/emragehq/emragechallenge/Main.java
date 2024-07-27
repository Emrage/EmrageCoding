package de.emragehq.emragechallenge;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Main extends JavaPlugin {
    private boolean challengeStarted = false;
    private Player challengePlayer = null;
    private List<String> excludedEntities;
    private int glowingEffectDuration;
    private boolean soundsEnabled;
    private String spawnSound;
    private String glowRemoveSound;
    private boolean debugMessages;
    private FileConfiguration messages;

@Override
public void onEnable() {
    saveDefaultConfig();
    loadConfigValues();
    this.getCommand("start").setExecutor(new StartCommandExecutor(this));
    getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
    getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
}

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean isChallengeStarted() {
        return challengeStarted;
    }

    public void setChallengeStarted(boolean challengeStarted, Player player) {
        this.challengeStarted = challengeStarted;
        this.challengePlayer = player;
        if (challengeStarted) {
            for (Player p : getServer().getOnlinePlayers()) {
                if (!p.equals(player)) {
                    p.setGameMode(GameMode.SPECTATOR);
                }
            }
        } else {
            this.challengePlayer = null;
        }
    }

    public Player getChallengePlayer() {
        return challengePlayer;
    }

    public List<String> getExcludedEntities() {
        return excludedEntities;
    }

    public int getGlowingEffectDuration() {
        return glowingEffectDuration;
    }

    public boolean isSoundsEnabled() {
        return soundsEnabled;
    }

    public String getSpawnSound() {
        return spawnSound;
    }

    public String getGlowRemoveSound() {
        return glowRemoveSound;
    }

    public boolean isDebugMessages() {
        return debugMessages;
    }

    public String getMessage(String key) {
        return messages.getString("messages." + key);
    }

    private void loadConfigValues() {
        FileConfiguration config = getConfig();
        excludedEntities = config.getStringList("excluded-entities");
        glowingEffectDuration = config.getInt("glowing-effect-duration");
        soundsEnabled = config.getBoolean("sounds.enabled");
        spawnSound = config.getString("sounds.spawn");
        glowRemoveSound = config.getString("sounds.glow-remove");
        debugMessages = config.getBoolean("debug-messages");
        messages = config;
    }
}