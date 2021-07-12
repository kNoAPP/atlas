package com.knoban.atlas.utils;

import org.bukkit.ChatColor;

public enum Message {

	MISSING(ChatColor.GOLD + "Permission> "),
	USAGE(ChatColor.GOLD + "Usage> "),
	INFO(ChatColor.GOLD + "Info> "),
	WARN(ChatColor.GOLD + "Warn> "),
	CHUNK(ChatColor.GOLD + "Chunks> "),
	
	HELP("  ");
	
	private final String prefix;
	private final String suffix;
	
	Message(String prefix) {
		this(prefix, null);
	}
	
	Message(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public String getMessage(String s) {
		if(this == MISSING) return prefix + ChatColor.GRAY + "You are missing Node [" + ChatColor.DARK_AQUA + s + ChatColor.GRAY + "]!";
		if(this == WARN) return prefix + ChatColor.RED + s;
		if(this == HELP) return prefix + ChatColor.GOLD + s.replaceFirst(" -", ChatColor.GRAY + " -");
		return prefix + ChatColor.GRAY + s;
	}
}
