package me.botsko.dhmcstats.rank.groups;

public class NewModerator extends Group {
	
	/**
	 * 
	 */
	public NewModerator(){
		may_auto_promo_to = false;
		nice_name = "NewMod";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new Moderator();
	}
}