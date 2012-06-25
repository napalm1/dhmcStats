package me.botsko.dhmcstats.rank;

import java.text.ParseException;
import java.util.Date;

import me.botsko.dhmcstats.Dhmcstats;
import me.botsko.dhmcstats.playtime.Playtime;
import me.botsko.dhmcstats.playtime.PlaytimeUtil;
import me.botsko.dhmcstats.seen.SeenUtil;

public class RankUtil {
	
	
	/**
	 * 
	 * @param plugin
	 * @param username
	 * @return
	 * @throws ParseException
	 */
	public static Rank getPlayerRank( Dhmcstats plugin, String username ) throws ParseException{
		
		// get the base join date
    	Date joined = SeenUtil.getPlayerFirstSeen( plugin, username );
    	Playtime playtime = PlaytimeUtil.getPlaytime(plugin,username);
    	
    	return new Rank( joined, playtime, plugin.permissions.getUser(username).getGroupsNames() );
		
	}
}