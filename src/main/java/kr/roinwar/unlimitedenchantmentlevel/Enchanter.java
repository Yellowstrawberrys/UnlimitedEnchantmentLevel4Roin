package kr.roinwar.unlimitedenchantmentlevel;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.util.HashMap;
import java.util.Map;

public class Enchanter {

    static Map<Enchantment, Integer> itemEnchantMultiplier = Map.ofEntries(
            Map.entry(Enchantment.PROTECTION_ENVIRONMENTAL, 1),
            Map.entry(Enchantment.PROTECTION_FIRE, 2),
            Map.entry(Enchantment.PROTECTION_FALL, 2),
            Map.entry(Enchantment.PROTECTION_EXPLOSIONS, 4),
            Map.entry(Enchantment.PROTECTION_PROJECTILE, 2),
            Map.entry(Enchantment.THORNS, 8),
            Map.entry(Enchantment.OXYGEN, 4),
            Map.entry(Enchantment.DEPTH_STRIDER, 4),
            Map.entry(Enchantment.WATER_WORKER, 4),
            Map.entry(Enchantment.DAMAGE_ALL, 1),
            Map.entry(Enchantment.DAMAGE_UNDEAD, 2),
            Map.entry(Enchantment.DAMAGE_ARTHROPODS, 2),
            Map.entry(Enchantment.KNOCKBACK, 2),
            Map.entry(Enchantment.FIRE_ASPECT, 4),
            Map.entry(Enchantment.LOOT_BONUS_MOBS, 4),
            Map.entry(Enchantment.DIG_SPEED, 1),
            Map.entry(Enchantment.SILK_TOUCH, 8),
            Map.entry(Enchantment.DURABILITY, 2),
            Map.entry(Enchantment.LOOT_BONUS_BLOCKS, 4),
            Map.entry(Enchantment.ARROW_DAMAGE, 1),
            Map.entry(Enchantment.ARROW_KNOCKBACK, 4),
            Map.entry(Enchantment.ARROW_FIRE, 4),
            Map.entry(Enchantment.ARROW_INFINITE, 8),
            Map.entry(Enchantment.LUCK, 4),
            Map.entry(Enchantment.LURE, 4),
            Map.entry(Enchantment.FROST_WALKER, 4),
            Map.entry(Enchantment.MENDING, 4),
            Map.entry(Enchantment.BINDING_CURSE, 8),
            Map.entry(Enchantment.VANISHING_CURSE, 8),
            Map.entry(Enchantment.IMPALING, 4),
            Map.entry(Enchantment.RIPTIDE, 4),
            Map.entry(Enchantment.LOYALTY, 1),
            Map.entry(Enchantment.CHANNELING, 8),
            Map.entry(Enchantment.MULTISHOT, 4),
            Map.entry(Enchantment.PIERCING, 1),
            Map.entry(Enchantment.QUICK_CHARGE, 2),
            Map.entry(Enchantment.SOUL_SPEED, 8),
            Map.entry(Enchantment.SWIFT_SNEAK, 8),
            Map.entry(Enchantment.SWEEPING_EDGE, 4)
    );

    public boolean isResultEnchanted(ItemStack first, ItemStack second) {
        return (first != null && (second.getItemMeta().hasEnchants() || second.getType() == Material.ENCHANTED_BOOK)) || (first.getType() == Material.ENCHANTED_BOOK && second.getType() == Material.ENCHANTED_BOOK);
    }

    public Map.Entry<Integer, ItemStack> enchant(ItemStack first, ItemStack second, String renamed) {
        if(second.getType() == Material.ENCHANTED_BOOK) return enchantItem(renamed, true, first, second);
        else if(first.getType() != second.getType()) return null;
        else return enchantItem(renamed, false, first, second);
    }

