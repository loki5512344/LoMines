package dev.loki.lomines.core.mine;

import dev.loki.lomines.LoMinesPlugin;
import dev.lolib.scheduler.ScheduledTask;
import dev.lolib.scheduler.Scheduler;

/**
 * Centralized ticker for all mines.
 * Uses a single scheduled task to tick all mines every game tick.
 * This is more efficient than having each mine run its own ticker.
 */
public final class MineTicker {

    private final Mines mines;
    private final LoMinesPlugin plugin;
    private ScheduledTask tickerTask;

    public MineTicker(Mines mines, LoMinesPlugin plugin) {
        this.mines = mines;
        this.plugin = plugin;
    }

    /**
     * Starts the centralized ticker.
     * Ticks all mines every game tick (1L, 1L).
     */
    public void start() {
        if (tickerTask != null) {
            return; // Already running
        }

        tickerTask = Scheduler.get(plugin).runTimer(() -> {
            for (Mine mine : mines.getAll()) {
                tickMine(mine);
            }
        }, 1L, 1L);
    }

    /**
     * Stops the centralized ticker.
     */
    public void stop() {
        if (tickerTask != null) {
            tickerTask.cancel();
            tickerTask = null;
        }
    }

    /**
     * Ticks a single mine, checking if it needs to reset.
     */
    private void tickMine(Mine mine) {
        int currentTicks = mine.getTicksAtomic().incrementAndGet();

        if (currentTicks >= mine.getConfig().reset().intervalTicks()) {
            mine.getTicksAtomic().set(0);
            mine.reset(false);
        }
    }
}
