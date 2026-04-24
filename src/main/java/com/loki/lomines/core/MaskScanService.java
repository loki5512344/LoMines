package com.loki.lomines.core;

import com.loki.lomines.data.config.ConfigParseException;
import com.loki.lomines.util.location.Cuboid;
import com.loki.lomines.util.selection.MaskScanner;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.IOException;
import java.util.List;

/**
 * Service for scanning and saving mask positions.
 */
final class MaskScanService {

    private final MineRepository repository;
    private final MineFileManager fileManager;

    MaskScanService(MineRepository repository, MineFileManager fileManager) {
        this.repository = repository;
        this.fileManager = fileManager;
    }

    int scanAndSave(String mineName) throws IOException, ConfigParseException {
        Mine mine = repository.get(mineName);

        Material marker = parseMarkerMaterial(mine.getConfig().getMaskMarkerMaterial());
        List<Cuboid> regions = mine.getRegions();
        List<Location> found = MaskScanner.scan(regions, marker);

        fileManager.saveMaskPositions(
            mineName,
            mine.getConfig().getMaskMarkerMaterial(),
            found
        );

        repository.reload(mineName);

        return found.size();
    }

    private Material parseMarkerMaterial(String materialName) {
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.PINK_CONCRETE;
        }
    }
}
