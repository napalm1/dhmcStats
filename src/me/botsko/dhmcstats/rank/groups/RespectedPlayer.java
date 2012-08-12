package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class RespectedPlayer extends Group {
	
	/**
	 * 
	 */
	public RespectedPlayer(){
		may_auto_promo_to = true;
		nice_name = "Respected";
		hours_required = 20;
		days_required = 5;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new LegendaryPlayer();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ChatColor getColor() {
		return ChatColor.BLUE;
	}
}