package dev.loki.lomines.core.service;

import dev.loki.lomines.core.service.write.MineConfigWriter;
import dev.loki.lomines.data.config.ConfigLoader;
import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import org.bukkit.Location;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MineFileManager {

    private final Path minesFolder;
    private final ConfigLoader configLoader;
    private final MineConfigWriter configWriter;

    public MineFileManager(Path minesFolder) {
        this.minesFolder = minesFolder;
        this.configLoader = new ConfigLoader(minesFolder.getParent());
        this.configWriter = new MineConfigWriter(minesFolder, configLoader);
    }

    public Path getMinesFolder() {
        return minesFolder;
    }

    public void ensureFolderExists() throws IOException {
        if (!Files.exists(minesFolder)) {
            Files.createDirectories(minesFolder);
        }
    }

    public void createDefaultConfig(String name, Location corner1, Location corner2) throws IOException {
        ensureFolderExists();
        configWriter.createDefaultConfig(name, corner1, corner2);
    }

    public MineConfig loadConfig(String name) throws IOException, ConfigLoader.ConfigLoadException {
        return configLoader.load(name);
    }

    public void deleteConfig(String name) throws IOException {
        Path configFile = minesFolder.resolve(name + ".yml");
        if (Files.exists(configFile)) {
            Files.delete(configFile);
        }
    }

    public void saveConfig(MineConfig config) throws ConfigLoader.ConfigLoadException {
        configLoader.save(config);
    }

    public void saveConfig(String name, MineConfig config) throws ConfigLoader.ConfigLoadException {
        configLoader.save(config);
    }

    public void saveMaskPositions(String name, BlockKey markerMaterial,
                                   java.util.List<Location> positions) throws IOException, ConfigLoader.ConfigLoadException {
        configWriter.saveMaskPositions(name, markerMaterial, positions);
    }
}
