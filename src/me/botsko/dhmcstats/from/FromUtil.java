package me.botsko.dhmcstats.from;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import me.botsko.dhmcstats.Dhmcstats;

public class FromUtil {
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static void add( Dhmcstats plugin, String username, String reason ){
		try {
			// Save join date
	        java.util.Date date= new java.util.Date();
	        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
			
			plugin.dbc();
	        PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO referrals (username,reason,date_created) VALUES (?,?,?)");
	        s.setString(1, username);
	        s.setString(2, reason);
	        s.setString(3, ts);
	        s.executeUpdate();
    		s.close();
            plugin.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static boolean hasReferral( Dhmcstats plugin, String username ){
		try {
            
			plugin.dbc();
			
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT * FROM referrals WHERE username = ?");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();

    		if(rs.first()){
    			return true;
    		}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return false;
	}
}
