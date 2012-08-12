package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class Admin extends Group {
	
	/**
	 * 
	 */
	public Admin(){
		may_auto_promo_to = false;
		nice_name = "Admin";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new Ambassador();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ChatColor getColor() {
		return ChatColor.DARK_PURPLE;
	}
}