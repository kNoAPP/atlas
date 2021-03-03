package com.knoban.atlas.world;

import com.knoban.atlas.commandsII.ACAPI;
import com.knoban.atlas.commandsII.annotations.AtlasCommand;
import com.knoban.atlas.commandsII.annotations.AtlasParam;
import com.knoban.atlas.utils.Message;
import com.knoban.atlas.utils.Tools;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class ChunkCommandHandle {

    public static final String PERMISSION = "atlas.chunk";
    private final JavaPlugin plugin;

    public ChunkCommandHandle(JavaPlugin plugin) {
        this.plugin = plugin;

        ACAPI api = ACAPI.getApi();
        api.registerCommandsFromClass(plugin, ChunkCommandHandle.class, this);
    }

    @AtlasCommand(paths = {"chunk", "chunk help", "chunk help 1"}, permission = PERMISSION)
    public void cmdChunkBase(CommandSender sender) {
        sender.sendMessage(Message.INFO.getMessage("Chunk Help"));
        sender.sendMessage(ChatColor.DARK_GREEN + "------------------");
        sender.sendMessage(Message.HELP.getMessage("/chunk - Show all commands for chunks"));
        sender.sendMessage(Message.HELP.getMessage("/chunk preload <block diameter> - Preload chunks"));
        sender.sendMessage(Message.HELP.getMessage("/chunk status - Check chunk loading status"));
        sender.sendMessage(Message.HELP.getMessage("/chunk cancel - Cancel the ongoing task"));
    }

    private final HashMap<UUID, ChunkGenerationTask> generations = new HashMap<>();

    @AtlasCommand(paths = {"chunk preload"}, permission = PERMISSION)
    public void cmdChunkPreload(Player sender, @AtlasParam(filter = "min:0") int diameter) {
        cmdChunkPreload(sender, diameter, false);
    }

    @AtlasCommand(paths = {"chunk preload"}, permission = PERMISSION, classPriority = 1)
    public void cmdChunkPreload(Player sender, @AtlasParam(filter = "min:0") int diameter, boolean loud) {
        World world = sender.getWorld();
        Chunk center = sender.getChunk();
        UUID uuid = sender.getUniqueId();

        ChunkGenerationTask chunkGenerationTask = generations.get(uuid);
        if(chunkGenerationTask != null && chunkGenerationTask.getPercentComplete() < 1.0f) {
            sender.sendMessage("§cA chunk task is already ongoing! §7Try /chunk cancel.");
            return;
        }

        // Divide 16 for blocks -> chunks. Divide 2 for diameter -> radius
        final int radiusChunks = diameter/32;
        chunkGenerationTask = new ChunkGenerationTask(world, center.getX(), center.getZ(), radiusChunks);
        chunkGenerationTask.setLoud(loud);
        chunkGenerationTask.setCallback(() -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null && player.isOnline()) {
                player.sendMessage("§2Good news! §7The chunk generation has completed.");
                player.playSound(player.getLocation(), Sound.ITEM_LODESTONE_COMPASS_LOCK, 1f, 1f);
            }
            generations.remove(uuid);
        });
        generations.put(uuid, chunkGenerationTask);
        chunkGenerationTask.start(plugin);

        sender.sendMessage("§aBeginning chunk generation of §2" + diameter*diameter + " blocks§a!");
        sender.sendMessage("§eThis operation will complete in §6~" + Tools.millisToDHMS(chunkGenerationTask.getTimeLeftInMillis()) + "§e.");
    }

    @AtlasCommand(paths = {"chunk status"}, permission = PERMISSION)
    public void cmdChunkStatus(Player sender) {
        ChunkGenerationTask chunkGenerationTask = generations.get(sender.getUniqueId());
        if(chunkGenerationTask == null || chunkGenerationTask.getPercentComplete() >= 1.0f) {
            sender.sendMessage("§6Status: §2Complete!");
            return;
        }
        sender.sendMessage("§6Status: §a" + String.format("%.2f", chunkGenerationTask.getPercentComplete()*100f) + "% §7- §5" + Tools.millisToDHMS(chunkGenerationTask.getTimeLeftInMillis()));
    }

    @AtlasCommand(paths = {"chunk cancel"}, permission = PERMISSION)
    public void cmdChunkCancel(Player sender) {
        ChunkGenerationTask chunkGenerationTask = generations.get(sender.getUniqueId());
        if(chunkGenerationTask == null || chunkGenerationTask.getPercentComplete() >= 1.0f) {
            sender.sendMessage("§cNo ongoing task to cancel.");
            return;
        }
        sender.sendMessage("§aThe current task has been cancelled!");
        chunkGenerationTask.cancel();
        generations.remove(sender.getUniqueId());
    }

    /**
     * Pre-generates chunks to improve server speed post-generation.
     * @author Alden Bansemer (kNoAPP)
     */
    private static final class ChunkGenerationTask extends BukkitRunnable {

        private final World world;
        private final int radius;
        private final int centerX, centerZ;
        private int i, j, dx, dz;
        private long start, finish, overheadTPS;
        private boolean loud;

        private Runnable callback;

        /**
         * Generate part of a world's chunks in an async matter. Improves server speed in areas where pre-generation
         * has occurred.
         * @param world The world subject to generation
         * @param centerX The x-coordinate of the center of the generation
         * @param centerZ The z-coordinate of the center of the generation
         * @param radius The number of chunks in radius around the center to generate
         */
        private ChunkGenerationTask(@NotNull World world, int centerX, int centerZ, int radius) {
            this.world = world;
            this.radius = radius;
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.i = this.j = 0;
            this.dx = -radius;
            this.dz = -radius;
            this.loud = false;
            this.start = this.finish = System.currentTimeMillis();
        }

        @Override
        public void run() {
            finish = System.currentTimeMillis();
            // Recalculate how behind the TPS is (max 30s).
            overheadTPS = Math.max(0, Math.min(30000, overheadTPS + (finish - start) - (50 + 10))); // 50ms each tick, 10ms extra forgiveness

            start = System.currentTimeMillis();
            // Skip a cycle if the server is behind, but guarantee one chunk of progress every 15s minimum.
            if(overheadTPS < 50 || i%300 == 0) {
                if(dx >= radius) {
                    if(loud)
                        Bukkit.getConsoleSender().sendMessage("§e[ACG] §7Finished generation! Saving world...");
                    world.save();
                    this.cancel();

                    if(callback != null)
                        callback.run();
                    return;
                }

                if(dz >= radius) {
                    dz = -radius;
                    ++dx;
                }

                Chunk c = world.getChunkAt(centerX+(dx*16),centerZ+(dz*16)); // Generate chunk
                if(loud)
                    Bukkit.getConsoleSender().sendMessage("§e[ACG] §7Generating chunk (" + c.getX() + ", " + c.getZ() + ")...");
                c.load(true);
                c.unload(true);
                if(j % 100 == 0) { // Save world every 100 chunks just in case.
                    if(loud)
                        Bukkit.getConsoleSender().sendMessage("§e[ACG] §7Checkpoint reached ("
                                + String.format("%.2f", getPercentComplete()*100f)
                                + "% complete). Saving world...");
                    world.save();
                }

                ++dz;
                ++j;
            }

            ++i;
        }

        /**
         * This callback will be made on the game thread when chunk generation completes. The callback will
         * not be made if the task is cancelled.
         * @param callback The callback to make
         */
        public void setCallback(@Nullable Runnable callback) {
            this.callback = callback;
        }

        /**
         * Start generating chunks (1 per tick). This may only be called once
         * @param pl The plugin scheduling the task
         */
        public void start(@NotNull Plugin pl) {
            start = System.currentTimeMillis();
            i = j = 0;
            this.runTaskTimer(pl, 0L, 1L);
        }

        /**
         * Loud chunk generation reports each individual chunk to the console when generated. This allows
         * a user to track progress in console, but could be considered spammy.
         * @param loud True, if details should be printed to console.
         */
        public void setLoud(boolean loud) {
            this.loud = loud;
        }

        /**
         * @return A percentage 0.0f to 1.0f of how complete the task is
         */
        public float getPercentComplete() {
            return (float)j / (4f* radius * radius);
        }

        /**
         * @return The number of milliseconds projected to remain in the task
         */
        public long getTimeLeftInMillis() {
            return (4L*radius*radius - j) * 50L;
        }
    }
}
