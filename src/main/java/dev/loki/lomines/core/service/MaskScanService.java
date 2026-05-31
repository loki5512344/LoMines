package dev.loki.lomines.core.service;

import dev.loki.lomines.core.Mine;
import dev.loki.lomines.data.config.parser.ConfigParseException;
import dev.loki.lomines.util.ValidationUtils;
import dev.loki.lomines.util.location.Cuboid;
import dev.loki.lomines.util.selection.MaskScanner;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.IOException;
import java.util.List;

/**
 * Service for scanning and saving mask positions.
 */
public final class MaskScanService {

    private final MineRepository repository;
    private final MineFileManager fileManager;

    public MaskScanService(MineRepository repository, MineFileManager fileManager) {
        this.repository = repository;
        this.fileManager = fileManager;
    }

    public int scanAndSave(String mineName) throws IOException, ConfigParseException {
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
        return ValidationUtils.parseMaterialOrDefault(materialName, Material.PINK_CONCRETE);
    }
}
