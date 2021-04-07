package com.knoban.atlas.utils;

import com.knoban.atlas.world.Coordinate;
import joptsimple.internal.Strings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Alden Bansemer (kNoAPP)
 */
public class Tools {

    public static List<Block> blocksFromTwoPoints(Location loc1, Location loc2) {
    	List<Block> blocks = new ArrayList<Block>();
    	if(loc1.getWorld() == null || loc2.getWorld() == null) {
    		return blocks;
    	}
    	//if(!loc1.getChunk().isLoaded()) loc1.getChunk().load();
    	//if(!loc2.getChunk().isLoaded()) loc2.getChunk().load();
    	
        int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
        int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
 
        int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
        int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
 
        int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
        int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
 
        for(int x = bottomBlockX; x <= topBlockX; x++) {
            for(int z = bottomBlockZ; z <= topBlockZ; z++) {
                for(int y = bottomBlockY; y <= topBlockY; y++) {
                	//Location l = new Location(loc1.getWorld(), x, y, z);
                	//if(!l.getChunk().isLoaded()) l.getChunk().load();
                    Block block = loc1.getWorld().getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }
    
    public static boolean isWithin(Location t, Location loc1, Location loc2) {
    	return t.getWorld() != null && loc1.getWorld() != null && loc2.getWorld() != null &&
    			t.getWorld().getName().equals(loc1.getWorld().getName()) &&
    			t.getWorld().getName().equals(loc2.getWorld().getName()) &&
    			Math.min(loc1.getX(), loc2.getX()) <= t.getX() && t.getX() <= Math.max(loc1.getX(), loc2.getX()) &&
    			Math.min(loc1.getY(), loc2.getY()) <= t.getY() && t.getY() <= Math.max(loc1.getY(), loc2.getY()) &&
    			Math.min(loc1.getZ(), loc2.getZ()) <= t.getZ() && t.getZ() <= Math.max(loc1.getZ(), loc2.getZ());
    }
    
    public static void broadcastSound(Sound s, Float v, Float p) {
    	for(Player pl : Bukkit.getOnlinePlayers()) {
    		pl.playSound(pl.getLocation(), s, v, p);
    	}
    }

	/**
	 * Ignores the Y-Axis!
	 * @param b1 - The 1st lower Coordinate.
	 * @param t1 - The 1st upper Coordinate.
	 * @param b2 - The 2nd lower Coordinate.
	 * @param t2 - The 2nd upper Coordinate.
	 * @return True, if the bounding boxes intersect.
	 */
	public static boolean intersects(Coordinate b1, Coordinate t1, Coordinate b2, Coordinate t2) {
		return b1.getWorldName().equals(b2.getWorldName()) && t1.getZ() >= b2.getZ() && t1.getX() >= b2.getX() && b1.getZ() <= t2.getZ() && b1.getX() <= t2.getX();
	}
    
	public static void clearFullInv(Player p) {
		p.getInventory().clear();
		p.getInventory().setBoots(new ItemStack(Material.AIR, 1));
		p.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
		p.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
		p.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
		
		for(PotionEffect pe : p.getActivePotionEffects()) p.removePotionEffect(pe.getType());
	}
	
	public static int randomNumber(int min, int max) {
		Random rand = new Random();
		int val = rand.nextInt(max - min + 1) + min;
		return val;
	}
	
	public static String timeOutput(int timeInSeconds) {
	    int secondsLeft = timeInSeconds;
	    int minutes = secondsLeft / 60;
	    secondsLeft = secondsLeft - minutes * 60;
	    int seconds = secondsLeft;

	    String formattedTime = "";
	    //if(minutes < 10)
	       //formattedTime += "0";
	    formattedTime += minutes + ":";

	    if(seconds < 10)
	        formattedTime += "0";
	    formattedTime += seconds ;

	    return formattedTime;
	}
	
	public static Firework launchFirework(Location l, Color c, int power) {
		Firework fw = (Firework) l.getWorld().spawn(l, Firework.class);
		FireworkMeta data = fw.getFireworkMeta();
		data.setPower(power);
		data.addEffects(new FireworkEffect[]{FireworkEffect.builder().withColor(c).withColor(c).withColor(c).with(FireworkEffect.Type.BALL_LARGE).build()});
		fw.setFireworkMeta(data);
		return fw;
	}
	
	public static List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = new ArrayList<Block>();
        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                   blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }
	
	public static Vector subtractVectors(Location from, Location to) {
		Vector fromV = new Vector(from.getX(), from.getY(), from.getZ());
		Vector toV  = new Vector(to.getX(), to.getY(), to.getZ());
		 
		Vector vector = toV.subtract(fromV);
		return vector;
	}
	
	public static Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if(lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }
	
	public static boolean convertBoolean(int i) {
		if(i == 0) return false;
		else return true;
	}
	
	public static int convertInt(boolean b) {
		if(b) return 1;
		else return 0;
	}
	
	public static FileConfiguration getYML(File f) {
		FileConfiguration fc = new YamlConfiguration();
		try {
			fc.load(f);
		} catch (Exception e) {
			return null;
		}
		return fc;
	}
	
	public static boolean saveYML(FileConfiguration fc, File f) {
		try {
			fc.save(f);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public static void sendTitle(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
		try {
			Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
					.invoke(null, "{\"text\": \"" + title + "\"}");
			Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
					getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
					int.class, int.class, int.class);
			Object packet = titleConstructor.newInstance(
					getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle,
					fadeInTime, showTime, fadeOutTime);

			Object chatsTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
					.invoke(null, "{\"text\": \"" + subtitle + "\"}");
			Constructor<?> stitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
					getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
					int.class, int.class, int.class);
			Object spacket = stitleConstructor.newInstance(
					getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null),
					chatsTitle, fadeInTime, showTime, fadeOutTime);

			sendPacket(player, packet);
			sendPacket(player, spacket);
		} catch (Exception ex) {
		}
	}

	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		} catch (Exception ex) {}
	}

	/**
	 * @deprecated Use {@link Player#sendActionBar(String)}
	 * @param player The player to send the actionbar to.
	 * @param msg The message to send.
	 */
	public static void actionbarMessage(Player player, String msg) {
		player.sendActionBar(msg);
	}
	
	public static void adjustDamage(LivingEntity le, float dmg) {
		if(le instanceof Player) {
			Player p = (Player) le;
			try {
				//((EntityPlayer)p).damageEntity(DamageSource.GENERIC, dmg);
				Object handle = p.getClass().getMethod("getHandle").invoke(p);
				handle.getClass().getMethod("damageEntity", getNMSClass("DamageSource"), float.class).invoke(handle, getNMSClass("DamageSource").getField("GENERIC").get(null), dmg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else le.damage(dmg);
	}

	public static Class<?> getNMSClass(String name) {
		try {
			return Class.forName("net.minecraft.server." + getVersion() + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	}
	
	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}
	
	public static double round(double value, int precision) {
	    int scale = (int)Math.pow(10, precision);
	    return (double)Math.round(value * scale)/scale;
	}
	
	public static Object[] canParseToInteger(String s) {
		try {
			int i = Integer.parseInt(s);
			return new Object[]{true, i};
		} catch(NumberFormatException ex) {
			return new Object[]{false, 0};
		}
	}
	
	public static Object[] canParseToDouble(String s) {
		try {
			double d = Double.parseDouble(s);
			return new Object[]{true, d};
		} catch(NumberFormatException ex) {
			return new Object[]{false, 0.0};
		}
	}
	
	public static double randomNumber(double min, double max) {
	    Random r = new Random();
	    return (r.nextInt((int)((max-min)*10+1))+min*10) / 10.0;
	}
	
	public static Firework instantFirework(FireworkEffect fe, Location loc) {
        Firework f = (Firework) loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        fm.addEffect(fe);
        f.setFireworkMeta(fm);
        try {
            Class<?> entityFireworkClass = getClass("net.minecraft.server.", "EntityFireworks");
            Class<?> craftFireworkClass = getClass("org.bukkit.craftbukkit.", "entity.CraftFirework");
            Object firework = craftFireworkClass.cast(f);
            Method handle = firework.getClass().getMethod("getHandle");
            Object entityFirework = handle.invoke(firework);
            Field expectedLifespan = entityFireworkClass.getDeclaredField("expectedLifespan");
            Field ticksFlown = entityFireworkClass.getDeclaredField("ticksFlown");
            ticksFlown.setAccessible(true);
            ticksFlown.setInt(entityFirework, expectedLifespan.getInt(entityFirework) - 1);
            ticksFlown.setAccessible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return f;
    }
   
    private static Class<?> getClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }
    
    public static void projTrail(Plugin pl, Projectile proj, Particle[] particles) {
		new BukkitRunnable() {
			public void run() {
				if(proj != null && proj.isValid() && !proj.isDead()) 
					for(Particle particle : particles) 
						proj.getWorld().spawnParticle(particle, proj.getLocation(), 1, 0F, 0F, 0F, 0.01);
				else this.cancel();
			}
		}.runTaskTimer(pl, 0L, 3L);
	}
    
    /*
	@SuppressWarnings("deprecation")
	public static ItemStack createPotion(PotionType pt, int level) {
		Potion pot = new Potion(pt, level, false);
		pot.setSplash(false);
		return pot.toItemStack(1);
	}
	*/
    
	/**
	 * A generic version of the selection sort algorithm. Here we can sort any type T that
	 * implements the Comparable interface.
	 * @param <T> The type of array to be sorted.
	 * @param arr The array to be sorted.
	 */
	public static <T extends Comparable<T>> void selectionSort(T[] arr) {
		for(int i=0; i<arr.length-1; ++i) {
			int minIndex = i;
			for(int j=i+1; j<arr.length; ++j) if(arr[j].compareTo(arr[minIndex]) < 0) minIndex = j;
			// String changed to T
			T temp = arr[i];
			arr[i] = arr[minIndex];
			arr[minIndex] = temp;
		}
	}
	
	public static ItemStack buildPotion(Material m, PotionEffectType type, int ticks, int pow) {
		ItemStack is = new ItemStack(m);
		PotionMeta pm = (PotionMeta) is.getItemMeta();
		pm.addCustomEffect(new PotionEffect(type, ticks, pow), true);
		pm.setColor(type.getColor());
		if(m == Material.POTION) pm.setDisplayName(ChatColor.YELLOW + "Potion");
		if(m == Material.SPLASH_POTION) pm.setDisplayName(ChatColor.YELLOW + "Splash Potion");
		is.setItemMeta(pm);
		return is;
	}
	
	public static ItemStack buildEnchantment(Enchantment ench, int level, boolean ignoreLevelRestriction) {
		ItemStack is = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta esm = (EnchantmentStorageMeta) is.getItemMeta();
		esm.addStoredEnchant(ench, level, ignoreLevelRestriction);
		is.setItemMeta(esm);
		return is;
	}
	
	/**
	 * 75% - [+++++++++++++++-----] where + is left and - is right.
	 * @param prct Percent the meter should be filled
	 * @param total Total amount of
	 * @param leftColor ChatColor for left side
	 * @param leftCharacter Character for left side
	 * @param rightColor ChatColor for right side
	 * @param rightCharacter Character for right side
	 * @return The generated wait-bar String
	 */
	public static String generateWaitBar(double prct, double total, ChatColor leftColor, char leftCharacter,
										 ChatColor rightColor, char rightCharacter) {
		StringBuilder sb = new StringBuilder();
		double i;

		sb.append(leftColor);
		for(i=0; i/total <= prct && i<total; i++) {
			sb.append(leftCharacter);
		}
		sb.append(rightColor);
		for(; i<total; i++) {
			sb.append(rightCharacter);
		}

		return sb.toString();
	}

	public static Block floor(Block b) {
		while(b.getType() == Material.AIR) {
			b = b.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ());
		}
		return b;
	}

	public static List<String> formatLore(String lore, int maxCharsPerLine) {
		return formatLore(lore, maxCharsPerLine, null);
	}

	public static List<String> formatLore(String lore, int maxCharsPerLine, ChatColor color) {
		List<String> formatted = new ArrayList<String>();
		if(lore == null)
			return formatted;

		String[] words = lore.split("\\s+");
		StringBuilder line = new StringBuilder();
		if(color != null)
			line.append(color);
		for(int i=0; i<words.length; i++) {
			String word = words[i];
			if(line.length() == 0 && word.length() >= maxCharsPerLine) {
				line.append(word);
				formatted.add(line.toString());
				line = new StringBuilder();
				if(color != null)
					line.append(color);
				continue;
			}

			line.append(word);

			if(i < words.length - 1) {
				if(words[i + 1].length() + line.length() + 1 > maxCharsPerLine) {
					formatted.add(line.toString());
					line = new StringBuilder();
					if(color != null)
						line.append(color);
				} else line.append(" ");
			} else formatted.add(line.toString());
		}
		return formatted;
	}

	public static List<String> formatLore(String lore, String indication) {
		return Arrays.asList(lore.split(indication));
	}

	public static Material getSpawnEgg(EntityType et) {
		switch(et) {
			case BAT:
				return Material.BAT_SPAWN_EGG;
			case BEE:
				return Material.BEE_SPAWN_EGG;
			case BLAZE:
				return Material.BLAZE_SPAWN_EGG;
			case CAT:
				return Material.CAT_SPAWN_EGG;
			case CAVE_SPIDER:
				return Material.CAVE_SPIDER_SPAWN_EGG;
			case CHICKEN:
				return Material.CHICKEN_SPAWN_EGG;
			case COD:
				return Material.COD_SPAWN_EGG;
			case COW:
				return Material.COW_SPAWN_EGG;
			case CREEPER:
				return Material.CREEPER_SPAWN_EGG;
			case DOLPHIN:
				return Material.DOLPHIN_SPAWN_EGG;
			case DONKEY:
				return Material.DONKEY_SPAWN_EGG;
			case DROWNED:
				return Material.DROWNED_SPAWN_EGG;
			case ELDER_GUARDIAN:
				return Material.ELDER_GUARDIAN_SPAWN_EGG;
			case ENDERMAN:
				return Material.ENDERMAN_SPAWN_EGG;
			case ENDERMITE:
				return Material.ENDERMITE_SPAWN_EGG;
			case EVOKER:
				return Material.EVOKER_SPAWN_EGG;
			case FOX:
				return Material.FOX_SPAWN_EGG;
			case GHAST:
				return Material.GHAST_SPAWN_EGG;
			case GUARDIAN:
				return Material.GUARDIAN_SPAWN_EGG;
			case HOGLIN:
				return Material.HOGLIN_SPAWN_EGG;
			case HORSE:
				return Material.HORSE_SPAWN_EGG;
			case HUSK:
				return Material.HUSK_SPAWN_EGG;
			case LLAMA:
				return Material.LLAMA_SPAWN_EGG;
			case MAGMA_CUBE:
				return Material.MAGMA_CUBE_SPAWN_EGG;
			case MUSHROOM_COW:
				return Material.MOOSHROOM_SPAWN_EGG;
			case MULE:
				return Material.MULE_SPAWN_EGG;
			case OCELOT:
				return Material.OCELOT_SPAWN_EGG;
			case PANDA:
				return Material.PANDA_SPAWN_EGG;
			case PARROT:
				return Material.PARROT_SPAWN_EGG;
			case PHANTOM:
				return Material.PHANTOM_SPAWN_EGG;
			case PIG:
				return Material.PIG_SPAWN_EGG;
			case PILLAGER:
				return Material.PILLAGER_SPAWN_EGG;
			case POLAR_BEAR:
				return Material.POLAR_BEAR_SPAWN_EGG;
			case PUFFERFISH:
				return Material.PUFFERFISH_SPAWN_EGG;
			case RABBIT:
				return Material.RABBIT_SPAWN_EGG;
			case RAVAGER:
				return Material.RAVAGER_SPAWN_EGG;
			case SALMON:
				return Material.SALMON_SPAWN_EGG;
			case SHEEP:
				return Material.SHEEP_SPAWN_EGG;
			case SHULKER:
				return Material.SHULKER_SPAWN_EGG;
			case SILVERFISH:
				return Material.SILVERFISH_SPAWN_EGG;
			case SKELETON:
				return Material.SKELETON_SPAWN_EGG;
			case SKELETON_HORSE:
				return Material.SKELETON_HORSE_SPAWN_EGG;
			case SLIME:
				return Material.SLIME_SPAWN_EGG;
			case SPIDER:
				return Material.SPIDER_SPAWN_EGG;
			case SQUID:
				return Material.SQUID_SPAWN_EGG;
			case STRAY:
				return Material.STRAY_SPAWN_EGG;
			case TRADER_LLAMA:
				return Material.TRADER_LLAMA_SPAWN_EGG;
			case TROPICAL_FISH:
				return Material.TROPICAL_FISH_SPAWN_EGG;
			case TURTLE:
				return Material.TURTLE_SPAWN_EGG;
			case VEX:
				return Material.VEX_SPAWN_EGG;
			case VILLAGER:
				return Material.VILLAGER_SPAWN_EGG;
			case VINDICATOR:
				return Material.VINDICATOR_SPAWN_EGG;
			case WANDERING_TRADER:
				return Material.WANDERING_TRADER_SPAWN_EGG;
			case WITCH:
				return Material.WITCH_SPAWN_EGG;
			case WITHER_SKELETON:
				return Material.WITHER_SKELETON_SPAWN_EGG;
			case WOLF:
				return Material.WOLF_SPAWN_EGG;
			case ZOMBIE:
				return Material.ZOMBIE_SPAWN_EGG;
			case ZOMBIE_HORSE:
				return Material.ZOMBIE_HORSE_SPAWN_EGG;
			case ZOGLIN:
				return Material.ZOGLIN_SPAWN_EGG;
			case ZOMBIFIED_PIGLIN:
				return Material.ZOMBIFIED_PIGLIN_SPAWN_EGG;
			case ZOMBIE_VILLAGER:
				return Material.ZOMBIE_VILLAGER_SPAWN_EGG;
			default:
				return null;
		}
	}

	public static String enumNameToHumanReadable(String enumName) {
		if(enumName.length() <= 0)
			return "";

		String[] nameParts = enumName.toLowerCase().split("_");
		for(int i=0; i<nameParts.length; i++) {
			if(nameParts[i].length() <= 0)
				continue;

			nameParts[i] = Character.toUpperCase(nameParts[i].charAt(0)) + nameParts[i].substring(1);
		}

		return Strings.join(nameParts, " ");
	}

	public static String millisToDHMS(long millis) {
		StringBuilder sb = new StringBuilder();
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		boolean show = false;
		if(show || (show = days > 0)) {
			sb.append(String.format("%02d", days));
			sb.append("d");
		}

		long hours = TimeUnit.MILLISECONDS.toHours(millis) % TimeUnit.DAYS.toHours(1);
		if(show || (show = hours > 0)) {
			sb.append(String.format("%02d", hours));
			sb.append("h");
		}

		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
		if(show || (show = minutes > 0)) {
			sb.append(String.format("%02d", minutes));
			sb.append("m");
		}

		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);
		if(show) {
			sb.append(String.format("%02d", seconds));
			sb.append("s");
		} else if((show = seconds > 0)) {
			sb.append(String.format("%01d", seconds));
			sb.append("s");
		}

		if(!show)
			return "0s";

		return sb.toString();
	}

	public static String millisToDHMSWithSpacing(long millis) {
		StringBuilder sb = new StringBuilder();
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		boolean show = false;
		if(show || (show = days > 0)) {
			sb.append(String.format("%02d", days));
			sb.append("d ");
		}

		long hours = TimeUnit.MILLISECONDS.toHours(millis) % TimeUnit.DAYS.toHours(1);
		if(show || (show = hours > 0)) {
			sb.append(String.format("%02d", hours));
			sb.append("h ");
		}

		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
		if(show || (show = minutes > 0)) {
			sb.append(String.format("%02d", minutes));
			sb.append("m ");
		}

		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);
		if(show) {
			sb.append(String.format("%02d", seconds));
			sb.append("s");
		} else if((show = seconds > 0)) {
			sb.append(String.format("%01d", seconds));
			sb.append("s");
		}

		if(!show)
			return "0s";

		return sb.toString();
	}
}
