package me.botsko.dhmcstats.warnings;

public class Warnings {
	
	public final int id;
	public final String datewarned;
	public final String username;
	public final String moderator;
	public final String reason;
	
	public Warnings( int id, String datewarned, String username, String reason, String moderator ){
		this.id = id;
		this.username = username;
		this.datewarned = datewarned;
		this.moderator = moderator;
		this.reason = reason;
	}
}