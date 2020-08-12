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

import java.util.UUID;

@CommandInfo(name = "r", aliases = {"reply"}, description = "Privately reply to a player",
		usage = "/r <message>", min = 1)
public class ReplyCommand extends AtlasCommand {

	private static final Formation FORM = new FormationBuilder().player().endWithString("<msg>").build();

	private JavaPlugin plugin;
	private PrivateMessagingManager manager;

	public ReplyCommand(JavaPlugin plugin, PrivateMessagingManager manager) {
		super();
		this.plugin = plugin;
		this.manager = manager;
	}

	@Override
	public boolean onCommand(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
		UUID replyTo = manager.getReplyUUID(sender.getUniqueId());
		if(replyTo != null) {
			Player t = Bukkit.getPlayer(replyTo);
			if(t != null) { // Don't check for canSee here since replies should occur if the admin initiates contact.
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
					sender.sendMessage("§cThat player is no longer online.");
			} else
				sender.sendMessage("§cThat player is no longer online.");
		} else
			sender.sendMessage("§cYou don't have anyone to reply to.");
		return true;
	}

	@NotNull
	@Override
	protected Formation getFormation(@NotNull CommandSender sender) {
		return FORM;
	}
}
