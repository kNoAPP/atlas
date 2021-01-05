package com.knoban.atlas.listeners;

import com.knoban.atlas.Atlas;
import com.knoban.atlas.callbacks.Callback;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Used to simply cache callbacks for ItemStacks when hovered on.
 * Data on callbacks is purged when the player leaves!
 *
 * @author Alden Bansemer (kNoAPP)
 */
public final class HeldSlotListener implements Listener {

    public static final int START = 0, CONTINUOUS = 1, STOP = 2;
    private static final long CONTINUOUS_TICKS = 20L;

    private static final HeldSlotListener INSTANCE = new HeldSlotListener(Atlas.getInstance());

    private final JavaPlugin plugin;

    private final HashMap<Player, CallableItemSet> stack = new HashMap<>();
    private BukkitTask continuousTask;
    private boolean isListening;

    /**
     * Creates an Item Listener for the purpose of adding, tracking, and getting callbacks on an {@link ItemStack}. This
     * should be used in conjunction with itemized-abilities where those abilities require some call.
     *
     * This {@link HeldSlotListener} will automatically register itself as an {@link Listener} if there are
     * ItemStacks requiring tracking. When the number of items to track reduces to 0, it will automatically remove
     * its {@link Listener}.
     *
     * @param plugin The plugin instance that is registering this listener
     */
    private HeldSlotListener(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.isListening = false;
    }

    /**
     * Creates a callback local to a player's item. This callback is called when the player holds the item for the
     * first time.
     * @param player The {@link Player} to set the callback for
     * @param item The {@link ItemStack} to set the callback for
     * @param start A callback called when the item has started being held
     * @param continuous A callback called periodically when the item is held
     * @param stop A callback called when the item has stopped being held
     */
    public void setCallbacks(Player player, ItemStack item, Runnable start, Runnable continuous, Runnable stop) {
        CallableItemSet set = stack.get(player);
        if(set != null) {
            set.setItemCallbacks(item, start, continuous, stop);
        } else {
            set = new CallableItemSet();
            set.setItemCallbacks(item, start, continuous, stop);
            stack.put(player, set);
            checkListener();
        }
    }

    /**
     * Removes all callbacks of an {@link ItemStack} from a {@link Player}. This will stop tracking the {@link ItemStack}
     * and nothing will further occur when the {@link Player} holds the item in their hand.
     * @param player The {@link Player} to remove the callbacks from
     * @param item The {@link ItemStack} to remove the callbacks from
     */
    public void removeCallbacks(Player player, ItemStack item) {
        CallableItemSet set = stack.get(player);
        if(set != null) {
            set.removeItemCallbacks(item);
            if(set.getSize() <= 0) {
                stack.remove(player);
                checkListener();
            }
        }
    }

    /**
     * Runs the start callback of an {@link ItemStack} owned by a {@link Player}.
     * @param player The {@link Player} to get the callback from
     * @param item The {@link ItemStack} to get the callback of
     * @param type 0-2 type of callback
     */
    @Nullable
    public void runCallback(Player player, ItemStack item, int type) {
        if(type < 0 || 2 < type)
            return;

        CallableItemSet set = stack.get(player);
        if(set != null)
            set.runItemCallback(item, type);
    }

    /**
     * Gets the callback of an {@link ItemStack} owned by a {@link Player}.
     * @param player The {@link Player} to get the callback from
     * @param item The {@link ItemStack} to get the callback of
     * @param type 0-2 type of callback
     * @return The callback on the item
     */
    @Nullable
    public Runnable getCallback(Player player, ItemStack item, int type) {
        if(type < 0 || 2 < type)
            return null;
        CallableItemSet set = stack.get(player);
        if(set != null)
            return set.getItemCallbacks(item)[type];
        return null;
    }

    /**
     * Clears all callbacks of all {@link ItemStack}s from all {@link Player}s. This will also indirectly cause this
     * instance to remove itself as an {@link Listener}. It will only add itself back as a {@link Listener} if more
     * {@link ItemStack} charges are added to this instance.
     */
    public void clearAllCallbacks() {
        stack.clear();
        checkListener();
    }

    /**
     * Helper method to enable/disable this class's {@link Listener}.
     */
    private void checkListener() {
        if(isListening && stack.size() <= 0) {
            HandlerList.unregisterAll(this);
            continuousTask.cancel();
            isListening = false;
        } else if(!isListening && stack.size() > 0) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            continuousTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                for(Player pl : stack.keySet()) {
                    CallableItemSet set = stack.get(pl);
                    set.runItemCallback(pl.getInventory().getItemInMainHand(), CONTINUOUS);
                }
            }, CONTINUOUS_TICKS, CONTINUOUS_TICKS);
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
            runCallback(p, is, START);

            is = p.getInventory().getItem(e.getPreviousSlot());
            runCallback(p, is, STOP);
        }
    }

    public static HeldSlotListener getInstance() {
        return INSTANCE;
    }

    /**
     * A helper class to keep track of all {@link ItemStack}s with callbacks.
     */
    private static class CallableItemSet {

        private final HashMap<ItemStack, Runnable[]> itemCallbacks = new HashMap<>();

        public void setItemCallbacks(@Nullable ItemStack item, @Nullable Runnable... callbacks) {
            if(item == null)
                return;

            itemCallbacks.put(item, callbacks);
        }

        public Runnable[] removeItemCallbacks(@Nullable ItemStack item) {
            if(item == null)
                return null;

            return itemCallbacks.remove(item);
        }

        public void runItemCallback(@Nullable ItemStack item, int type) {
            if(item == null || type < 0 || 2 < type)
                return;

            Runnable[] callbacks = itemCallbacks.get(item);
            if(callbacks == null)
                return;

            if(callbacks[type] != null)
                callbacks[type].run();

        }

        @Nullable
        public Runnable[] getItemCallbacks(@Nullable ItemStack item) {
            if(item == null)
                return null;

            return itemCallbacks.get(item);
        }

        public int getSize() {
            return itemCallbacks.size();
        }
    }
}
