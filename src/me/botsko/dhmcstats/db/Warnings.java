package me.botsko.dhmcstats.db;

public class Warnings {
	
	public final String datewarned;
	public final String username;
	public final String moderator;
	public final String reason;
	
	public Warnings( String datewarned, String username, String reason, String moderator ){
		this.username = username;
		this.datewarned = datewarned;
		this.moderator = moderator;
		this.reason = reason;
	}
}