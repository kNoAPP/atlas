package com.knoban.atlas.gui;

import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author Alden Bansemer (kNoAPP)
 */
public class GUIClickable {

    private HashMap<ClickType, GUIAction> actions = new HashMap<>();

    /**
     * Sets an action to occur when a particular click type occurs. For example,
     * a left click or a right click; a shift click or a CTRL click.
     * @param action The action that should be called
     * @param ct The list of ClickTypes that can trigger this action
     */
    public void setActionOnClick(@NotNull GUIAction action, @NotNull ClickType... ct) {
        if(ct.length == 0) {
            for(ClickType c : ClickType.values())
                actions.put(c, action);
        } else {
            for(ClickType c : ct)
                actions.put(c, action);
        }
    }

    /**
     * Get the action set for a particular ClickType.
     * @param ct The ClickType to search for
     * @return The action associated with the ClickType or null if none
     */
    @NotNull
    public GUIAction getActionOnClick(@NotNull ClickType ct) {
        return actions.get(ct);
    }
}
