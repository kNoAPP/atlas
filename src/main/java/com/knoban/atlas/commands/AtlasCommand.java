package com.knoban.atlas.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alden Bansemer (kNoAPP)
 */
@Deprecated
public abstract class AtlasCommand implements TabExecutor, Listener {

	private static final String NO_PERMISSION = ChatColor.GOLD + "Permission> " + ChatColor.GRAY + "You are missing permission " + ChatColor.DARK_AQUA + "%perm%" + ChatColor.GRAY + "!";
	private static final String USAGE = ChatColor.GOLD + "Try> " + ChatColor.GRAY + "%usage%";
	
	private CommandInfo info = getClass().getAnnotation(CommandInfo.class);
	private List<AtlasCommand> extensions = new ArrayList<>();
	private boolean root = false;

	public AtlasCommand() {}

	/**
	 * Base logic, not recommended to Override.
	 */
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		if(!(sender instanceof Player || sender instanceof ConsoleCommandSender))
			return null;
		
		List<String> suggestions = new ArrayList<>();
		if(root) {
			for(AtlasCommand ac : extensions) {
				List<String> ret = ac.onTabComplete(sender, command, alias, args);
				if(ret != null)
					suggestions.addAll(ret);
			}
		}
		
		if(!info.permission().equals("") && !sender.hasPermission(info.permission()))
			return suggestions.size() > 0 ? suggestions : null;
		
		
		Formation form = getFormation(sender);
		if(form.lastMatch(args) >= args.length - 2) {
			int type = form.getArgType(args.length - 1);
			switch(type) {
			case Formation.PLAYER:
				suggestions.addAll((sender instanceof Player ? form.getPlayer((Player) sender) : form.getPlayer()).stream().filter(s -> s.startsWith(args[args.length-1])).collect(Collectors.toList()));
				break;
			case Formation.NUMBER:
				suggestions.addAll(form.getNumber(args.length - 1).stream().filter(s -> s.startsWith(args[args.length-1])).collect(Collectors.toList()));
				break;
			case Formation.LIST:
				suggestions.addAll(form.getList(args.length - 1).stream().filter(s -> s.startsWith(args[args.length-1])).collect(Collectors.toList()));
				break;
			case Formation.STRING:
				suggestions.addAll(form.getString(args.length - 1).stream().filter(s -> s.startsWith(args[args.length-1])).collect(Collectors.toList()));
				break;
			case Formation.ENDING_STRING:
				suggestions.addAll(form.getEndingString().stream().collect(Collectors.toList()));
			default:
				break;
			}
		}
		return suggestions.size() > 0 ? suggestions : null;
	}

	/**
	 * Base logic, not recommended to Override.
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(!(sender instanceof Player || sender instanceof ConsoleCommandSender))
			return true;
		
		if(root)
			for(AtlasCommand ac : extensions)
				if(ac.onCommand(sender, command, label, args))
					return true;
		
		boolean permission = info.permission().equals("") || sender.hasPermission(info.permission());
		int lastMatchedArg = getFormation(sender).lastMatch(args);
		if(lastMatchedArg < args.length - 1 || args.length - 1 < info.argMatch()) {
			if(info.argMatch() <= lastMatchedArg && permission)
				alertUsage(sender, info.usage());
			return root;
		}
		
		if(!permission) {
			alertNoPermission(sender, info.permission());
			return root;
		}
		
		if(!validArgs(args.length)) {
			alertUsage(sender, info.usage());
			return root;
		}
		
		if(sender instanceof Player) {
			onCommand((Player) sender, label, args);
		} else {
			onCommand((ConsoleCommandSender) sender, label, args);
		}
		return true;
	}
	
	protected void alertNoPermission(@NotNull CommandSender sender, @NotNull String permission) {
		sender.sendMessage(NO_PERMISSION.replaceAll("%perm%", permission));
	}
	
	protected void alertUsage(@NotNull CommandSender sender, @NotNull String usage) {
		sender.sendMessage(USAGE.replaceAll("%usage%", usage));
	}
	
	private boolean validArgs(int passed) {
		for(int a : info.length()) {
			if((passed == a || a == -1) && passed > info.min() - 1)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Passed when a Player types a valid command that matches Formation and desired arg length.
	 * @param sender The Player who sent the command
	 * @param args The args of the command (guaranteed length by CommandInfo argMatch)
	 * @return Doesn't matter, gets ignored
	 */
	protected boolean onCommand(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
		sender.sendMessage(ChatColor.GOLD + "Warn> " + ChatColor.RED + "This command may only be run by the console.");
		return true; 
	}
	
	/**
	 * Passed when a ConsoleCommandSender types a valid command that matches Formation and desired arg length.
	 * @param sender The ConsoleCommandSender who sent the command
	 * @param args The args of the command (guaranteed length by CommandInfo argMatch)
	 * @return Doesn't matter, gets ignored
	 */
	protected boolean onCommand(@NotNull ConsoleCommandSender sender, @NotNull String label, @NotNull String[] args) {
		sender.sendMessage(ChatColor.GOLD + "Warn> " + ChatColor.RED + "This command may only be run by players.");
		return true; 
	}
	
	/**
	 * Use Formation.FormationBuilder to build a Formation. For constant Formations, use a private static final Formation.
	 * 
	 * Ex. new FormationBuilder().list("foo", "bar").player().number(5.5, 10, 0.5).string("potato", "tomato").build();
	 * list - REQUIRED to have one of these Strings.
	 * player - REQUIRED to include a player name. However, since a player may be offline, no argument checking occurs on the inputed String 
	 *          (Online players that the sender Player#canSee() will be recommended)
	 * number - REQUIRED to include a number (Double). Will create suggestions based on #number(low, high, step).
	 * string - OPTIONAL to have one of these Strings. (Strings other than suggestions may be passed)
	 * 
	 * @param sender A Player or ConsoleCommandSender who tab-completed or ran the command.
	 * @return A Formation with proper command structure.
	 */
	@NotNull
	protected abstract Formation getFormation(@NotNull CommandSender sender);
	
	public CommandInfo getInfo() {
		return info;
	}
	
	/**
	 * Registers the command to the plugin with an included Event Listener.
	 * 
	 * Command execution priority follows the following set of rules
	 *      1. Zero or ONLY ONE AtlasCommand instance will be executed (or "passed") per command request.
	 *      1. The first time an AtlasCommand is registered with a specific command label (e.g. /foobar),
	 *         it becomes a "command"
	 *      2. Following registrations of AtlasCommands with the same command label become "subcommands"
	 *      3. When subcommands are present, the parent command will have the lowest execution priority
	 *      4. Subcommand priority is determined by the order the commands were registered. First come, first served.
	 * @param plugin Plugin main class (the class instance with onEnable/onDisable, commonly passed as "this")
	 */
	public void registerCommandWithListener(@NotNull JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		registerCommand(plugin);
	}
	
	/**
	 * Registers the command to the plugin.
	 * 
	 * Command execution priority follows the following set of rules
	 *      1. Zero or ONLY ONE AtlasCommand instance will be executed (or "passed") per command request.
	 *      2. The first time an AtlasCommand is registered with a specific command label (e.g. /foobar),
	 *         it becomes a "command"
	 *      3. Following registrations of AtlasCommands with the same command label become "subcommands"
	 *      4. When subcommands are present, the parent command will have the lowest execution priority
	 *      5. Subcommand priority is determined by the order the commands were registered. First come, first served.
	 * @param plugin Plugin main class (the class instance with onEnable/onDisable, commonly passed as "this")
	 */
	public void registerCommand(@NotNull JavaPlugin plugin) {
		registerCommand(info.name(), info.aliases(), plugin);
	}
	
	private void registerCommand(@NotNull String cmd, @NotNull String[] aliases, @NotNull JavaPlugin plugin) {
		if(info == null)
			throw new UnsupportedOperationException("CommandInfo annotation is missing!");	
		
		PluginCommand pc = plugin.getCommand(cmd);
		if(pc == null) {
			try {
				Constructor<PluginCommand> cons = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
				cons.setAccessible(true);
				pc = cons.newInstance(cmd, plugin);
				
				pc.setAliases(Arrays.asList(aliases));
				pc.setDescription(info.description());
				pc.setUsage(info.usage());
				
				Field cmdMap = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                cmdMap.setAccessible(true);
                CommandMap map = (CommandMap) cmdMap.get(Bukkit.getPluginManager());

                map.register(plugin.getName(), pc);
			} catch (NoSuchMethodException | IllegalAccessException |
                    InstantiationException | InvocationTargetException |
                    NoSuchFieldException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Failed to load " + this.getClass().getSimpleName() + ": /" + cmd);
                return;
            }
			
			plugin.getLogger().info("Successfully loaded " + this.getClass().getSimpleName() + ": /" + cmd);
			for(String alias : aliases)
				plugin.getLogger().info("Successfully loaded " + this.getClass().getSimpleName() + " alias: /" + alias);
			
			pc.setExecutor(this);
			pc.setTabCompleter(this);
			root = true;
		} else if(pc.getExecutor() instanceof AtlasCommand) {
			plugin.getLogger().info("Successfully loaded " + this.getClass().getSimpleName() + " subcommand: /" + cmd);
			((AtlasCommand) pc.getExecutor()).extensions.add(this);
			
			//Recursive call to potentially register an alias as a command
			if(aliases.length > 1)
				registerCommand(aliases[0], Arrays.copyOfRange(aliases, 1, aliases.length), plugin);
		} else plugin.getLogger().warning("Command " + cmd + " has been registered by a non-Atlas command! Cannot register.");
	}
}
