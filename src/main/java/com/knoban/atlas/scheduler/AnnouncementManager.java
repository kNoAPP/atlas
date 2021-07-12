package com.knoban.atlas.scheduler;

import com.knoban.atlas.Atlas;
import com.knoban.atlas.data.local.DataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class for sending announcements periodically. Initially will import from the announcements.yml file.
 * Can be modified with more or less messages during runtime. Use {@code AnnouncementManager.getAnnouncementManager()}
 *
 * @author Alden Bansemer (kNoAPP)
 */
public final class AnnouncementManager {

    private static final AnnouncementManager MANAGER = new AnnouncementManager(Atlas.getInstance());

    private Plugin plugin;
    private BukkitTask loop;
    private List<String> announcements = new ArrayList<>();
    private int nextAnnouncementIndex;
    private boolean isEnabled;
    private long interval;

    /**
     * Singleton constructor for making the AnnouncementManager
     * @param plugin An instance of the registering plugin
     */
    private AnnouncementManager(@NotNull Plugin plugin) {
        this.plugin = plugin;

        FileConfiguration yml = new DataHandler.YML(plugin, "/announcements.yml").getCachedYML();
        announcements.addAll(yml.getStringList("announcements"));

        isEnabled = yml.getBoolean("enabled");
        interval = yml.getLong("interval");

        loop = Bukkit.getServer().getScheduler().runTaskLater(plugin, this::announcementLoop, interval);
        if(!isEnabled)
            loop.cancel();
    }

    /**
     * Helper method to announce and update the bounds for the next announcement.
     */
    private void announcementLoop() {
        // Prepare next call to this function
        loop = Bukkit.getServer().getScheduler().runTaskLater(plugin, this::announcementLoop, interval);

        // Edge case: did someone modify our announcement list? Are we out of bounds now?
        if(nextAnnouncementIndex >= announcements.size())
            nextAnnouncementIndex = 0;

        String[] announcement = announcements.get(nextAnnouncementIndex).split("%n%");
        for(String msg : announcement)
            Bukkit.broadcastMessage(msg);

        // Update nextAnnouncement
        nextAnnouncementIndex = (nextAnnouncementIndex + 1) % announcements.size();
    }

    /**
     * Gets a list of all announcements that will be sent in a loop (one after the other).
     * @return The list of announcements
     */
    @NotNull
    public List<String> getAnnouncements() {
        return announcements;
    }

    /**
     * Gets the next announcement that will be broadcasted on the server
     * @return The next announcement or null if no messages are registered
     */
    @Nullable
    public String getNextAnnouncement() {
        if(nextAnnouncementIndex >= announcements.size())
            nextAnnouncementIndex = 0;

        if(announcements.size() == 0)
            return null;

        return announcements.get(nextAnnouncementIndex);
    }

    /**
     * @return True, if this tool is enabled and broadcasting messages. Will return true even if there's no messages to
     * broadcast.
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Enable or disable this announcer. When disabled, no messages are sent on the server.
     * @param enabled True, if messages should be sent. False, if not.
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;

        if(!isEnabled) {
            if(!loop.isCancelled())
                loop.cancel();
        } else {
            if(loop.isCancelled())
                loop = Bukkit.getServer().getScheduler().runTaskLater(plugin, this::announcementLoop, interval);
        }
    }

    /**
     * @return The interval between broadcasted announcements
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Set the interval between broadcasted announcements.
     * @param interval The interval between messages
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * Get the single-ton instance of the AnnouncementManager. The initialization function needs to be called
     * at least once before this can be called.
     * @return The AnnouncementManager or null if initialize() has not been called once
     */
    @Nullable
    public static AnnouncementManager getAnnouncementManager() {
        return MANAGER;
    }
}
