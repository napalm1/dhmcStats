package me.botsko.dhmcstats.rank.groups;

public class RespectedPlayer extends Group {
	
	protected boolean is_promotable = true;
	protected String nice_name = "Trusted";
	protected int hours_required = 5;
	protected int days_required = 1;
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new RespectedPlayer();
	}
}