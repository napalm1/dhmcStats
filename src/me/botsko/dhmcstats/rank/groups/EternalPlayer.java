package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class EternalPlayer extends Group {
	
	/**
	 * 
	 */
	public EternalPlayer(){
		may_auto_promo_to = false;
		nice_name = "Eternal";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new NewModerator();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ChatColor getColor() {
		return ChatColor.DARK_GREEN;
	}
}