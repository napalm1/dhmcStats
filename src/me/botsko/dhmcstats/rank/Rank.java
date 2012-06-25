package me.botsko.dhmcstats.rank;

import java.util.Calendar;
import java.util.Date;

import org.bukkit.ChatColor;

import me.botsko.dhmcstats.playtime.Playtime;

public class Rank {
	
	protected Playtime playtime;
	protected Date joined;
	protected int days_since_join = 0;
	protected boolean player_joined_today = false;
	protected String[] permissions_groups;
	protected boolean player_rank_is_promotable = false;
	protected boolean player_qualifies_for_promo = false;
	protected Group next_rank_in_ladder;
	
	
	/**
	 * 
	 * @param joined
	 * @param playtime
	 * @param permissions_groups
	 * @param messages_for_self
	 */
	public Rank( Date joined, Playtime playtime, String[] permissions_groups){
		
		this.joined = joined;
		this.playtime = playtime;
		this.permissions_groups = permissions_groups;
		
		setDaysSinceJoin();
		setPlayerQualifications();
		
	}
	
	
	/******************************
	 * PUBLIC METHODS
	 *********************/
	
	
	/**
	 * 
	 * @return
	 */
	public boolean getPlayerQualifiesForPromo(){
		return player_qualifies_for_promo;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getQualifiedPromotionRank(){
		return next_rank_in_ladder;
	}
	
	
	/**
	 * 
	 * @param group
	 * @param username
	 * @return
	 */
	public String getPromotionStatusMessage( String username, boolean message_is_for_self ){
		
		for(String group : permissions_groups){
			
			if(group.equalsIgnoreCase("owner")){
				return (message_is_for_self ? "You're" : "Vive's") + " rank is Pure Awesome. Silly you, checking the owner's rank.";
			} else {
			
				// is group promotable?
				if(isGroupPromotable(group)){
					return (message_is_for_self ? "You qualify" : username+" qualifies")+ " for " + ChatColor.AQUA + " " + getQualifiedPromotionRank();
				} else {
					if(group.equalsIgnoreCase("owner")){
						return (message_is_for_self ? "You're" : username+" is") + " an admin. Nowhere to go...";
					} else {
						return (message_is_for_self ? "You're" : username+" is") + " a "+group.toLowerCase()+". A promotion is up to Vive";
					}
				}
			}
		}
		return null;
	}
	
	
	/******************************
	 * INTERNAL METHODS
	 *********************/

	
	/**
	 * 
	 * @return
	 */
	protected boolean getPlayerJoinedToday(){
		return player_joined_today;
	}
	
	
	/**
	 * 
	 * @return
	 */
	protected int getDaysSinceJoin(){
		return days_since_join;
	}
	
	
	/**
	 * 
	 * @return
	 */
	protected boolean isGroupPromotable( String group ){
		if(group.equalsIgnoreCase("Player") || group.equalsIgnoreCase("TrustedPlayer") || group.equalsIgnoreCase("RespectedPlayer")){
			return true;
		}
		return false;
	}	
	
	
	/**
	 * 
	 */
	protected void setDaysSinceJoin(){
		if(joined != null){
			
			// Set days since joined
	    	Date today = new Date();
	    	long diff = today.getTime() - joined.getTime();
	    	days_since_join = (int) ((diff / 1000) / 86400);
	    	
	    	// Is today the same day they joined (simply checks the dates,
	    	// does not force a 24-hour wait)
    		Calendar cal1 = Calendar.getInstance();
    		Calendar cal2 = Calendar.getInstance();
    		cal1.setTime(today);
    		cal2.setTime(joined);
    		player_joined_today = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	    	
		}
	}
	
	
	/**
	 * 
	 */
	protected void setPlayerQualifications(){
		for(String group : permissions_groups){
			// is group promotable?
			if(isGroupPromotable(group)){
				
				player_rank_is_promotable = true;
				// does the user playtime meet rank-up qualifications?
				setPlayerQualifiesForPromotion( group );
				
			}
		}
	}
	
	
	/**
	 * 
	 */
	protected void setPlayerQualifiesForPromotion( String group ){
		// Using the player's current group, do they qualify for a rank-up?
		if(group.equalsIgnoreCase("Player")){
			if(playerHasEarnedRank( Group.Player )){
				next_rank_in_ladder = Group.Player;
				player_qualifies_for_promo = true;
			}
		}
		if(group.equalsIgnoreCase("TrustedPlayer")){
			if(playerHasEarnedRank( Group.TrustedPlayer )){
				next_rank_in_ladder = Group.TrustedPlayer;
				player_qualifies_for_promo = true;
			}
		}
		if(group.equalsIgnoreCase("RespectedPlayer")){
			if(playerHasEarnedRank( Group.RespectedPlayer )){
				next_rank_in_ladder = Group.RespectedPlayer;
				player_qualifies_for_promo = true;
			}
		}
	}
	
	
	/**
	 * 
	 * @param group
	 * @return
	 */
	protected boolean playerHasEarnedRank( Group group ){
		
		// Trusted
		if(group == Group.Player){
			if(!getPlayerJoinedToday() && playtime.getHours() >= 5){
				return true;
			}
		}
		
		// Respected
		if(group == Group.TrustedPlayer){
			if(getDaysSinceJoin() >= 5 && playtime.getHours() >= 20){
				return true;
			}
		}
		
		// Legendary
		if(group == Group.RespectedPlayer){
			if(getDaysSinceJoin() >= 25 && playtime.getHours() >= 80){
				return true;
			}
		}
		return false;
	}
}
