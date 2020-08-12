package com.knoban.atlas.pm;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PrivateMessageEvent extends Event implements Cancellable {

    private Player to, from;
    private String message;
    private boolean cancel;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /**
     * Creates an event for alerting plugins that one player is messaging another privately.
     * @param to The player being sent a message
     * @param from The player sending the message
     * @param message The message
     */
    public PrivateMessageEvent(@NotNull Player to, @NotNull Player from, @NotNull String message) {
        this.to = to;
        this.from = from;
        this.message = message;
        this.cancel = false;
    }

    /**
     * @return The player the message is being sent to.
     */
    @NotNull
    public Player getTo() {
        return to;
    }

    /**
     * @return The player who sent the message.
     */
    @NotNull
    public Player getFrom() {
        return from;
    }

    /**
     * @return The message
     */
    @NotNull
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message to be sent privately between the two players.
     * @param message The message to be sent
     */
    public void setMessage(@NotNull String message) {
        this.message = message;
    }

    /**
     * @return True, if this private message will not be sent.
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Cancels the private message from being sent.
     * @param cancel True, if the message shouldn't be sent. False, if it should be sent.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
