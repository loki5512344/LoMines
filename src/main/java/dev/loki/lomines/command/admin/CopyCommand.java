package dev.loki.lomines.command.admin;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.core.mine.MineConfig;
import dev.lolilb.commands.annotation.Arg;
import dev.lolilb.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/**
 * Command to copy mine configuration from one mine to another.
 */
public class CopyCommand {

    private final LoMinesPlugin plugin;

    public CopyCommand(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Copies mine configuration (blocks, reset settings, rewards, etc.)
     * from source mine to target mine. Regions and positions are NOT copied.
     * Usage: /lm copy <from> <to>
     */
    @Subcommand(value = "copy", permission = "lomines.admin.copy")
    void copy(CommandSender sender,
              @Arg("from-mine") String fromMineName,
              @Arg("to-mine") String toMineName) {

        Mine fromMine = plugin.getMines().find(fromMineName).orElse(null);
        if (fromMine == null) {
            sender.sendMessage(Component.text("Исходная шахта не найдена: " + fromMineName, NamedTextColor.RED));
            return;
        }

        Mine toMine = plugin.getMines().find(toMineName).orElse(null);
        if (toMine == null) {
            sender.sendMessage(Component.text("Целевая шахта не найдена: " + toMineName, NamedTextColor.RED));
            return;
        }

        if (fromMine.getName().equalsIgnoreCase(toMine.getName())) {
            sender.sendMessage(Component.text("Нельзя копировать шахту саму в себя!", NamedTextColor.RED));
            return;
        }

        var sourceConfig = fromMine.getConfig();
        var targetConfig = toMine.getConfig();

        // Copy blocks configuration
        targetConfig.blocks().weights().clear();
        targetConfig.blocks().weights().putAll(sourceConfig.blocks().weights());

        // Copy reset settings
        var targetReset = targetConfig.reset();
        var sourceReset = sourceConfig.reset();
        targetReset.intervalSeconds(sourceReset.intervalSeconds());
        targetReset.percentTrigger(sourceReset.percentTrigger());
        targetReset.percentTriggerEnabled(sourceReset.isPercentTriggerEnabled());
        targetReset.intervalDisplay(sourceReset.intervalDisplay());

        // Copy teleport and spawn settings
        var targetTeleport = targetConfig.teleport();
        var sourceTeleport = sourceConfig.teleport();
        targetTeleport.enabled(sourceTeleport.enabled());
        if (sourceTeleport.getLocation().isPresent()) {
            targetTeleport.setLocation(sourceTeleport.getLocation().get());
        } else {
            targetTeleport.setLocation(null);
        }

        var targetSpawn = targetConfig.playerSpawn();
        var sourceSpawn = sourceConfig.playerSpawn();
        targetSpawn.enabled(sourceSpawn.enabled());
        if (sourceSpawn.getLocation().isPresent()) {
            targetSpawn.setLocation(sourceSpawn.getLocation().get());
        } else {
            targetSpawn.setLocation(null);
        }

        // Copy rewards
        targetConfig.rewards().entries().clear();
        targetConfig.rewards().entries().addAll(sourceConfig.rewards().entries());

        // Copy other settings
        targetConfig.blockPhysics(sourceConfig.isBlockPhysicsEnabled());
        targetConfig.entitySpawning(sourceConfig.isEntitySpawningEnabled());
        targetConfig.liquidFlow(sourceConfig.isLiquidFlowEnabled());

        // Save target mine
        toMine.save();

        sender.sendMessage(Component.text("§aКонфигурация скопирована из §f" + fromMine.getName() +
                " §aв §f" + toMine.getName()));
        sender.sendMessage(Component.text("§7Скопировано:"));
        sender.sendMessage(Component.text("  §7• Блоки: §f" + sourceConfig.blocks().weights().size() + " типов"));
        sender.sendMessage(Component.text("  §7• Настройки сброса"));
        sender.sendMessage(Component.text("  §7• Точки телепорта/спавна"));
        sender.sendMessage(Component.text("  §7• Награды: §f" + sourceConfig.rewards().entries().size() + " записей"));
        sender.sendMessage(Component.text("§7§oПримечание: регионы и позиции не скопированы"));
    }
}
