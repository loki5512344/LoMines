package dev.loki.lomines.wand.group;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player {@link GroupWandSession} for the group wand workflow.
 */
public final class GroupWandManager {

    private final Map<UUID, GroupWandSession> sessions = new ConcurrentHashMap<>();

    public GroupWandSession getSession(UUID playerId) {
        return sessions.computeIfAbsent(playerId, u -> new GroupWandSession());
    }

    public void removeSession(UUID playerId) {
        sessions.remove(playerId);
    }
}
