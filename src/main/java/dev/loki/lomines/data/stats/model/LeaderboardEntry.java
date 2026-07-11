package dev.loki.lomines.data.stats.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable data class representing a single entry in the leaderboard.
 * Contains a player's UUID and their block count.
 */
public record LeaderboardEntry(UUID playerId, long count) {

    public LeaderboardEntry(UUID playerId, long count) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null");
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LeaderboardEntry that = (LeaderboardEntry) o;
        return count == that.count && playerId.equals(that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, count);
    }

    @Override
    public String toString() {
        return "LeaderboardEntry{playerId=" + playerId + ", count=" + count + "}";
    }
}
