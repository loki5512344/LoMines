package dev.loki.lomines.core.service;

import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.ConfigLoader;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.util.ValidationUtils;
import dev.loki.lomines.util.location.Cuboid;
import dev.loki.lomines.util.selection.MaskScanner;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.IOException;
import java.util.List;

/**
 * Service for scanning and saving mask positions.
 * Updated for new configuration system (v2).
 */
public final class MaskScanService {

    private final MineRepository repository;
    private final MineFileManager fileManager;

    public MaskScanService(MineRepository repository, MineFileManager fileManager) {
        this.repository = repository;
        this.fileManager = fileManager;
    }

    public int scanAndSave(String mineName) throws IOException, ConfigLoader.ConfigLoadException {
        Mine mine = repository.get(mineName);

        // Get marker from config - use mask marker if exists, otherwise use pink_concrete default
        BlockKey marker = getMarkerFromConfig(mine);
        Material markerMaterial = parseMarkerMaterial(marker);

        List<Cuboid> regions = mine.getRegions();
        List<Location> found = MaskScanner.scan(regions, markerMaterial);

        fileManager.saveMaskPositions(
                mineName,
                marker,
                found
        );

        repository.reload(mineName);

        return found.size();
    }

    private BlockKey getMarkerFromConfig(Mine mine) {
        var mask = mine.getConfig().blocks().mask();
        if (mask != null && mask.marker() != null) {
            return mask.marker();
        }
        // Default to pink_concrete
        return new BlockKey.Vanilla(Material.PINK_CONCRETE);
    }

    private Material parseMarkerMaterial(BlockKey key) {
        if (key instanceof BlockKey.Vanilla(Material material)) {
            return material;
        }
        // For custom blocks (Oraxen/ItemsAdder), we need to get the underlying material
        // This would need integration with those plugins to get the actual material
        return ValidationUtils.parseMaterialOrDefault(key.serialize(), Material.PINK_CONCRETE);
    }
}
