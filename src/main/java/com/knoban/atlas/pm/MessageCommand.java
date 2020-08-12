package com.knoban.atlas.pm;

import com.knoban.atlas.commands.AtlasCommand;
import com.knoban.atlas.commands.CommandInfo;
import com.knoban.atlas.commands.Formation;
import com.knoban.atlas.commands.Formation.FormationBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@CommandInfo(name = "msg", aliases = {"pm", "w"}, description = "Privately message a player",
		usage = "/msg <player> <message>", min = 2)
public class MessageCommand extends AtlasCommand {

	private static final Formation FORM = new FormationBuilder().player().endWithString("<msg>").build();

	private JavaPlugin plugin;
	private PrivateMessagingManager manager;

	public MessageCommand(JavaPlugin plugin, PrivateMessagingManager manager) {
		super();
		this.plugin = plugin;
		this.manager = manager;
	}

	@Override
	public boolean onCommand(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
		Player t = Bukkit.getPlayer(args[0]);
		if(t != null && sender.canSee(t)) {
			if(t != sender) {
				StringBuilder builder = new StringBuilder();
				for(int i = 1; i < args.length - 1; i++) {
					builder.append(args[i]);
					builder.append(" ");
				}
				builder.append(args[args.length - 1]);
				String msg = builder.toString();

				PrivateMessageEvent pme = new PrivateMessageEvent(t, sender, msg);
				plugin.getServer().getPluginManager().callEvent(pme);
				if(pme.isCancelled())
					return true;

				if(!manager.sendPrivateMessage(pme.getTo(), pme.getFrom(), pme.getMessage()))
					sender.sendMessage("§cThat player isn't online.");
			} else
				sender.sendMessage("§cYou cannot message yourself. §7§oGet some friends...");
		} else
			sender.sendMessage("§cThat player isn't online.");
		return true;
	}

	@NotNull
	@Override
	protected Formation getFormation(@NotNull CommandSender sender) {
		return FORM;
	}
}
