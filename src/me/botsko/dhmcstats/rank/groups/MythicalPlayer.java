package me.botsko.dhmcstats.rank.groups;

public class MythicalPlayer extends Group {
	
	/**
	 * 
	 */
	public MythicalPlayer(){
		may_auto_promo_to = false;
		nice_name = "Myth";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new EternalPlayer();
	}
}