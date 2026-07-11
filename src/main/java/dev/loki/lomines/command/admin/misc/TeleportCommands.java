package dev.loki.lomines.command.admin.misc;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.registry.Mines;
import dev.loki.lomines.util.ErrorHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class TeleportCommands {

    private final TeleportActionHandler actionHandler;

    public TeleportCommands(LoMinesPlugin plugin, Mines mines, ErrorHandler errorHandler) {
        this.actionHandler = new TeleportActionHandler(plugin, mines, errorHandler);
    }

    public void handle(CommandSender sender, String subcommand, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /lm " + subcommand + " <mine>", NamedTextColor.RED));
            return;
        }
        switch (subcommand.toLowerCase()) {
            case "setteleport" -> actionHandler.setTeleport(sender, args[0]);
            case "setspawn" -> actionHandler.setSpawn(sender, args[0]);
            case "clearspawn" -> actionHandler.clearSpawn(sender, args[0]);
            default -> {}
        }
    }
}
