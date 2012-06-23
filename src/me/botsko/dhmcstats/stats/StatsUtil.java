package me.botsko.dhmcstats.stats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.botsko.dhmcstats.Dhmcstats;

public class StatsUtil {

	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static int getPlayerJoinCount( Dhmcstats plugin ){
		try {
			
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
            
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT COUNT( DISTINCT(username) ) FROM `joins`");
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		Integer total = 0;
    		while( rs.next() ){
    			total = rs.getInt(1);
    		}
    		rs.close();
    		s.close();
            plugin.conn.close();
            
            return total;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return 0;
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static int getPlayerJoinTodayCount( Dhmcstats plugin ){
		try {
			
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
            
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT COUNT( DISTINCT(username) ) FROM `joins` WHERE DATE_FORMAT(player_join,'%Y-%m-%d') = DATE_FORMAT(NOW(),'%Y-%m-%d')");
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		Integer total = 0;
    		while( rs.next() ){
    			total = rs.getInt(1);
    		}
    		rs.close();
    		s.close();
            plugin.conn.close();
            
            return total;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return 0;
	}
}
