package kr.roinwar.unlimitedenchantmentlevel;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class UnlimitedEnchantmentLevel extends JavaPlugin implements Listener {

    Enchanter enchanter = new Enchanter();

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void prepareAnvilEvent(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        //Check Null
        if(anvil.getFirstItem() == null || anvil.getSecondItem() == null) return;

        if(enchanter.isResultEnchanted(anvil.getFirstItem(), anvil.getSecondItem())) {
            Map.Entry<Integer, ItemStack> entry = enchanter.enchant(anvil.getFirstItem(), anvil.getSecondItem(), anvil.getRenameText());
            if(entry == null) return;
            event.setResult(entry.getValue());
            anvil.setRepairCost(entry.getKey());
        }
    }
}
