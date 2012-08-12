package me.botsko.dhmcstats.rank;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.ChatColor;

import me.botsko.dhmcstats.playtime.Playtime;
import me.botsko.dhmcstats.rank.groups.Admin;
import me.botsko.dhmcstats.rank.groups.Ambassador;
import me.botsko.dhmcstats.rank.groups.EternalPlayer;
import me.botsko.dhmcstats.rank.groups.Group;
import me.botsko.dhmcstats.rank.groups.LeadModerator;
import me.botsko.dhmcstats.rank.groups.LegendaryPlayer;
import me.botsko.dhmcstats.rank.groups.Moderator;
import me.botsko.dhmcstats.rank.groups.MythicalPlayer;
import me.botsko.dhmcstats.rank.groups.NewModerator;
import me.botsko.dhmcstats.rank.groups.Owner;
import me.botsko.dhmcstats.rank.groups.Player;
import me.botsko.dhmcstats.rank.groups.RespectedPlayer;
import me.botsko.dhmcstats.rank.groups.TrustedPlayer;

public class Rank {
	
	/**
	 * 
	 */
	protected Playtime playtime;
	
	/**
	 * 
	 */
	protected Date joined;
	
	/**
	 * 
	 */
	protected int days_since_join = 0;
	
	/**
	 * 
	 */
	protected boolean player_joined_today = false;
	
	/**
	 * 
	 */
	protected String[] permissions_groups;
	
	/**
	 * 
	 */
	protected boolean player_qualifies_for_promo = false;
	
	/**
	 * 
	 */
	protected Group current_rank_in_ladder;
	
	/**
	 * 
	 */
	protected Group next_rank_in_ladder;
	
	/**
	 * 
	 */
	protected HashMap<UserGroup,Group> dhmcRanks = new HashMap<UserGroup,Group>();
	
