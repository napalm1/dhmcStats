package me.botsko.dhmcstats.rank.groups;

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
}