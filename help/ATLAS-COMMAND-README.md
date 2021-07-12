# Atlas Commands System
### Compatible with 1.7.10+
Most plugins require some form of command handling for when a player (or console) types a command. The Atlas Commands 
system is a full implementation for command handling that automatically handles command registration, argument checking 
(type, data, and length), permission checking, proper usage alerting, suggesting commands, and tab-filling partially 
typed commands. It removes the need for a static plugin.yml commands section implementation and will inject commands 
directly into the server when registered. **For this reason, do not register any AtlasCommand in your plugin.yml!**

In this README, we will cover the following topics:
- [AtlasCommand](https://github.com/GodComplexMC/godcomplex-core/blob/master/src/main/java/org/godcomplex/core/command/AtlasCommand.java)
- [CommandInfo](https://github.com/GodComplexMC/godcomplex-core/blob/master/src/main/java/org/godcomplex/core/command/CommandInfo.java)
- [Formation](https://github.com/GodComplexMC/godcomplex-core/blob/master/src/main/java/org/godcomplex/core/command/Formation.java)
- Console vs. Player handling
- Registration of AtlasCommand
- Execution Priority
- Examples

## AtlasCommand
Every `AtlasCommand` has three requirements for proper implementation. In your command class you must...
1. Extend `AtlasCommand`
2. Include a `CommandInfo` class annotation (see below)
3. Override and implement `AtlasCommand#getFormation(CommandSender)` which returns a `Formation` (see below)

To then enable the command on the server, be sure to register it (usually within the `onEnable` function in your Main 
plugin class). See below.

### Commands vs. Subcommands
The Atlas Commands system supports building both regular commands and subcommands. A subcommand is any command that is 
not uniquely defined by its base invocation. For example, `/boosters kNoAPP add 4` is a command, but 
`/salem boosters kNoAPP add 4` is a subcommand since I might use the base invocation again elsewhere like 
`/salem karma kNoAPP remove 18`. Below, the `CommandInfo` annotation `argMatch` can be used to identify the specific 
argument from which a command becomes unique. In our example, this is argument `0`.

## CommandInfo
The `CommandInfo` annotation is used to reference configuration for a command. The annotation is placed just above the 
class declaration and can contain several properties...
- `name` (required)
    > The command's base invocation.
    Example: `name = "salem"`
- `length` (optional)
    > An array listing acceptable amounts of arguments to run the command successfully. Passing a -1 or not including
    this argument signals the command takes unlimited args.
    Example: `length = {2, 4}`
- `min` (optional)
    > Similar to length. The command must have at least this many args. 
    Useful for commands that take unlimited args.
- `description` (highly recommended)
    > A helpful description of the command. Is viewed when /help is run.
    Example: `description = "Modify or view the boosters a player has"`
- `usage` (highly recommended)
    > The proper usage for a command. Is displayed if the command was typed incorrectly.
    Example: `usage = "/salem boosters <player> (add | remove) (amt)"`
- `aliases` (optional)
    > Acts in the same way as `name` but is optional. Will add additional base invocations for a command.
    Example: `aliases = {"game", "s", "manager"}`
- `permission` (optional)
    > The permission (if any) needed to run the command. If left blank, no permission is required to run the command.
    Example: `permission = "salem.boosters"`
- `argMatch` (optional)
    > Only display the usage message if the last matched argument is equal to or greater than this number. `0` indicates 
    the first argument. Commonly used only for subcommands (see above)
    Example: `argMatch = 0`


## Formation
A `Formation` is how you'll build out acceptable arguments and suggestions for players. Formations serve two purposes. 
First, they provide a format for how a command should be written. The Atlas Command system uses your `Formation` to 
verify proper arguments before passing you the command. It checks against type (`String` or `Double`) and `String` data. 
Second, it provides players with a list of suggestions prior to command execution. You'll provide these suggestions in 
the [Formation.FormationBuilder](https://github.com/GodComplexMC/godcomplex-core/blob/master/src/main/java/org/godcomplex/core/command/Formation.java).

### FormationBuilder
An instance of a `Formation` should be created using the `FormationBuilder`. Eclipse has a strange bug where it won't 
suggest to import the class since its nested in the `Formation` class. You can get around that with 
`import com.knoban.core.command.Formation.FormationBuilder;`. A FormationBuilder builds your Formation in a standard 
linear order. Here's an example of a proper `Formation`...

```java
private static final Formation FORM = new FormationBuilder().list("booster").player().list("add", "remove").number(-10, 10, 4).build();

@Override
protected Formation getFormation(CommandSender sender) {
	return FORM;
}
```

A `FormationBuilder` has the following functions you can use the generate the Formation. Each of these functions acts as
 a placeholder for an argument in the command.
- `player()`
    > Indicates this argument should be a player name. Will suggest online player's names. **Does not check** the passed 
               >argument since a offline player name could be passed.
- `number(double low, double high, double step)`
    > Indicates this argument should be a number. Will create suggestions based on the passed low, high, and step values. 
    Will trim off the decimal if the number is a whole number. 
    **Will check** if the passed argument is a Double.
- `list(String... data)`
    > Indicates this argument must be one of the following Strings. Will suggest the given Strings. **Will check** if 
    the passed argument is one of the Strings (ignoring case).
- `string(String... data)`
    > Indicates this argument could be one of the following Strings. Will suggest the given Strings. **Does not check** 
    if the passed argument is one of the Strings (ignoring case). This function is purely used 
    for recommending suggestions, so even a number could be passed.
- `endWithString(String... note)`
    > Indicates the command may have unlimited additional non-checked String arguments. You can pass a list of Strings
    to suggest in the arguments. This should only ever be called immediately before `build()`.

When you are ready to generate the command `Formation`, add a `build()` call to the end of the FormationBuilder. Pass the 
returned Formation to your overridden `getFormation(CommandSender)` function. It is recommended to use a `private static 
final Formation` if your Formation is not dependent on additional data. This way a new `Formation` is not built each 
function call. However, if(for example) your command is dependent on the CommandSender, you may use the passed 
`CommandSender sender` to generate a dynamic Formation for your `AtlasCommand`.

If a executed command does not meet the criteria of the Formation or does not match the `length` `CommandInfo` property, 
the command is not passed to your `AtlasCommand` and a usage command will be sent out if the `argMatch` `CommandInfo` 
property is met or isn't set.

## Console vs. Player handling
The Atlas Commands system is designed only to handle commands sent by the Console or by a Player. Commands written or 
sent from other blocks or entities are disregarded, but no exceptions are thrown. Since commands sometimes may only be 
run by a player or console, or commands must act differently when run from a player vs. console, the Atlas Commands 
system has been designed to pass commands on a case-by-case base. 

There are two ways you may fulfill your command with Atlas. For both of them, the `sender` is the origin of the command. 
The `label` is the command's base invocation. The `args` are an array of Strings representing the args of the command. 
You are not require to override **any** of these methods, but it wouldn't make sense to override less than one of them. 
By default, a message will be sent to the Player or Console letting them know the command is not valid for Players or 
Consoles if the related function is not overridden.

### Handling a Player
By overriding the `onCommand(Player sender, String label, String[] args)` method, commands typed by players are now 
passed to you if their length and `Formation` both match. The return boolean does not matter. Here's an example...

```java
@Override
public boolean onCommand(Player sender, String label, String[] args) {
	Mode m = Mode.getMode(args[1]); //Atlas ensures a correct mode is selected so a NPE check is not needed
	PlayerData pd = PlayerData.getPlayerData(sender.getUniqueId());
	pd.setMode(m);
	sender.sendMessage(Message.INFO.getMessage("Your mode has been switched to " + m.getName().toUpperCase() + "."));
	return true;
}
```

### Handling a Console
By overriding the `onCommand(ConsoleCommandSender sender, String label, String[] args)` method, commands typed by the 
Console are now passed to you if their length and `Formation` both match. The return boolean does not matter. Here's an 
example...

```java
@Override
public boolean onCommand(ConsoleCommandSender sender, String label, String[] args) {
	Game g = Game.getCurrent();
	sender.sendMessage(Message.INFO.getMessage(g.dev() ? "Oh man, you'd better be careful." : "Back to safety. Phew!"));
	return true;
}
```

## Registration of AtlasCommand
Once you've implemented AtlasCommand correctly, you need to make sure you register it in order to use it. By default, 
all AtlasCommands begin unregistered when the server starts. If you need to command to appear and be executable, call 
one of the following member functions...
- `AtlasCommand#registerCommand(JavaPlugin)`: EventListener is not registered
- `AtlasCommand#registerCommandWithListener(JavaPlugin)`: EventListener is registered

Once a command is registered, it will appear ingame until the server shuts down or is reloaded. AtlasCommands with 
the same `CommandInfo` `name` property as a previously registered command will be registered as a subcommand. Aliases 
remain unaffected and will register as a normal command if possible.

Here's an example of a proper registration of commands/subcommands:
```java
new SalemHelpCommand().registerCommand(this); //Base command - Should be registered first!
new SalemBoosterCommand().registerCommand(this);
new SalemClasspassCommand().registerCommand(this);
new SalemDevCommand().registerCommand(this);
new SalemDisableCommand().registerCommand(this);
new SalemHonorCommand().registerCommand(this);
new SalemModeCommand().registerCommand(this);
new SalemRemoveCommand().registerCommand(this);
new SalemRequireCommand().registerCommand(this);
new SalemResetCommand().registerCommand(this);
new SalemSelectCommand().registerCommandWithListener(this);
```

## Execution Priority
Command execution priority follows the following set of rules
1. **Zero or One** `AtlasCommand` instances will be executed (or "passed") per command request.
2. The first time an `AtlasCommand` is registered with a specific command label (e.g. /foobar), it becomes a "command"
3. Following registrations of AtlasCommands with the same command label become "subcommands"
4. When subcommands are present, the parent command will have the lowest execution priority
5. Subcommand priority is determined by the order the commands were registered. First come, first served.

## Examples
The following locations are great places to see the Atlas Commands system in action:
- [Salem](https://github.com/GodComplexMC/Salem/tree/master/src/main/java/com/kNoAPP/salem/commands)
- [OnTheRun](https://github.com/GodComplexMC/OnTheRun/tree/master/src/main/java/com/kNoAPP/ontherun/commands)
- [P3](https://github.com/GodComplexMC/P3/tree/master/src/main/java/com/kNoAPP/P3/commands)

### AtlasCommand acting in the capacity of a command
```java
@CommandInfo(name = "soundgen", aliases = {"sg"}, description = "Generate custom sounds", usage = "/soundgen (on | off)", length = {0, 1})
public class SoundGenCommand extends AtlasCommand {
	
	private static final Formation FORM = new FormationBuilder().list("on", "off").build();
	private List<UUID> soundgen = new ArrayList<UUID>();

	@Override
	public boolean onCommand(Player sender, String label, String[] args) {
		switch(args.length) {
		case 0:
			sender.sendMessage(Message.SOUNDGEN.getMessage("Flushing sounds..."));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stopsound " + sender.getName());
			return true;
		case 1:
			if(args[0].equalsIgnoreCase("on")) {
				if(!soundgen.contains(sender.getUniqueId())) {
					soundgen.add(sender.getUniqueId());
					sender.sendMessage(Message.SOUNDGEN.getMessage("On..."));
					new BukkitRunnable() {
						public void run() {
							if(sender != null && soundgen.contains(sender.getUniqueId()) && sender.isOnline()) {
								Sound s = Sound.values()[Tools.randomNumber(0, Sound.values().length-1)];
								while(s.name().contains("RECORD")) s = Sound.values()[Tools.randomNumber(0, Sound.values().length-1)];
								float pitch = (float) Tools.randomNumber(0.5, 2.0);
								
								sender.playSound(sender.getLocation(), s, 1F, pitch);
								sender.sendMessage(Message.SOUNDGEN.getMessage(s.name() + " - " + pitch));
							} else {
								soundgen.remove(sender.getUniqueId());
								this.cancel();
							}
						}
					}.runTaskTimer(Ultimates.getPlugin(), 20L, 30L);
				} else sender.sendMessage(Message.SOUNDGEN.getMessage("On already..."));
			} else if(args[0].equalsIgnoreCase("off")) {
				soundgen.remove(sender.getUniqueId());
				sender.sendMessage(Message.SOUNDGEN.getMessage("Off..."));
			}
		}
		return true;
	}

	@Override
	protected Formation getFormation(CommandSender sender) {
		return FORM;
	}
}
```

### AtlasCommand acting in the capacity of a subcommand
```java
@CommandInfo(name = "salem", description = "Set or freeze the Salem game timer.", usage = "/salem timer (time)", permission = "salem.timer", length = {1, 2}, argMatch = 0)
public class SalemTimerCommand extends AtlasCommand {

	private static final Formation FORM = new FormationBuilder().list("timer").number(30, 120, 30).build();
	
	@Override
	public boolean onCommand(Player sender, String label, String[] args) {
		Game g = Game.getCurrent();
		if(args.length == 1) {
			g.setFrozen(!g.isFrozen());
			sender.sendMessage(Message.INFO.getMessage("The timer has been toggled " + (g.isFrozen() ? "on." : "off.")));
		} else {
			g.setTime((int) Double.parseDouble(args[1]));
			sender.sendMessage(Message.INFO.getMessage("The timer has been updated."));
		}
		return true;
	}
	
	@Override
	public boolean onCommand(ConsoleCommandSender sender, String label, String[] args) {
		Game g = Game.getCurrent();
		if(args.length == 1) {
			g.setFrozen(!g.isFrozen());
			sender.sendMessage(Message.INFO.getMessage("The timer has been toggled " + (g.isFrozen() ? "on." : "off.")));
		} else {
			g.setTime((int) Double.parseDouble(args[1]));
			sender.sendMessage(Message.INFO.getMessage("The timer has been updated."));
		}
		return true;
	}
	
	@Override
	protected Formation getFormation(CommandSender sender) {
		return FORM;
	}
}
```

### AtlasCommand taking unlimited args
```java
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
```


