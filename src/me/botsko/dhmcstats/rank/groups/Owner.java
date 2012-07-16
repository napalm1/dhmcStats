package me.botsko.dhmcstats.rank.groups;

public class Owner extends Group {
	
	/**
	 * 
	 */
	public Owner(){
		may_auto_promo_to = false;
		nice_name = "Owner";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return null;
	}
}