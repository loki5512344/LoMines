package dev.loki.lomines.gui.mine.edit.blocks.select;

import dev.loki.lomines.gui.common.ItemStackFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BlockMaterialSelector {

    private static final List<Material> AVAILABLE_MATERIALS = new ArrayList<>();

    static {
        for (Material mat : Material.values()) {
            if (mat.isBlock() && !mat.isAir() && mat.isSolid()) {
                AVAILABLE_MATERIALS.add(mat);
            }
        }
        AVAILABLE_MATERIALS.sort((a, b) -> a.name().compareTo(b.name()));
    }

    private BlockMaterialSelector() {
    }

    public static List<Material> getAvailableMaterials() {
        return AVAILABLE_MATERIALS;
    }

    public static Material getMaterialAtSlot(int slot, int page, int itemsPerPage) {
        int index = page * itemsPerPage + slot;
        if (index >= 0 && index < AVAILABLE_MATERIALS.size()) {
            return AVAILABLE_MATERIALS.get(index);
        }
        return null;
    }

    public static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    public static ItemStack materialItem(Material material) {
        String name = formatMaterialName(material);
        return ItemStackFactory.create(material, "\u00a7a\u00a7l" + name,
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0434\u043b\u044f \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0438\u044f",
                "\u00a77\u0432 \u043a\u043e\u043d\u0444\u0438\u0433\u0443\u0440\u0430\u0446\u0438\u044e \u0448\u0430\u0445\u0442\u044b",
                "",
                "\u00a78ID: \u00a77" + material.name().toLowerCase());
    }

    public static ItemStack prevPageItem() {
        return ItemStackFactory.create(Material.ARROW, "\u00a7e\u00a7l\u2190 \u041f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0430\u044f",
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0434\u043b\u044f \u043f\u0435\u0440\u0435\u0445\u043e\u0434\u0430",
                "\u00a77\u043d\u0430 \u043f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0443\u044e \u0441\u0442\u0440\u0430\u043d\u0438\u0446\u0443"
        );
    }

    public static ItemStack nextPageItem() {
        return ItemStackFactory.create(Material.ARROW, "\u00a7e\u00a7l\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f \u2192",
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0434\u043b\u044f \u043f\u0435\u0440\u0435\u0445\u043e\u0434\u0430",
                "\u00a77\u043d\u0430 \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0443\u044e \u0441\u0442\u0440\u0430\u043d\u0438\u0446\u0443"
        );
    }

    public static ItemStack backItem() {
        return ItemStackFactory.create(Material.BARRIER, "\u00a7c\u00a7l\u041e\u0442\u043c\u0435\u043d\u0430",
                "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                "\u00a77\u0412\u0435\u0440\u043d\u0443\u0442\u044c\u0441\u044f"
                        + " \u0431\u0435\u0437 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0438\u044f");
    }
}
