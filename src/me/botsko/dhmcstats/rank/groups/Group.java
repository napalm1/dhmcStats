package me.botsko.dhmcstats.rank.groups;

import org.bukkit.ChatColor;

public class Group {
	
	protected boolean may_auto_promo_to = false;
	protected String nice_name = "";
	protected int hours_required = 0;
	protected int days_required = 0;
	protected ChatColor rank_color = null;
	
	
	/**
	 * @return the nice_name
	 */
	public String getNiceName() {
		return nice_name;
	}
	
	
	/**
	 * @return the hours_required
	 */
	public int getHoursRequired() {
		return hours_required;
	}
	
	
	/**
	 * @return the days_required
	 */
	public int getDaysRequired() {
		return days_required;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean mayBeAutoPromotedTo(){
		return may_auto_promo_to;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return null;
	}


	public ChatColor getColor() {
		return null;
	}
}