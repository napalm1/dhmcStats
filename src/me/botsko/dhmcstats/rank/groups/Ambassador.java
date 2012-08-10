package me.botsko.dhmcstats.rank.groups;

public class Ambassador extends Group {
	
	/**
	 * 
	 */
	public Ambassador(){
		may_auto_promo_to = false;
		nice_name = "Ambassador";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return null;
	}
}