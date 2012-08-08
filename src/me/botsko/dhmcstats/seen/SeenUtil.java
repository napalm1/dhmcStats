package me.botsko.dhmcstats.seen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.botsko.dhmcstats.Dhmcstats;

public class SeenUtil {
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 * @throws ParseException 
	 */
	public static boolean hasPlayerBeenSeen( Dhmcstats plugin, String username ) throws ParseException{
		boolean seen = false;
		try {
			
			plugin.dbc();
            
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT id FROM joins WHERE username = ? ORDER BY player_join LIMIT 1;");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		if(rs.first()){
    			seen = true;
    		}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return seen;
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 * @throws ParseException 
	 */
	public static Date getPlayerFirstSeen( Dhmcstats plugin, String username ) throws ParseException{
		Date joined = null;
		try {
			
			plugin.dbc();
            
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT player_join FROM joins WHERE username = ? ORDER BY player_join LIMIT 1;");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		if(rs.first()){
    			String join = rs.getString("player_join");
	    		DateFormat formatter ;
	        	formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	joined = (Date)formatter.parse( join );
    		}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
            return joined;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return null;
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 * @throws ParseException 
	 */
	public static Date getPlayerLastSeen( Dhmcstats plugin, String username ) throws ParseException{
		Date seen = null;
		try {
			
			plugin.dbc();
            
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT player_quit FROM joins WHERE username = ? AND player_quit IS NOT NULL ORDER BY player_quit DESC LIMIT 1;");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		if(rs.first()){
	    		String join = rs.getString("player_quit");
	    		DateFormat formatter ;
	        	formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	seen = (Date)formatter.parse( join );
    		}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
            return seen;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return null;
	}

}
