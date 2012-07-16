package me.botsko.dhmcstats.rank.groups;

public class TrustedPlayer extends Group {

	
	/**
	 * 
	 */
	public TrustedPlayer(){
		may_auto_promo_to = true;
		nice_name = "Trusted";
		hours_required = 5;
		// days_required left at 0 because Rank auto checks for day-after-they-joined
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new RespectedPlayer();
	}
}