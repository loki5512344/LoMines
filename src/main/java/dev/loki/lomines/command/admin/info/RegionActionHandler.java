package dev.loki.lomines.command.admin.info;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.util.location.geo.Cuboid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class RegionActionHandler {

    private final LoMinesPlugin plugin;

    public RegionActionHandler(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    public void addRegion(CommandSender sender, String mineName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Эта команда только для игроков!", NamedTextColor.RED));
            return;
        }

        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }

        var wandManager = plugin.getGroupWandManager();
        var session = wandManager.getSession(player.getUniqueId());
        int slot = session.getActiveSlot();
        var pos1 = session.getPos1(slot);
        var pos2 = session.getPos2(slot);
        if (pos1 == null || pos2 == null) {
            player.sendMessage(Component.text("§cВыделите регион палочкой! ЛКМ - 1-я точка, ПКМ - 2-я точка"));
            return;
        }
        Cuboid newRegion = new Cuboid(pos1, pos2);
        String mineWorld = mine.getConfig().region().worldName();
        String regionWorld = newRegion.getWorld().getName();

        if (!mineWorld.equals(regionWorld)) {
            player.sendMessage(Component.text("§cРегион должен быть в мире §f" + mineWorld + "§c, а не §f" + regionWorld));
            return;
        }

        List<Cuboid> currentRegions = new ArrayList<>(mine.getRegions());
        currentRegions.add(newRegion);

        var newRegionConfig = new RegionConfig(currentRegions);
        updateMineRegions(mine, newRegionConfig);

        player.sendMessage(Component.text("§aДобавлен регион #" + currentRegions.size()));
        player.sendMessage(Component.text("§7От: §f" + newRegion.getMinX() + ", " + newRegion.getMinY() + ", " + newRegion.getMinZ()));
        player.sendMessage(Component.text("§7До: §f" + newRegion.getMaxX() + ", " + newRegion.getMaxY() + ", " + newRegion.getMaxZ()));
        player.sendMessage(Component.text("§7Объём: §f" + newRegion.getVolume() + " §7блоков"));

        session.clearCorners(slot);
    }

    public void removeRegion(CommandSender sender, String mineName, int index) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            sender.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }

        List<Cuboid> currentRegions = new ArrayList<>(mine.getRegions());

        if (currentRegions.size() <= 1) {
            sender.sendMessage(Component.text("§cНельзя удалить последний регион! Удалите всю шахту."));
            return;
        }

        if (index < 1 || index > currentRegions.size()) {
            sender.sendMessage(Component.text("§cНеверный номер региона. Доступны: 1-" + currentRegions.size()));
            return;
        }

        Cuboid removed = currentRegions.remove(index - 1);
        var newRegionConfig = new RegionConfig(currentRegions);
        updateMineRegions(mine, newRegionConfig);

        sender.sendMessage(Component.text("§aУдалён регион #" + index));
        sender.sendMessage(Component.text("§7Был: §f" + removed.getMinX() + ", " + removed.getMinY() + ", " + removed.getMinZ() +
                " §7-> §f" + removed.getMaxX() + ", " + removed.getMaxY() + ", " + removed.getMaxZ()));
        sender.sendMessage(Component.text("§7Осталось регионов: §f" + currentRegions.size()));
    }

    private void updateMineRegions(Mine mine, RegionConfig newConfig) {
        var oldConfig = mine.getConfig();
        var newMineConfig = new MineConfig(
                oldConfig.name(),
                newConfig,
                oldConfig.blocks(),
                oldConfig.reset(),
                oldConfig.rewards(),
                oldConfig.teleport(),
                oldConfig.ui(),
                oldConfig.worldGuard(),
                oldConfig.playerSpawn()
        );

        try {
            plugin.getMines().updateMineConfig(mine.getName(), newMineConfig);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update mine regions", e);
        }
    }
}
