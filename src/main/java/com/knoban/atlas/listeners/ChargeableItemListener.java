package com.knoban.atlas.listeners;

import com.knoban.atlas.utils.Tools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Used to simply cache charges for ItemStacks that flash when hovered on.
 * Data on charges is purged when the player leaves!
 *
 * Use {@link HeldSlotListener} instead for use of callbacks. This class utility is deprecated.
 *
 * @author Alden Bansemer (kNoAPP)
 */
@Deprecated
public class ChargeableItemListener implements Listener {

    private HashMap<Player, ChargableItemSet> stack = new HashMap<Player, ChargableItemSet>();

    private JavaPlugin plugin;
    private boolean isListening;

    /**
     * Creates an Item Listener for the purpose of adding, tracking, and getting charges on an {@link ItemStack}. This
     * should be used in conjunction with itemized-abilities where those abilities require some charge.
     *
     * This {@link ChargeableItemListener} will automatically register itself as an {@link Listener} if there are
     * ItemStacks requiring tracking. When the number of items to track reduces to 0, it will automatically remove
     * its {@link Listener}.
     *
     * @param plugin The plugin instance that is registering this listener
     */
    public ChargeableItemListener(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.isListening = false;
    }

    /**
     * Grant charges of an ItemStack to a specific {@link Player}. These charges will appear when the {@link Player}
     * holds the item in their hand.
     * @param player The {@link Player} to set the charges for
     * @param item The {@link ItemStack} to set the charges for
     * @param charges The number of charges
     */
    public void setCharges(Player player, ItemStack item, Integer charges) {
        setCharges(player, item, charges, "§6Charges §e(§f%c%§e)");
    }

    /**
     * Grant charges of an ItemStack to a specific {@link Player}. These charges will appear when the {@link Player}
     * holds the item in their hand.
     * @param player The {@link Player} to set the charges for
     * @param item The {@link ItemStack} to set the charges for
     * @param charges The number of charges
     * @param alert The alert message with a {@code %c%} representing the number of charges
     */
    public void setCharges(Player player, ItemStack item, Integer charges, String alert) {
        ChargableItemSet set = stack.get(player);
        if(set != null) {
            set.setItem(item, charges, alert);
        } else {
            set = new ChargableItemSet();
            set.setItem(item, charges, alert);
            stack.put(player, set);
            checkListener();
        }
    }

    /**
     * Removes all charges of an {@link ItemStack} from a {@link Player}. This will stop tracking the {@link ItemStack}
     * and nothing will further occur when the {@link Player} holds the item in their hand.
     * @param player The {@link Player} to remove the charges from
     * @param item The {@link ItemStack} to remove the charges from
     */
    public void removeCharges(Player player, ItemStack item) {
        ChargableItemSet set = stack.get(player);
        if(set != null) {
            set.removeItem(item);
            if(set.getSize() <= 0) {
                stack.remove(player);
                checkListener();
            }
        }
    }

    /**
     * Gets all charges of an {@link ItemStack} owned by a {@link Player}.
     * @param player The {@link Player} to get the charges from
     * @param item The {@link ItemStack} to get the charges of
     */
    public Integer getCharges(Player player, ItemStack item) {
        ChargableItemSet set = stack.get(player);
        if(set != null)
            return set.getCharges(item);
        return null;
    }

    /**
     * Gets the alert message of an {@link ItemStack} owned by a {@link Player}.
     * @param player The {@link Player} to get the message from
     * @param item The {@link ItemStack} to get the message of
     */
    public String getAlert(Player player, ItemStack item) {
        ChargableItemSet set = stack.get(player);
        if(set != null)
            return set.getAlertMsg(item);
        return null;
    }

    /**
     * Clears all charges of all {@link ItemStack}s from all {@link Player}s. This will also indirectly cause this
     * instance to remove itself as an {@link Listener}. It will only add itself back as a {@link Listener} if more
     * {@link ItemStack} charges are added to this instance.
     */
    public void clearAllCharges() {
        stack.clear();
        checkListener();
    }

    /**
     * Helper method to enable/disable this class's {@link Listener}.
     */
    private void checkListener() {
        if(isListening && stack.size() <= 0) {
            HandlerList.unregisterAll(this);
            isListening = false;
        } else if(!isListening && stack.size() > 0) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            isListening = true;
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        stack.remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSlotChange(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        if(stack.containsKey(p)) {
            ItemStack is = p.getInventory().getItem(e.getNewSlot());
            Integer charges = getCharges(p, is);
            if(charges != null) {
                Tools.actionbarMessage(p, getAlert(p, is).replaceAll("%c%", charges.toString()));
            }
        }
    }

    /**
     * A helper class to keep track of all {@link ItemStack}s with charges.
     */
    public static class ChargableItemSet {

        private TreeMap<String, Integer> charge = new TreeMap<>();
        private TreeMap<String, String> alert = new TreeMap<>();

        public void setItem(ItemStack item, Integer charges, String alertMsg) {
            if(item == null || !item.hasItemMeta())
                return;

            ItemMeta im = item.getItemMeta();
            charge.put(im.getDisplayName(), charges);
            alert.put(im.getDisplayName(), alertMsg);
        }

        public void removeItem(ItemStack item) {
            if(item == null || !item.hasItemMeta())
                return;

            ItemMeta im = item.getItemMeta();
            charge.remove(im.getDisplayName());
            alert.remove(im.getDisplayName());
        }

        public Integer getCharges(ItemStack item) {
            if(item == null || !item.hasItemMeta())
                return null;

            ItemMeta im = item.getItemMeta();
            return charge.get(im.getDisplayName());
        }

        public String getAlertMsg(ItemStack item) {
            if(item == null || !item.hasItemMeta())
                return null;

            ItemMeta im = item.getItemMeta();
            return alert.get(im.getDisplayName());
        }

        public int getSize() {
            return charge.size();
        }
    }
}
