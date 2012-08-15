package me.botsko.dhmcstats.bans;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import me.botsko.dhmcstats.Dhmcstats;

public class BanUtil {
	
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static void recordBan( Dhmcstats plugin, String banned_player, String banning_player, String reason ){
		try {
			// Save join date
	        java.util.Date date= new java.util.Date();
	        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
			
			plugin.dbc();
	        PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO bans (banned_player,banning_player,reason,date_created) VALUES (?,?,?,?)");
	        s.setString(1, banned_player);
	        s.setString(2, banning_player);
	        s.setString(3, reason);
	        s.setString(4, ts);
	        s.executeUpdate();
    		s.close();
            plugin.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
	}

}
