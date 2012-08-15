package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class LegendaryPlayer extends Group {
	
	/**
	 * 
	 */
	public LegendaryPlayer(){
		may_auto_promo_to = true;
		nice_name = "Legendary";
		hours_required = 80;
		days_required = 25;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new MythicalPlayer();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ChatColor getColor() {
		return ChatColor.DARK_BLUE;
	}
}