    private Map.Entry<Integer, ItemStack> enchantItem(String renamed, boolean isEnchantingWithBook, ItemStack... items) {
        if(items.length != 2) throw new IllegalArgumentException("ItemStack should be 2");
        Map<Enchantment, Integer> result = new HashMap<>();
        int cost = 0;

        for(ItemStack itm : items) {
            for(Map.Entry<Enchantment, Integer> enchantment : (itm.getType() == Material.ENCHANTED_BOOK ? ((EnchantmentStorageMeta) itm.getItemMeta()).getStoredEnchants().entrySet() : itm.getEnchantments().entrySet())) {
                //Skip if is not applicable
                if(!isApplicable(items[0], enchantment.getKey())) continue;

                int multiplier = (isEnchantingWithBook ? getBookMultiplier(enchantment.getKey()) : Enchanter.itemEnchantMultiplier.get(enchantment.getKey()));

                if(result.containsKey(enchantment.getKey())) {
                    cost -= result.get(enchantment.getKey())*multiplier;
                    result.replace(enchantment.getKey(), (enchantment.getValue().intValue() == result.get(enchantment.getKey()).intValue() ? enchantment.getValue() + 1 : Math.max(enchantment.getValue(), result.get(enchantment.getKey()))));
                } else {
                    result.put(enchantment.getKey(), enchantment.getValue());
                }
                cost += result.get(enchantment.getKey())*multiplier;
            }
        }

        boolean isnamechanged = !renamed.isEmpty();
        boolean isRepaired = false;

        ItemStack itemResult = new ItemStack(items[0].getType());
        if(!isEnchantingWithBook) {
            Damageable d = (Damageable) items[0].getItemMeta();
            int dd = ((Damageable) items[0].getItemMeta()).getDamage() + (((Damageable) items[1].getItemMeta()).getDamage()-items[1].getType().getMaxDurability());
            if(dd > (itemResult.getType().getMaxDurability() * 0.12)) {
                dd -= itemResult.getType().getMaxDurability() * 0.12;
            }
            d.setDamage((Math.max(dd, 0)));
            itemResult.setItemMeta(d);
            if(d.getDamage() > 0) isRepaired = true;
        }
        Repairable meta = (Repairable) itemResult.getItemMeta();
        if(isnamechanged) meta.displayName(Component.text(renamed));
        meta.setRepairCost(Math.max(((Repairable)items[0].getItemMeta()).getRepairCost(), ((Repairable)items[1].getItemMeta()).getRepairCost())+1);
        clearEnchantments(meta, items[0].getType() == Material.ENCHANTED_BOOK);
        addEnchantments(meta, result, items[0].getType() == Material.ENCHANTED_BOOK);
        itemResult.setItemMeta(meta);

        return Map.entry((int) (cost+(Math.pow(2, meta.getRepairCost())-1)+(isnamechanged?1:0)+(isRepaired?1:0)), itemResult);
    }

    private int getBookMultiplier(Enchantment enchantment) {
        return (itemEnchantMultiplier.get(enchantment)/2 > 0 ? itemEnchantMultiplier.get(enchantment)/2 : 1);
    }

    private boolean isApplicable(ItemStack item, Enchantment enchantment) {
        if(!enchantment.getItemTarget().includes(item)) return false;
        else if(item.getEnchantments().containsKey(enchantment)) return true;
        for(Enchantment e : item.getEnchantments().keySet()) {
            if(enchantment.conflictsWith(e)) return false;
        }
        return enchantment.getItemTarget().includes(item) || item.getType() == Material.ENCHANTED_BOOK;
    }

    private void clearEnchantments(ItemMeta meta, boolean isBook) {
        for(Enchantment e : meta.getEnchants().keySet()) {
            if(isBook) ((EnchantmentStorageMeta) meta).removeStoredEnchant(e);
            else meta.removeEnchant(e);
        }
    }

    private void addEnchantments(ItemMeta meta, Map<Enchantment, Integer> enchantments, boolean isBook) {
        for(Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if(isBook) ((EnchantmentStorageMeta) meta).addStoredEnchant(entry.getKey(), entry.getValue(), true);
            else meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
    }
}
