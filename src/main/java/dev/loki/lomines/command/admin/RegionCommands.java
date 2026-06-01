package dev.loki.lomines.command.admin;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.region.RegionConfig;
import dev.loki.lomines.util.location.Cuboid;
import dev.lolilb.commands.annotation.Arg;
import dev.lolilb.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Commands for managing mine regions (multiple spawn areas).
 */
public class RegionCommands {

    private final LoMinesPlugin plugin;

    public RegionCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Lists all regions in a mine.
     * Usage: /lm regions <mine>
     */
    @Subcommand(value = "regions", permission = "lomines.admin.regions")
    void listRegions(CommandSender sender, @Arg("mine") String mineName) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            sender.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }

        List<Cuboid> regions = mine.getRegions();
        sender.sendMessage(Component.text("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(Component.text("§6§lРегионы шахты §f" + mine.getName()));
        sender.sendMessage(Component.text("§7Всего регионов: §f" + regions.size()));
        sender.sendMessage(Component.text(""));

        for (int i = 0; i < regions.size(); i++) {
            Cuboid r = regions.get(i);
            sender.sendMessage(Component.text("§e#" + (i + 1) + " §7(§f" + r.getVolume() + " §7блоков)"));
            sender.sendMessage(Component.text("  §7Мир: §f" + r.getWorld().getName()));
            sender.sendMessage(Component.text("  §7От: §f" + r.getMinX() + ", " + r.getMinY() + ", " + r.getMinZ()));
            sender.sendMessage(Component.text("  §7До: §f" + r.getMaxX() + ", " + r.getMaxY() + ", " + r.getMaxZ()));
        }
        sender.sendMessage(Component.text("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    /**
     * Adds a new region to a mine using current wand selection.
     * Usage: /lm addregion <mine>
     */
    @Subcommand(value = "addregion", permission = "lomines.admin.regions")
    void addRegion(CommandSender sender, @Arg("mine") String mineName) {
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
        var session = wandManager.getSession(player);

        if (!session.isComplete()) {
            player.sendMessage(Component.text("§cВыделите регион палочкой! ЛКМ - 1-я точка, ПКМ - 2-я точка"));
            return;
        }

        Cuboid newRegion = session.toCuboid();
        String mineWorld = mine.getConfig().region().worldName();
        String regionWorld = newRegion.getWorld().getName();

        if (!mineWorld.equals(regionWorld)) {
            player.sendMessage(Component.text("§cРегион должен быть в мире §f" + mineWorld + "§c, а не §f" + regionWorld));
            return;
        }

        // Add new region to existing regions
        List<Cuboid> currentRegions = new ArrayList<>(mine.getRegions());
        currentRegions.add(newRegion);

        // Update config
        var newRegionConfig = new RegionConfig(currentRegions);
        updateMineRegions(mine, newRegionConfig);

        player.sendMessage(Component.text("§aДобавлен регион #" + currentRegions.size()));
        player.sendMessage(Component.text("§7От: §f" + newRegion.getMinX() + ", " + newRegion.getMinY() + ", " + newRegion.getMinZ()));
        player.sendMessage(Component.text("§7До: §f" + newRegion.getMaxX() + ", " + newRegion.getMaxY() + ", " + newRegion.getMaxZ()));
        player.sendMessage(Component.text("§7Объём: §f" + newRegion.getVolume() + " §7блоков"));

        session.clear();
    }

    /**
     * Removes a region by index.
     * Usage: /lm removeregion <mine> <index>
     */
    @Subcommand(value = "removeregion", permission = "lomines.admin.regions")
    void removeRegion(CommandSender sender,
                      @Arg("mine") String mineName,
                      @Arg("index") int index) {
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
                newConfig,
                oldConfig.blocks(),
                oldConfig.reset(),
                oldConfig.teleport(),
                oldConfig.playerSpawn(),
                oldConfig.rewards(),
                oldConfig.ui(),
                oldConfig.worldGuard()
        );

        // Update and save
        plugin.getMines().updateMineConfig(mine.getName(), newMineConfig);
        mine.save();
    }
}
