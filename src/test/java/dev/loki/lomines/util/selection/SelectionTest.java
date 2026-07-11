package dev.loki.lomines.util.selection;

import dev.loki.lomines.util.location.geo.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectionTest {

    private World world;
    private Selection selection;

    @BeforeEach
    void setUp() {
        world = mock(World.class);
        selection = new Selection();
        when(world.getName()).thenReturn("world");
    }

    @Test
    void testSetAndGetPoint() {
        Location loc = new Location(world, 10, 64, 20);

        selection.setPoint(0, loc);

        Optional<Location> result = selection.getPoint(0);
        assertTrue(result.isPresent());
        assertEquals(loc, result.get());
    }

    @Test
    void testGetPointReturnsEmptyWhenNotSet() {
        Optional<Location> result = selection.getPoint(5);
        assertFalse(result.isPresent());
    }

    @Test
    void testSetPointInvalidIndexThrows() {
        Location loc = new Location(world, 10, 64, 20);

        assertThrows(IllegalArgumentException.class, () -> selection.setPoint(-1, loc));
        assertThrows(IllegalArgumentException.class, () -> selection.setPoint(10, loc));
    }

    @Test
    void testGetPointInvalidIndexThrows() {
        assertThrows(IllegalArgumentException.class, () -> selection.getPoint(-1));
        assertThrows(IllegalArgumentException.class, () -> selection.getPoint(10));
    }

    @Test
    void testGetPair() {
        Location loc1 = new Location(world, 10, 64, 20);
        Location loc2 = new Location(world, 20, 74, 30);

        selection.setPoint(0, loc1);
        selection.setPoint(1, loc2);

        Location[] pair = selection.getPair(0);
        assertEquals(2, pair.length);
        assertEquals(loc1, pair[0]);
        assertEquals(loc2, pair[1]);
    }

    @Test
    void testGetPairWithNulls() {
        Location loc1 = new Location(world, 10, 64, 20);

        selection.setPoint(2, loc1);

        Location[] pair = selection.getPair(1);
        assertEquals(2, pair.length);
        assertEquals(loc1, pair[0]);
        assertNull(pair[1]);
    }

    @Test
    void testGetPairInvalidIndexThrows() {
        assertThrows(IllegalArgumentException.class, () -> selection.getPair(-1));
        assertThrows(IllegalArgumentException.class, () -> selection.getPair(5));
    }

    @Test
    void testHasPairReturnsTrueWhenBothSet() {
        Location loc1 = new Location(world, 10, 64, 20);
        Location loc2 = new Location(world, 20, 74, 30);

        selection.setPoint(0, loc1);
        selection.setPoint(1, loc2);

        assertTrue(selection.hasPair(0));
    }

    @Test
    void testHasPairReturnsFalseWhenOnlyOneSet() {
        Location loc1 = new Location(world, 10, 64, 20);

        selection.setPoint(0, loc1);

        assertFalse(selection.hasPair(0));
    }

    @Test
    void testHasPairReturnsFalseWhenNoneSet() {
        assertFalse(selection.hasPair(0));
    }

    @Test
    void testHasPairInvalidIndexThrows() {
        assertThrows(IllegalArgumentException.class, () -> selection.hasPair(-1));
        assertThrows(IllegalArgumentException.class, () -> selection.hasPair(5));
    }

    @Test
    void testClear() {
        Location loc1 = new Location(world, 10, 64, 20);
        Location loc2 = new Location(world, 20, 74, 30);

        selection.setPoint(0, loc1);
        selection.setPoint(1, loc2);
        selection.setPoint(5, loc1);

        selection.clear();

        assertFalse(selection.getPoint(0).isPresent());
        assertFalse(selection.getPoint(1).isPresent());
        assertFalse(selection.getPoint(5).isPresent());
    }
}
