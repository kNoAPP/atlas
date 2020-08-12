package com.knoban.atlas.updater;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

/**
 * @author Alden Bansemer (kNoAPP)
 *
 * While it seemed like a good idea at first, using GitHub commits as a way of storing and pushing code to
 * production is not a great practice. We should instead look to use Jenkins or another CI solution. There is nothing
 * programmatically wrong with this class though. You can use it to pull from Git.
 */
@Deprecated
public class Updater {

    private JavaPlugin plugin;
    private String remote, branch, repoPath, repoFile;
    private Thread updater;
    private volatile boolean updating;

    private String ongoing, begin, ready, foundButFail, failed;

    private File key;
    private String password;

    private File update;

    public Updater(JavaPlugin plugin, String remote, String branch, String repoPath, String repoFile,
                   String ongoing, String begin, String ready, String foundButFail, String failed) {
        this(plugin, remote, branch, repoPath, repoFile, null, null, ongoing, begin, ready, foundButFail, failed);
    }

    public Updater(JavaPlugin plugin, String remote, String branch, String repoPath, String repoFile, File key, String password,
                   String ongoing, String begin, String ready, String foundButFail, String failed) {
        this.plugin = plugin;
        this.remote = remote;
        this.branch = branch;
        this.repoPath = repoPath;
        this.repoFile = repoFile;

        this.ongoing = ongoing;
        this.begin = begin;
        this.ready = ready;
        this.foundButFail = foundButFail;
        this.failed = failed;

        if(key != null && password != null) {
            this.key = key;
            this.password = password;
        }
    }

    public void update(CommandSender requester) {
        update(requester, branch);
    }

    public void update(CommandSender requester, String branch) {
        if(updating) {
            requester.sendMessage(ongoing);
            return;
        }
        updating = true;
        requester.sendMessage(begin);

        updater = new Thread(() -> {
            File cloneDir = new File(plugin.getDataFolder(), "update");
            if(!reset(cloneDir, requester)) {
                updating = false;
                return;
            }
            cloneDir.mkdirs();

            try {
                GitService gs = key != null && key.exists() ? new GitService(cloneDir, key, password) : new GitService(cloneDir);
                gs.clone(remote, branch);
                File update = new File(cloneDir + repoPath, repoFile);
                if(update.exists()) {
                    FileUtils.moveFileToDirectory(update, plugin.getDataFolder(), false);
                    this.update = new File(plugin.getDataFolder(), update.getName());
                    requester.sendMessage(ready);
                } else
                    requester.sendMessage(foundButFail);
            } catch(GitAPIException e) {
                requester.sendMessage(failed);
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + plugin.getName() + "] Failed to pull from git: " + e.getMessage());
            } catch(IOException e) {
                requester.sendMessage(failed);
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + plugin.getName() + "] Was able to download update file, but cannot stage it: " + e.getMessage());
            }

            reset(cloneDir, requester);
            updating = false;
        });
        updater.start();
    }

    private boolean reset(File cloneDir, CommandSender requester) {
        if(cloneDir.exists()) {
            try {
                FileUtils.deleteDirectory(cloneDir);
            } catch (IOException e) {
                requester.sendMessage(failed);
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + plugin.getName() + "] Failed to delete update folder: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public void install() {
        if(updater != null && updater.isAlive()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + plugin.getName() + "] Waiting on updater to finish before restarting...");
            try {
                updater.join();
            } catch(InterruptedException e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + plugin.getName() + "] Failed to wait on updater: " + e.getMessage());
                return;
            }
        }

        if(update == null || !update.exists())
            return;

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[" + plugin.getName() + "] Update found! Installing...");
        try {
            File to = new File(plugin.getDataFolder().getParentFile(), update.getName());
            if(to.exists())
                FileUtils.forceDelete(to);

            FileUtils.moveFileToDirectory(update, to.getParentFile(), false);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[" + plugin.getName() + "] Update successfully installed.");
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + plugin.getName() + "] Failed to install update: " + e.getMessage());
        }
    }
}
