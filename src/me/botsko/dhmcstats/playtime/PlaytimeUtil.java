package me.botsko.dhmcstats.playtime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import me.botsko.dhmcstats.Dhmcstats;

public class PlaytimeUtil {

	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 * @throws ParseException 
	 */
	public static Playtime getPlaytime( Dhmcstats plugin, String username ) throws ParseException{
		try {
			
			plugin.dbc();
			
			// query for the null quit record for this player
			PreparedStatement s;
			s = plugin.conn.prepareStatement ("SELECT SUM(playtime) as playtime FROM joins WHERE username = ?");
			s.setString(1, username);
			s.executeQuery();
			ResultSet rs = s.getResultSet();
			
			try {
				rs.first();
				int before_current = rs.getInt(1);
				
				// We also need to pull any incomplete join and calc up-to-the-minute playtime
				PreparedStatement s1;
				s1 = plugin.conn.prepareStatement ("SELECT player_join FROM joins WHERE username = ? AND player_quit IS NULL");
				s1.setString(1, username);
				s1.executeQuery();
				ResultSet rs1 = s1.getResultSet();
				
				long session_hours = 0;
				try {
					if(rs1.first()){
						String session_started = rs1.getString("player_join");
						
						DateFormat formatter ;
				    	formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				    	Date joined = (Date)formatter.parse( session_started );
				    	Date today = new Date();
				    	session_hours = today.getTime() - joined.getTime();
				    	session_hours = session_hours / 1000;
					}
				}
				catch ( SQLException e ) {
					e.printStackTrace();
				}
				
				rs1.close();
				s1.close();
				
				return new Playtime( (int) (before_current + session_hours) );
				
			}
			catch ( SQLException e ) {
				e.printStackTrace();
			}
			
			rs.close();
			s.close();
			plugin.conn.close();
			return null;

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
	 */
	public static HashMap<Playtime,String> getPlayerPlaytimeHistory( Dhmcstats plugin, String username ){
		try {
            
			plugin.dbc();
			
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT DATE_FORMAT(joins.player_join,'%Y-%m-%d') as playdate, SUM(playtime) as playtime FROM joins WHERE username = ? GROUP BY DATE_FORMAT(joins.player_join,'%Y-%m-%d') ORDER BY joins.player_join DESC LIMIT 7;");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();

    		HashMap<Playtime,String> scores = new HashMap<Playtime, String>();
    		while(rs.next()){
    			scores.put( new Playtime(rs.getInt("playtime")), rs.getString("playdate") );
			}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
            return scores;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return null;
	}
}
