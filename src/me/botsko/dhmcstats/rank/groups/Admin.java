package me.botsko.dhmcstats.rank.groups;

public class Admin extends Group {
	
	/**
	 * 
	 */
	public Admin(){
		may_auto_promo_to = false;
		nice_name = "Admin";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new Owner();
	}
}