package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class LeadModerator extends Group {
	
	/**
	 * 
	 */
	public LeadModerator(){
		may_auto_promo_to = false;
		nice_name = "LeadMod";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new Admin();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ChatColor getColor() {
		return ChatColor.RED;
	}
}