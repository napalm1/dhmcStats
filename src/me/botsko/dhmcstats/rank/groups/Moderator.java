package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class Moderator extends Group {
	
	/**
	 * 
	 */
	public Moderator(){
		may_auto_promo_to = false;
		nice_name = "Mod";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new LeadModerator();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ChatColor getColor() {
		return ChatColor.DARK_AQUA;
	}
}