package me.botsko.dhmcstats.warnings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import me.botsko.dhmcstats.Dhmcstats;

public class WarningUtil {
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static void fileWarning( Dhmcstats plugin, String username, String reason, String filer ){
		try {
			// Save join date
	        java.util.Date date= new java.util.Date();
	        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
			
			plugin.dbc();
	        PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO warnings (username,reason,date_created,moderator) VALUES (?,?,?,?)");
	        s.setString(1, username);
	        s.setString(2, reason);
	        s.setString(3, ts);
	        s.setString(4, filer);
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
	public static List<Warnings> getPlayerWarnings( Dhmcstats plugin, String username ){
		ArrayList<Warnings> warnings = new ArrayList<Warnings>();
		try {
            
			plugin.dbc();
			
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT id, DATE_FORMAT(warnings.date_created,'%m/%d/%y') as warndate, reason, username, moderator FROM warnings WHERE username = ? AND deleted = 0");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();

    		while(rs.next()){
    			warnings.add( new Warnings(rs.getInt("id"), rs.getString("warndate"), rs.getString("username"), rs.getString("reason"), rs.getString("moderator")) );
			}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return warnings;
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static void deleteWarning( Dhmcstats plugin, int id ){
		try {
			plugin.dbc();
	        PreparedStatement s = plugin.conn.prepareStatement("UPDATE warnings SET deleted = 1 WHERE id = ?");
	        s.setInt(1, id);
	        s.executeUpdate();
    		s.close();
            plugin.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
	}

}
