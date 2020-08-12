package com.knoban.atlas.pm;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.knoban.atlas.utils.SoundBundle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This utility aids in providing support for private messaging on the server. It includes some commands, events,
 * message formatting, spying, and replies.
 *
 * @author Alden Bansemer (kNoAPP)
 */
public class PrivateMessagingManager {

    private JavaPlugin plugin;
    private String senderMsg, receiverMsg, spyMsg;
    private SoundBundle receiverPing;

    private Cache<UUID, UUID> replies = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES).build();

    private HashSet<UUID> spies = new HashSet<>();

    /**
     * Creates a PrivateMessageManager that registers the commands automatically: /msg, /pm, /w, /reply, /r, and /spy.
     * Also creates a {@link PrivateMessageEvent} that can be caught.
     * You must pass messaging formats here to the constructor using a few replaceable strings.
     * <br /><br />
     * %to% - The receiver's username of the message
     * <br />
     * %from% - The sender's username of the message
     * <br />
     * %msg% - The sender's message
     *
     * @param plugin
     * @param senderMsg
     * @param receiverMsg
     * @param spyMsg
     */
    public PrivateMessagingManager(@NotNull JavaPlugin plugin, @NotNull String senderMsg, @NotNull String receiverMsg,
                                   @Nullable SoundBundle receiverPing, @NotNull String spyMsg) {
        this.plugin = plugin;
        this.senderMsg = senderMsg;
        this.receiverMsg = receiverMsg;
        this.receiverPing = receiverPing;
        this.spyMsg = spyMsg;

        new MessageCommand(plugin, this).registerCommand(plugin);
        new ReplyCommand(plugin, this).registerCommand(plugin);
        new SpyCommand(this).registerCommand(plugin);
    }

    /**
     * Sends a private message from a {@link Player} to another. This does not call the
     * {@link PrivateMessageEvent}. The message will be visible to spies.
     * @param to The {@link Player} to send a private message to
     * @param from The {@link Player} thats sending the private message
     * @param msg The message
     * @return True if the message could be send. False if either player wasn't online.
     */
    public boolean sendPrivateMessage(@NotNull Player to, @NotNull Player from, @NotNull String msg) {
        if(to.isOnline() && from.isOnline()) {
            replies.put(to.getUniqueId(), from.getUniqueId());
            replies.put(from.getUniqueId(), to.getUniqueId());

            from.sendMessage(formatMessage(senderMsg, to.getName(), from.getName(), msg));
            to.sendMessage(formatMessage(receiverMsg, to.getName(), from.getName(), msg));
            if(receiverPing != null)
                receiverPing.playToPlayer(to, to.getLocation());
            String msgForSpy = formatMessage(spyMsg, to.getName(), from.getName(), msg);

            for(UUID spy : spies) {
                Player p = Bukkit.getPlayer(spy);
                if(p != null && p != to && p != from)
                    p.sendMessage(msgForSpy);
            }
            return true;
        } else
            return false;
    }

    /**
     * Replies with a private message from a {@link Player} to another. This does not call the
     * {@link PrivateMessageEvent}. The message will be visible to spies.
     * @param from The {@link Player} trying to reply with a private message
     * @param msg The message
     * @return True if the message could be send. False if there's no one they can reply to or their reply couldn't be
     * sent
     */
    public boolean replyToPrivateMessage(@NotNull Player from, @NotNull String msg) {
        UUID reply = replies.getIfPresent(from.getUniqueId());
        if(reply == null)
            return false;

        Player to = Bukkit.getPlayer(reply);
        if(to == null)
            return false;

        return sendPrivateMessage(to, from, msg);
    }

    /**
     * A helper method for parsing a formatted string
     */
    private String formatMessage(@NotNull String orig, @NotNull String to, @NotNull String from,
                                 @NotNull String msg) {
        return orig.replaceAll("%to%", to).replaceAll("%from%", from).replaceAll("%msg%", msg);
    }

    /**
     * Adds a spy that can see all private messages between players
     * @param uuid The uuid of the spy
     */
    public void addSpy(@NotNull UUID uuid) {
        spies.add(uuid);
    }

    /**
     * Removes a spy that can see all private messages between players
     * @param uuid The uuid of the spy
     */
    public void removeSpy(@NotNull UUID uuid) {
        spies.remove(uuid);
    }

    /**
     * Toggles spying for a spy that can see all private messages between players
     * @param uuid The uuid of the spy
     */
    public boolean toggleSpy(@NotNull UUID uuid) {
        if(spies.contains(uuid)) {
            spies.remove(uuid);
            return false;
        } else {
            spies.add(uuid);
            return true;
        }
    }

    /**
     * Gets the UUID of the {@link Player} that the passed {@link Player} UUID can reply to.
     * This may return null if there's no {@link Player} to reply to.
     * @param uuid The UUID of the {@link Player} to get the reply of
     * @return The UUID or null if there's no one to reply to
     */
    @Nullable
    public UUID getReplyUUID(@NotNull UUID uuid) {
        return replies.getIfPresent(uuid);
    }
}
