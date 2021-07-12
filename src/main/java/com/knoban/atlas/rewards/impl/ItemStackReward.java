package com.knoban.atlas.rewards.impl;

import com.knoban.atlas.rewards.Reward;
import com.knoban.atlas.rewards.RewardInfo;
import com.knoban.atlas.utils.Tools;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

@RewardInfo(name = "itemstack")
public class ItemStackReward extends Reward {

    private Material material;

    public ItemStackReward(Map<String, Object> data) {
        super(data);
        String materialName = (String) data.getOrDefault("material", Material.AIR.name());
        this.material = Material.getMaterial(materialName);
        if(material == null)
            throw new IllegalArgumentException("Invalid material: " + materialName);
        this.icon = createIcon(material, amount);
    }

    public ItemStackReward(ItemStack icon, long amount) {
        super(icon, amount);
    }

    public ItemStackReward(long amount) {
        super(createIcon(Material.AIR, amount), amount);
    }

    private static ItemStack createIcon(Material mat, long amount) {
        int amt = (int) amount;
        ItemStack icon = new ItemStack(mat, Math.min(amt, 64));
        ItemMeta im = icon.getItemMeta();
        im.setDisplayName("§f" + amount + " " + Tools.enumNameToHumanReadable(mat.name()));
        im.setLore(Arrays.asList("§7You get this item drop."));
        icon.setItemMeta(im);
        return icon;
    }

    @Override
    public void reward(@NotNull Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 0.6F);
        p.sendMessage("§2You got §f" + amount + " " + Tools.enumNameToHumanReadable(material.name()) + "§2!");

        p.getInventory().addItem(new ItemStack(material, (int) amount));
    }
}
