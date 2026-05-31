package dev.loki.lomines.core.common;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.core.service.MineFileManager;
import dev.loki.lomines.data.config.loader.common.ConfigLoader;
import dev.loki.lomines.data.config.MineConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Loads mine configurations from disk.
 */
final class MineLoader {

    private final LoMinesPlugin plugin;
    private final MineFileManager fileManager;
    private final MineRepository repository;

    MineLoader(LoMinesPlugin plugin, MineFileManager fileManager, MineRepository repository) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.repository = repository;
    }

    void loadAll() throws IOException {
        repository.clear();

        Path minesFolder = fileManager.getMinesFolder();
        if (!Files.exists(minesFolder)) {
            return;
        }

        try (Stream<Path> paths = Files.list(minesFolder)) {
            paths.filter(path -> path.toString().endsWith(".yml"))
                    .filter(path -> !path.getFileName().toString().startsWith("_"))
                    .forEach(this::loadMineFromFile);
        }
    }

    private void loadMineFromFile(Path configFile) {
        try {
            String fileName = configFile.getFileName().toString();
            String mineName = fileName.substring(0, fileName.length() - 4);

            MineConfig config = fileManager.loadConfig(mineName);
            repository.createAndStart(mineName, config);

            plugin.loLogger().info("Loaded mine: " + mineName);
        } catch (IOException | ConfigLoader.ConfigLoadException e) {
            plugin.loLogger().error("Failed to load mine from " + configFile.getFileName() + ": " + e.getMessage());
        }
    }
}
