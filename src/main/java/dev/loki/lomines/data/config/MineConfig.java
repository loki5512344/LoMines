package dev.loki.lomines.data.config;

import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.data.config.reset.ResetConfig;
import dev.loki.lomines.data.config.reward.RewardConfig;
import dev.loki.lomines.data.config.spawn.PlayerSpawnConfig;
import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.data.config.ui.UIConfig;
import dev.loki.lomines.integration.worldguard.WorldGuardConfig;

import org.bukkit.Location;

import java.util.Objects;
import java.util.Optional;

/**
 * Clean, type-safe, section-based mine configuration.
 * Proper separation of concerns: each section handles one aspect.
 */
public record MineConfig(
        String name,
        RegionConfig region,
        BlockConfig blocks,
        ResetConfig reset,
        RewardConfig rewards,
        TeleportConfig teleport,
        UIConfig ui,
        WorldGuardConfig worldGuard,
        PlayerSpawnConfig playerSpawn
) {

    public MineConfig {
        Objects.requireNonNull(name, "Mine name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Mine name cannot be blank");
        }
        name = name.toLowerCase().trim();

        Objects.requireNonNull(region, "Region config cannot be null");
        Objects.requireNonNull(blocks, "Block config cannot be null");
        Objects.requireNonNull(reset, "Reset config cannot be null");
        Objects.requireNonNull(rewards, "Reward config cannot be null");
        Objects.requireNonNull(teleport, "Teleport config cannot be null");
        Objects.requireNonNull(ui, "UI config cannot be null");
        // worldGuard and playerSpawn can be null (disabled by default)
        if (playerSpawn == null) {
            playerSpawn = PlayerSpawnConfig.disabled();
        }
    }

    /**
     * Total volume of the mine (for progress calculation).
     */
    public int volume() {
        return region.totalVolume();
    }

    /**
     * World name where the mine is located.
     */
    public String worldName() {
        return region.worldName();
    }

    /**
     * Creates builder for fluent construction.
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Creates config with sensible defaults.
     */
    public static MineConfig defaults(String name, RegionConfig region, BlockConfig blocks) {
        return new MineConfig(
                name,
                region,
                blocks,
                ResetConfig.defaults(),
                RewardConfig.empty(),
                TeleportConfig.disabled(),
                UIConfig.defaults(),
                WorldGuardConfig.disabled(),
                PlayerSpawnConfig.disabled()
        );
    }

    /**
     * Returns the spawn location for stuck players.
     * If playerSpawn is not set, falls back to teleport location.
     */
    public Optional<Location> getSpawnForStuckPlayer() {
        return playerSpawn.enabled()
                ? playerSpawn.getLocation()
                : teleport.getLocation();
    }

    // --- Builder ---

    public static class Builder {
        private final String name;
        private RegionConfig region;
        private BlockConfig blocks;
        private ResetConfig reset = ResetConfig.defaults();
        private RewardConfig rewards = RewardConfig.empty();
        private TeleportConfig teleport = TeleportConfig.disabled();
        private UIConfig ui = UIConfig.defaults();
        private WorldGuardConfig worldGuard = WorldGuardConfig.disabled();
        private PlayerSpawnConfig playerSpawn = PlayerSpawnConfig.disabled();

        private Builder(String name) {
            this.name = name;
        }

        public Builder region(RegionConfig region) {
            this.region = region;
            return this;
        }

        public Builder blocks(BlockConfig blocks) {
            this.blocks = blocks;
            return this;
        }

        public Builder reset(ResetConfig reset) {
            this.reset = reset;
            return this;
        }

        public Builder rewards(RewardConfig rewards) {
            this.rewards = rewards;
            return this;
        }

        public Builder teleport(TeleportConfig teleport) {
            this.teleport = teleport;
            return this;
        }

        public Builder ui(UIConfig ui) {
            this.ui = ui;
            return this;
        }

        public Builder worldGuard(WorldGuardConfig worldGuard) {
            this.worldGuard = worldGuard;
            return this;
        }

        public Builder playerSpawn(PlayerSpawnConfig playerSpawn) {
            this.playerSpawn = playerSpawn;
            return this;
        }

        public MineConfig build() {
            return new MineConfig(name, region, blocks, reset, rewards, teleport, ui, worldGuard, playerSpawn);
        }
    }
}
