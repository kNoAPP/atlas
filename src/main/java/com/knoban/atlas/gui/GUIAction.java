package com.knoban.atlas.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Define an action to be run in conjunction with a GUI Clickable. Usually,
 * you will want to use this class in a lambda expression.
 *
 * @author Alden Bansemer (kNoAPP)
 */
public interface GUIAction {

    /**
     * The action to be run with a few given helper arguments.
     * @param gui The GUI this action was called from
     * @param e The InventoryClickEvent that triggered this GUIAction
     */
    void onClick(@NotNull GUI gui, @NotNull InventoryClickEvent e);
}
