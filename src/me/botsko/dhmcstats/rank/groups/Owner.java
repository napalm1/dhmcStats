package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class Owner extends Group {
	
	/**
	 * 
	 */
	public Owner(){
		may_auto_promo_to = false;
		nice_name = "Owner";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return null;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ChatColor getColor() {
		return ChatColor.DARK_RED;
	}
}