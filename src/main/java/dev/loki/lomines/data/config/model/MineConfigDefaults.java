package dev.loki.lomines.data.config.model;

import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.data.config.reset.ResetConfig;
import dev.loki.lomines.data.config.reward.RewardConfig;
import dev.loki.lomines.data.config.spawn.PlayerSpawnConfig;
import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.data.config.ui.UIConfig;
import dev.loki.lomines.integration.worldguard.config.WorldGuardConfig;

public final class MineConfigDefaults {

    private MineConfigDefaults() {
    }

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

        public Builder(String name) {
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
