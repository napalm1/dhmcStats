package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class Ambassador extends Group {
	
	/**
	 * 
	 */
	public Ambassador(){
		may_auto_promo_to = false;
		nice_name = "Ambassador";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new Owner();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ChatColor getColor() {
		return ChatColor.DARK_RED;
	}
}