	/**
	 * 
	 */
	protected ChatColor rank_color;
	
	
	/**
	 * 
	 * @param joined
	 * @param playtime
	 * @param permissions_groups
	 * @param messages_for_self
	 */
	public Rank( Date joined, Playtime playtime, String[] permissions_groups){

		// add all groups
		dhmcRanks.put(UserGroup.Player, new Player());
		dhmcRanks.put(UserGroup.TrustedPlayer, new TrustedPlayer());
		dhmcRanks.put(UserGroup.RespectedPlayer, new RespectedPlayer());
		dhmcRanks.put(UserGroup.LegendaryPlayer, new LegendaryPlayer());
		dhmcRanks.put(UserGroup.MythicalPlayer, new MythicalPlayer());
		dhmcRanks.put(UserGroup.EternalPlayer, new EternalPlayer());
		dhmcRanks.put(UserGroup.NewModerator, new NewModerator());
		dhmcRanks.put(UserGroup.Moderator, new Moderator());
		dhmcRanks.put(UserGroup.LeadModerator, new LeadModerator());
		dhmcRanks.put(UserGroup.Admin, new Admin());
		dhmcRanks.put(UserGroup.Ambassador, new Ambassador());
		dhmcRanks.put(UserGroup.Owner, new Owner());
		
		this.joined = joined;
		this.playtime = playtime;
		this.permissions_groups = permissions_groups;
		
		setDaysSinceJoin();
		
		// Pull the group the user is in now
		UserGroup current = getCurrentRank();
		current_rank_in_ladder = dhmcRanks.get( current );
		this.rank_color = current_rank_in_ladder.getColor();
		
		// Pull the next rank
		next_rank_in_ladder = current_rank_in_ladder.getNextRank();
		
		// Determine if player has earned the next rank
		player_qualifies_for_promo = playerHasEarnedRank();
		
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
	
	public ChatColor getRankColor(){
		return rank_color;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return next_rank_in_ladder;
	}
	
	
	/**
	 * 
	 * @param group
	 * @param username
	 * @return
	 */
	public String getPromotionStatusMessage( String username, boolean message_is_for_self ){

		if(getPlayerQualifiesForPromo()){
			return (message_is_for_self ? "You qualify" : username+" qualifies")+ " for " + ChatColor.AQUA + " " + next_rank_in_ladder.getNiceName();
		} else {
			
			// Is Vive
			if(current_rank_in_ladder.getNiceName().equals("Owner")){
				return (message_is_for_self ? "You're" : "Vive's") + " rank is Pure Awesome. Silly you, checking the owner's rank.";
			}
			else if(current_rank_in_ladder.getNiceName().equals("Ambassador")){
				return (message_is_for_self ? "You sit" : username+" sits") + " on the Darkhelmet High Council.";
			}
			else if(current_rank_in_ladder.getNiceName().equals("Admin")){
				return (message_is_for_self ? "You're" : username+" is") + " an admin.";
			}
			else if(!next_rank_in_ladder.mayBeAutoPromotedTo()){
				return (message_is_for_self ? "You're" : username+" is") + " "+current_rank_in_ladder.getNiceName()+". A promotion is up to the Council";
			} else {
				
		    	int remain_days = (next_rank_in_ladder.getDaysRequired() - days_since_join);
				int remain_hrs = (next_rank_in_ladder.getHoursRequired() - playtime.getHours());
		
				// If days remain, but no hours
				String time_left = " You need to play " + remain_hrs + " hours over at least "+remain_days+" days for " + next_rank_in_ladder.getNiceName();
				if(remain_days > 0 && remain_hrs <= 0){
					time_left = next_rank_in_ladder.getNiceName()+" in "+remain_days+" days. "+(message_is_for_self ? "You meet" : username+" meets")+" the minimum playtime hours requirement.";
				}
				// If hours remain, but no days
				if(remain_days <= 0 && remain_hrs > 0){
					time_left = next_rank_in_ladder.getNiceName()+" in "+remain_hrs+" hours of playtime. "+(message_is_for_self ? "You meet" : username+" meets")+" the minimum days requirement.";
				}
				// If both remain
				if(remain_days > 0 && remain_hrs > 0){
					time_left = next_rank_in_ladder.getNiceName()+" in "+remain_hrs+" hours of playtime, in at least " + remain_days + " more days (since joined).";
				}
				// If trying to rank to trusted, check for the day after
				if(next_rank_in_ladder.getNiceName().equalsIgnoreCase("trusted")){
					if(days_since_join == 0){
						if(remain_hrs > 0){
							time_left = next_rank_in_ladder.getNiceName()+" in "+remain_hrs+" hours of playtime, but joined today. Promotion tomorrow.";
						} else {
							time_left = (message_is_for_self ? "You've" : username+" has") + " played enough for " + next_rank_in_ladder.getNiceName()+", but joined today. Promotion tomorrow.";
						}
					}
				}
				
				// They can be promoted, they're just not ready
				return time_left; 
				
			}
		}
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
	 * @param test
	 * @return
	 */
	public static boolean userGroupExists(String test) {

	    for (UserGroup c : UserGroup.values()) {
	        if (c.name().equals(test)) {
	            return true;
	        }
	    }

	    return false;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public UserGroup getCurrentRank(){
		for(String group : permissions_groups){
			if(userGroupExists(group)){
				UserGroup g = UserGroup.valueOf(group);
				if(g != null){
					return g;
				}
			}
		}
		return null;
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
	 * @param group
	 * @return
	 */
	protected boolean playerHasEarnedRank(){
		// No promotions on first day
		if(!getPlayerJoinedToday()){
			if(next_rank_in_ladder != null){
				if(next_rank_in_ladder.mayBeAutoPromotedTo()){
					if(playtime.getHours() >= next_rank_in_ladder.getHoursRequired()){
						if(days_since_join >= next_rank_in_ladder.getDaysRequired()){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
