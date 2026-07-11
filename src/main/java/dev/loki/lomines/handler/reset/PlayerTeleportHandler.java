package dev.loki.lomines.handler.reset;

import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.util.block.SafeTeleportFinder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class PlayerTeleportHandler {

    private final Mine mine;

    public PlayerTeleportHandler(Mine mine) {
        this.mine = mine;
    }

    public void teleportStuckPlayers() {
        var destOpt = mine.getConfig().getSpawnForStuckPlayer();
        if (destOpt.isEmpty()) {
            return;
        }
        Location dest = destOpt.get();
        if (dest.getWorld() == null) {
            return;
        }

        Location safeDest = SafeTeleportFinder.findSafeTeleportLocation(dest, 3);

        for (Player p : dest.getWorld().getPlayers()) {
            if (mine.contains(p.getLocation()) && isPlayerStuckInBlock(p)) {
                p.teleport(safeDest);
            }
        }
    }

    private boolean isPlayerStuckInBlock(Player player) {
        Location loc = player.getLocation();
        var block = loc.getBlock();
        return block.getType().isSolid() && !block.isLiquid();
    }
}
