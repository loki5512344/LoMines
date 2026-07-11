package dev.loki.lomines.core.mine.registry;

import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.core.service.MineRepository;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Optional;

public final class MineFinder {

    private final MineRepository repository;

    public MineFinder(MineRepository repository) {
        this.repository = repository;
    }

    public Mine get(String name) {
        return repository.get(name);
    }

    public Optional<Mine> find(String name) {
        return repository.find(name);
    }

    public Collection<Mine> getAll() {
        return repository.getAll();
    }

    public Optional<Mine> findByLocation(Location location) {
        return repository.findByLocation(location);
    }
}
