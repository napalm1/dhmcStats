package me.botsko.dhmcstats.rank.groups;

public class Player extends Group {

	
	/**
	 * 
	 */
	public Player(){
		nice_name = "New";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Group getNextRank(){
		return new TrustedPlayer();
	}
}