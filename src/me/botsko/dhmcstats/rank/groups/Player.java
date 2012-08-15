package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class Player extends Group {

	
	/**
	 * 
	 */
	public Player(){
		nice_name = "New";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new TrustedPlayer();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ChatColor getColor() {
		return ChatColor.GRAY;
	}
}