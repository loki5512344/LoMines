package dev.loki.lomines.command.admin.manage;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.core.mine.registry.Mines;
import dev.loki.lomines.gui.mine.main.MineEditGui;
import dev.loki.lomines.util.ErrorHandler;
import dev.loki.lomines.util.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public final class MineActionHandler {

    private final LoMinesPlugin plugin;
    private final Mines mines;
    private final ErrorHandler errorHandler;

    public MineActionHandler(LoMinesPlugin plugin, Mines mines, ErrorHandler errorHandler) {
        this.plugin = plugin;
        this.mines = mines;
        this.errorHandler = errorHandler;
    }

    public void create(CommandSender sender, String name) {
        try {
            mines.create(name);
            sender.sendMessage(MessageFormatter.success("Successfully created mine: " + name));
            sender.sendMessage(Component.text(
                    "Edit selection & contents in plugins/LoMines/mines/" + name + ".yml — "
                            + "for shape-based ore (not full cuboid), paint mask.marker blocks, then: /lm maskscan " + name));
            if (sender instanceof Player player) {
                sender.sendMessage(Component.text(
                        "Or use: /lm edit " + name + " to open GUI editor", NamedTextColor.YELLOW));
            }
        } catch (IllegalArgumentException e) {
            errorHandler.handleNotFound(sender, "Mine already exists", name);
        } catch (IOException e) {
            errorHandler.handleError(sender, "Failed to create mine: " + e.getMessage(),
                    "Failed to create mine " + name, e);
        }
    }

    public void delete(CommandSender sender, String name) {
        try {
            mines.delete(name);
            sender.sendMessage(MessageFormatter.success("Successfully deleted mine: " + name));
        } catch (IllegalArgumentException e) {
            errorHandler.handleNotFound(sender, "Mine", name);
        } catch (IOException e) {
            errorHandler.handleError(sender, "Failed to delete mine: " + e.getMessage(),
                    "Failed to delete mine " + name, e);
        }
    }

    public void edit(CommandSender sender, String mineName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return;
        }

        Mine mine = mines.find(mineName).orElse(null);
        if (mine == null) {
            errorHandler.handleNotFound(sender, "Mine", mineName);
            return;
        }

        MineEditGui.open(plugin, player, mineName);
    }

    public void reset(CommandSender sender, String mineName, boolean silent) {
        try {
            Mine mine = mines.get(mineName);
            mine.reset(silent);
            sender.sendMessage(MessageFormatter.success("Resetting mine: " + mineName));
        } catch (IllegalArgumentException e) {
            errorHandler.handleNotFound(sender, "Mine", mineName);
        }
    }

    public void reload(CommandSender sender) {
        try {
            mines.getAll().forEach(Mine::stop);
            mines.loadAll();
            int count = mines.getAll().size();
            sender.sendMessage(MessageFormatter.success("Successfully reloaded " + count + " mine(s)"));
        } catch (IOException e) {
            errorHandler.handleError(sender, "Failed to reload mines: " + e.getMessage(),
                    "Failed to reload mines", e);
        }
    }
}
