package dev.loki.lomines.data.config.model;

import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.data.config.reset.ResetConfig;
import dev.loki.lomines.data.config.reward.RewardConfig;
import dev.loki.lomines.data.config.spawn.PlayerSpawnConfig;
import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.data.config.ui.UIConfig;
import dev.loki.lomines.integration.worldguard.config.WorldGuardConfig;

import org.bukkit.Location;

import java.util.Objects;
import java.util.Optional;

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
        if (playerSpawn == null) {
            playerSpawn = PlayerSpawnConfig.disabled();
        }
    }

    public static MineConfigDefaults.Builder builder(String name) {
        return new MineConfigDefaults.Builder(name);
    }

    public int volume() {
        return region.totalVolume();
    }

    public String worldName() {
        return region.worldName();
    }

    public Optional<Location> getSpawnForStuckPlayer() {
        return playerSpawn.enabled()
                ? playerSpawn.getLocation()
                : teleport.getLocation();
    }
}
