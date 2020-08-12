package com.knoban.atlas.pm;

import com.knoban.atlas.commands.AtlasCommand;
import com.knoban.atlas.commands.CommandInfo;
import com.knoban.atlas.commands.Formation;
import com.knoban.atlas.commands.Formation.FormationBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@CommandInfo(name = "spy", aliases = {}, description = "Toggle spying on player messages",
		usage = "/spy", length = {0}, permission = "atlas.pm.spy")
public class SpyCommand extends AtlasCommand {

	private static final Formation FORM = new FormationBuilder().build();

	private JavaPlugin plugin;
	private PrivateMessagingManager manager;

	public SpyCommand(PrivateMessagingManager manager) {
		super();
		this.manager = manager;
	}

	@Override
	public boolean onCommand(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
		if(manager.toggleSpy(sender.getUniqueId()))
			sender.sendMessage("§aYou are now spying on player messages.");
		else
			sender.sendMessage("§cYou are no longer spying on player messages.");
		return true;
	}

	@NotNull
	@Override
	protected Formation getFormation(@NotNull CommandSender sender) {
		return FORM;
	}
}
