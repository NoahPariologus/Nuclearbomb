package com.cna.nuclearbomb;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

public final class Nuclearbomb extends JavaPlugin {

    public static ItemStack MissileDevice;
    public static Plugin plugin;

    @Override
    public void onEnable() {
        plugin=this;

        MissileDevice=new ItemStack(Material.DETECTOR_RAIL);
        ItemMeta MD_meta = MissileDevice.getItemMeta();
        if(MD_meta!=null) {
            MD_meta.setDisplayName("MissileDevice");
            MD_meta.setMaxStackSize(1);
        }
        MissileDevice.setItemMeta(MD_meta);//注册物品

        NamespacedKey key = new NamespacedKey(this, "missile_device");
        ShapedRecipe MD_recipe = new ShapedRecipe(key,MissileDevice);
        MD_recipe.shape(
                "III",
                "BBB",
                "DRD");
        MD_recipe.setIngredient('D', Material.DISPENSER);//发射器
        MD_recipe.setIngredient('I', Material.IRON_BLOCK);//铁块
        MD_recipe.setIngredient('B', Material.REDSTONE_BLOCK);//红石块
        MD_recipe.setIngredient('R', Material.OBSERVER);//侦测器
        Bukkit.getServer().addRecipe(MD_recipe);

        Bukkit.getServer().getPluginManager().registerEvents(new CheckBomb(),this);
        getCommand("nuc").setExecutor(new NUCTRL());

        Bukkit.getServer().getLogger().info("核弹插件加载完成");
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().getLogger().info("核弹插件卸载完成");
    }


}
