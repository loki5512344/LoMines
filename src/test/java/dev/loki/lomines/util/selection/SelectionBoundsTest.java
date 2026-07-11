package dev.loki.lomines.util.selection;

import dev.loki.lomines.util.location.geo.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectionBoundsTest {

    private World world;
    private Selection selection;

    @BeforeEach
    void setUp() {
        world = mock(World.class);
        selection = new Selection();
        when(world.getName()).thenReturn("world");
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

        List<Cuboid> cuboids = selection.toCuboids();

        assertEquals(1, cuboids.size());
    }

    @Test
    void testPairInvariant() {
        Location loc1 = new Location(world, 10, 64, 20);
        Location loc2 = new Location(world, 20, 74, 30);

        selection.setPoint(0, loc1);
        selection.setPoint(1, loc2);
        assertTrue(selection.hasPair(0));

        selection.setPoint(2, loc1);
        selection.setPoint(3, loc2);
        assertTrue(selection.hasPair(1));

        selection.setPoint(4, loc1);
        selection.setPoint(5, loc2);
        assertTrue(selection.hasPair(2));

        selection.setPoint(6, loc1);
        selection.setPoint(7, loc2);
        assertTrue(selection.hasPair(3));

        selection.setPoint(8, loc1);
        selection.setPoint(9, loc2);
        assertTrue(selection.hasPair(4));
    }
}
