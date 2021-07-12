package com.knoban.atlas.gui;

import com.knoban.atlas.callbacks.Callback;
import com.knoban.atlas.utils.SoundBundle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Alden Bansemer (kNoAPP)
 */
public class GUI implements Listener {

    private HashSet<Player> viewers = new HashSet<>();
    private Callback onCloseCallback, onDestroyCallback;
    private Inventory inv;
    private SoundBundle openGUISound, closeGUISound, nonInteractionSound, playerInvSound;
    private ArrayList<GUIClickable> clickables;

    private boolean autoDestroy = true;
    private boolean isDestroyed = false;

    /**
     * Create a new GUI with a custom title/size tied to a plugin.
     * @param plugin Your plugin instance
     * @param title The title of the inventory container (appears at the top)
     * @param invSize Size of the inventory (ex. 9, 18, 24, 36, etc.)
     */
    public GUI(@NotNull Plugin plugin, @NotNull String title, int invSize) {
        this(plugin, title, invSize, null, null, null, null);
    }

    /**
     * Create a new GUI with a custom title/size tied to a plugin.
     * @param plugin Your plugin instance
     * @param title The title of the inventory container (appears at the top)
     * @param invSize Size of the inventory (ex. 9, 18, 24, 36, etc.)
     * @param openGUISound Add a open GUI sound (can be null)...
     * @param closeGUISound Add a close GUI sound (can be null)...
     * @param nonInteractionSound Add a non-interaction sound for non-action ItemStacks (can be null)
     * @param playerInvSound Add a sound for when a player clicks in their inventory and not the GUI (can be null)
     */
    public GUI(@NotNull Plugin plugin, @NotNull String title, int invSize,
               @Nullable SoundBundle openGUISound,
               @Nullable SoundBundle closeGUISound,
               @Nullable SoundBundle nonInteractionSound,
               @Nullable SoundBundle playerInvSound) {
        inv = Bukkit.createInventory(null, invSize, title);
        clickables = new ArrayList<>(invSize);
        // Fully initialize array
        for(int i=0; i<invSize; i++)
            clickables.add(null);
        this.openGUISound = openGUISound;
        this.closeGUISound = closeGUISound;
        this.nonInteractionSound = nonInteractionSound;
        this.playerInvSound = playerInvSound;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Open this GUI for a player.
     * @param p The player to open the GUI for
     */
    public void openInv(@NotNull Player p) {
        viewers.add(p);
        p.openInventory(inv);
        if(openGUISound != null)
            openGUISound.playToPlayer(p, p.getLocation());
    }

    /**
     * Set a non-intractable ItemStack in a slot of the GUI.
     * @param slot # of slot. Starts from 0 (or the top-left slot)
     * @param is The ItemStack to put there (won't be intractable since no Clickable is attached)
     */
    public void setSlot(int slot, @NotNull ItemStack is) {
        inv.setItem(slot, is);
        clickables.set(slot, null);
    }

    /**
     * Set a intractable ItemStack in a slot of the GUI. Does something when clicked.
     * @param slot # of slot. Starts from 0 (or the top-left slot)
     * @param is The ItemStack to put there
     * @param clickable All possible actions from left-clicking, right-clicking, etc. on this item
     */
    public void setSlot(int slot, @NotNull ItemStack is, @Nullable GUIClickable clickable) {
        inv.setItem(slot, is);
        clickables.set(slot, clickable);
    }

    /**
     * Shifts the entire row of a GUI by an amount. ItemStacks thrown off one end or the other will appear on
     * the opposite end of the GUI. If an invalid row number is passed, this function returns immediately.
     * @param row The row number (starting from 0)
     * @param amount The amount to shift by (-9 to +9)
     */
    public void shiftRow(int row, int amount) {
        if(row < 0 || inv.getSize()/9 <= row || amount < -9 || 9 < amount)
            return;

        int baseIndex = row*9;
        GUIClickable[] shiftedClickables = new GUIClickable[9];
        ItemStack[] shiftedItemStacks = new ItemStack[9];
        for(int i=0; i<9; i++) {
            shiftedClickables[(i+amount+9)%9] = clickables.get(baseIndex+i);
            shiftedItemStacks[(i+amount+9)%9] = inv.getItem(baseIndex+i);
        }
        for(int i=0; i<9; i++) {
            clickables.set(baseIndex+i, shiftedClickables[i]);
            inv.setItem(baseIndex+i, shiftedItemStacks[i]);
        }
    }

    /**
     * Shifts the entire column of a GUI by an amount. ItemStacks thrown off one end or the other will appear on
     * the opposite end of the GUI. If an invalid column number is passed, this function returns immediately.
     * @param column The column number (0-9)
     * @param amount The amount to shift by (-size to +size)
     */
    public void shiftColumn(int column, int amount) {
        int size = inv.getSize()/9;
        if(column < 0 || 9 <= column || amount < -size || 9 < size)
            return;


        GUIClickable[] shiftedClickables = new GUIClickable[size];
        ItemStack[] shiftedItemStacks = new ItemStack[size];
        for(int i=0; i<size; i++) {
            shiftedClickables[(i+amount+size)%size] = clickables.get(i*9+column);
            shiftedItemStacks[(i+amount+size)%size] = inv.getItem(i*9+column);
        }
        for(int i=0; i<size; i++) {
            clickables.set(i*9+column, shiftedClickables[i]);
            inv.setItem(i*9+column, shiftedItemStacks[i]);
        }
    }

    /**
     * Remove just the Clickable from a slot. Keeps the ItemStack.
     * @param slot The slot containing the Clickable to remove
     * @return The Clickable that was previously there (null if none)
     */
    @NotNull
    public GUIClickable removeClickable(int slot) {
        return clickables.remove(slot);
    }

    /**
     * Should we handle releasing the Listener for this object when all players
     * viewing it have closed the GUI? By default, yes. But this is where you
     * tell us not to do that if you'd like to reuse this GUI.
     * @param autoDestroy True, if the GUI should unregister when no one is looking at it.
     */
    public void setAutoDestroy(boolean autoDestroy) {
        this.autoDestroy = autoDestroy;
    }

    /**
     * Creates a callback that's called when the player (or server) closes this gui.
     * @param onCloseCallback The callback
     */
    public void setOnCloseCallback(Callback onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
    }

    /**
     * Creates a callback that's called when the gui is destroyed.
     * @param onDestroyCallback The callback
     */
    public void setOnDestroyCallback(Callback onDestroyCallback) {
        this.onDestroyCallback = onDestroyCallback;
    }

    /**
     * @return True, if the GUI has been destroyed.
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    /**
     * Release the Listener for this object. Render's this object useless and
     * unable to handle future GUI-related events. A new GUI will need to be
     * created. You should only be calling this if you've previously called
     * setAutoDestroy(false).
     */
    public void destroy() {
        HandlerList.unregisterAll(this);
        isDestroyed = true;
        if(onDestroyCallback != null)
            onDestroyCallback.call();
    }

    /*
     * Implementation for handling clicks and actions to the GUI
     * If getting random NPEs relating to viewers, try removing viewers on PlayerLeaveEvent
     * Shouldn't be needed since InventoryCloseEvent is fired first.
     */
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if(inv.equals(this.inv) && e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            Inventory clickedInv = e.getClickedInventory();
            e.setCancelled(true);
            if(clickedInv != null && viewers.contains(p) && clickedInv.equals(this.inv)) {
                // See https://www.spigotmc.org/threads/difference-between-getslot-and-getrawslot.365362/
                GUIClickable clicked = clickables.get(e.getRawSlot());
                if(clicked != null) {
                    GUIAction action = clicked.getActionOnClick(e.getClick());
                    if(action != null)
                        action.onClick(this, e);
                    else if(nonInteractionSound != null)
                        nonInteractionSound.playToPlayer(p, p.getLocation());
                } else if(e.getCurrentItem() != null && nonInteractionSound != null)
                    nonInteractionSound.playToPlayer(p, p.getLocation());
            } else if(e.getCurrentItem() != null && playerInvSound != null)
                playerInvSound.playToPlayer(p, p.getLocation());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if(e.getInventory().equals(inv) && e.getPlayer() instanceof Player) {
            Player p = (Player) e.getPlayer();
            if(viewers.remove(p)) {
                if(closeGUISound != null)
                    closeGUISound.playToPlayer(p, p.getLocation());
                if(onCloseCallback != null)
                    onCloseCallback.call();
                if(autoDestroy && viewers.size() == 0)
                    destroy();
            }
        }
    }
}
