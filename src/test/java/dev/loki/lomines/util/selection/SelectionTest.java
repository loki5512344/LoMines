package dev.loki.lomines.util.selection;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void testToCuboidsWithNoPairs() {
        List<Cuboid> cuboids = selection.toCuboids();

        assertTrue(cuboids.isEmpty());
    }

    @Test
    void testToCuboidsWithOnePair() {
        Location loc1 = new Location(world, 10, 64, 20);
        Location loc2 = new Location(world, 20, 74, 30);

        selection.setPoint(0, loc1);
        selection.setPoint(1, loc2);

        List<Cuboid> cuboids = selection.toCuboids();

        assertEquals(1, cuboids.size());
        assertTrue(cuboids.get(0).contains(new Location(world, 15, 69, 25)));
    }

    @Test
    void testToCuboidsWithMultiplePairs() {
        Location loc1 = new Location(world, 10, 64, 20);
        Location loc2 = new Location(world, 20, 74, 30);
        Location loc3 = new Location(world, 30, 64, 40);
        Location loc4 = new Location(world, 40, 74, 50);

        selection.setPoint(0, loc1);
        selection.setPoint(1, loc2);
        selection.setPoint(2, loc3);
        selection.setPoint(3, loc4);

        List<Cuboid> cuboids = selection.toCuboids();

        assertEquals(2, cuboids.size());
    }

    @Test
    void testToCuboidsSkipsIncompletePairs() {
        Location loc1 = new Location(world, 10, 64, 20);
        Location loc2 = new Location(world, 20, 74, 30);
        Location loc3 = new Location(world, 30, 64, 40);

        selection.setPoint(0, loc1);
        selection.setPoint(1, loc2);
        selection.setPoint(2, loc3);
        // Point 3 not set

        List<Cuboid> cuboids = selection.toCuboids();

        assertEquals(1, cuboids.size());
    }

    @Test
    void testPairInvariant() {
        // Test that pairs are organized as 2i and 2i+1
        Location loc1 = new Location(world, 10, 64, 20);
        Location loc2 = new Location(world, 20, 74, 30);

        // Pair 0: indices 0 and 1
        selection.setPoint(0, loc1);
        selection.setPoint(1, loc2);
        assertTrue(selection.hasPair(0));

        // Pair 1: indices 2 and 3
        selection.setPoint(2, loc1);
        selection.setPoint(3, loc2);
        assertTrue(selection.hasPair(1));

        // Pair 2: indices 4 and 5
        selection.setPoint(4, loc1);
        selection.setPoint(5, loc2);
        assertTrue(selection.hasPair(2));

        // Pair 3: indices 6 and 7
        selection.setPoint(6, loc1);
        selection.setPoint(7, loc2);
        assertTrue(selection.hasPair(3));

        // Pair 4: indices 8 and 9
        selection.setPoint(8, loc1);
        selection.setPoint(9, loc2);
        assertTrue(selection.hasPair(4));
    }
}
