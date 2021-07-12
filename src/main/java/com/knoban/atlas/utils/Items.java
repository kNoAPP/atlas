package com.knoban.atlas.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Items {

    public static final ItemStack DEFAULT_REWARD_ITEM = getDefaultRewardItem();
    public static final ItemStack GUI_PLACEHOLDER_LIGHT_GRAY_ITEM = getGUIPlaceholderLightGrayItem();
    public static final ItemStack GUI_PLACEHOLDER_LIME_ITEM = getGUIPlaceholderLimeItem();
    public static final ItemStack GUI_PLACEHOLDER_ORANGE_ITEM = getGUIPlaceholderOrangeItem();
    public static final ItemStack BACK_ITEM = getBackItem();
    public static final ItemStack DECLINE_ITEM = getDeclineItem();
    public static final ItemStack ACCEPT_ITEM = getAcceptItem();
    public static final ItemStack NEXT_PAGE = getNextPageItem();
    public static final ItemStack PREVIOUS_PAGE = getPreviousPageItem();
    public static final ItemStack PASS_MAIN_EXPLANATION_ITEM = getMainPassExplanationItem();
    public static final ItemStack PASS_MISSIONS_MENU_ITEM = getMissionsMenuItem();
    public static final ItemStack PASS_PASS_MENU_ITEM = getBattlePassMenuItem();
    public static final ItemStack MISSIONS_EXPLANATION_ITEM = getMissionsExplanationItem();
    public static final ItemStack BATTLEPASS_EXPLANATION_ITEM = getBattlePassExplanationItem();
    public static final ItemStack BATTLEPASS_PURCHASE_PASS = getBattlePassPurchasePass();
    public static final ItemStack BATTLEPASS_UNLOCKED_LEVEL = getBattlePassUnlockedLevel();
    public static final ItemStack BATTLEPASS_LOCKED_LEVEL = getBattlePassLockedLevel();

    private static ItemStack getDefaultRewardItem() {
        ItemStack is = new ItemStack(Material.CHEST);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§7Reward Placeholder");
        List<String> lores = new ArrayList<>();
        lores.add(ChatColor.RED + "Developer description here.");
        im.setLore(lores);
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getGUIPlaceholderLightGrayItem() {
        ItemStack is = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§7");
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getGUIPlaceholderLimeItem() {
        ItemStack is = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§a");
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getGUIPlaceholderOrangeItem() {
        ItemStack is = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§6");
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getBackItem() {
        ItemStack is = new ItemStack(Material.ARROW);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§7Go Back");
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getDeclineItem() {
        ItemStack is = new ItemStack(Material.RED_DYE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§4Decline Purchase");
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getAcceptItem() {
        ItemStack is = new ItemStack(Material.LIME_DYE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§2Accept Purchase");
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getNextPageItem() {
        ItemStack is = new ItemStack(Material.GREEN_CARPET);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§bNext Page");
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getPreviousPageItem() {
        ItemStack is = new ItemStack(Material.RED_CARPET);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§bPrevious Page");
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getMainPassExplanationItem() {
        ItemStack is = new ItemStack(Material.BIRCH_SIGN);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§eWhat's this menu?");
        List<String> lores = new ArrayList<String>();
        lores.add("§7Here you can view the");
        lores.add("§7the §6Battle Pass§7 and your");
        lores.add("§2current missions§7.");
        im.setLore(lores);
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getMissionsMenuItem() {
        ItemStack is = new ItemStack(Material.END_CRYSTAL);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§dMissions");
        List<String> lores = new ArrayList<>();
        lores.add("§7View your §2current missions");
        lores.add("§7and your §bprogress§7.");
        im.setLore(lores);
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getBattlePassMenuItem() {
        ItemStack is = new ItemStack(Material.GOLD_INGOT);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§3Battle Pass");
        List<String> lores = new ArrayList<>();
        lores.add("§7View the §6Battle Pass §7and");
        lores.add("§7your §bupcoming rewards§7.");
        im.setLore(lores);
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getMissionsExplanationItem() {
        ItemStack is = new ItemStack(Material.BIRCH_SIGN);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§eWhat's this menu?");
        List<String> lores = new ArrayList<String>();
        lores.add("§7Here you can view all your");
        lores.add("§2current missions §7and their");
        lores.add("§brewards§7.");
        im.setLore(lores);
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getBattlePassExplanationItem() {
        ItemStack is = new ItemStack(Material.BIRCH_SIGN);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§eWhat's this menu?");
        List<String> lores = new ArrayList<>();
        lores.add("§7Here you can view your");
        lores.add("§6Battle Pass' §bupcoming rewards§7.");
        im.setLore(lores);
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getBattlePassPurchasePass() {
        ItemStack is = new ItemStack(Material.GOLD_INGOT);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§eWant more rewards?");
        List<String> lores = new ArrayList<String>();
        lores.add("§2Click here to purchase this");
        lores.add("§2season's §bpremium §6Battle Pass");
        lores.add("§2for §b§oextra rewards§2!");
        im.setLore(lores);
        is.setItemMeta(im);
        return is;
    }

    private static ItemStack getBattlePassUnlockedLevel() {
        return new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
    }

    private static ItemStack getBattlePassLockedLevel() {
        return new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
    }
}
