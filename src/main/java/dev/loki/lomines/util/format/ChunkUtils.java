package dev.loki.lomines.util.format;

import dev.loki.lomines.util.location.geo.Cuboid;
import org.bukkit.Chunk;

import java.lang.reflect.Method;

public final class ChunkUtils {

    private static final boolean IS_PAPER = detectPaper();
    private static final Method SEND_CHUNK_CHANGE_METHOD = findSendChunkChangeMethod();

    private ChunkUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void updateChunks(Cuboid region) {
        if (region == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }
        ChunkRefresher.refreshChunks(region, IS_PAPER, SEND_CHUNK_CHANGE_METHOD);
    }

    private static boolean detectPaper() {
        try {
            Class<?> chunkClass = Chunk.class;
            chunkClass.getMethod("getPluginChunkTickets");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static Method findSendChunkChangeMethod() {
        if (!IS_PAPER) {
            return null;
        }
        try {
            Class<?> chunkClass = Chunk.class;
            return chunkClass.getMethod("sendChunkChange");
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
