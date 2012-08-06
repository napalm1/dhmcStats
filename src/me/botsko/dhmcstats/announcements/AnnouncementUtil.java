package me.botsko.dhmcstats.announcements;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.ChatColor;

public class AnnouncementUtil {

	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static List<String> getActiveAnnouncements( Dhmcstats plugin ){
		ArrayList<String> announces = new ArrayList<String>();
		try {
            
			plugin.dbc();
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT announcement FROM announcements WHERE is_active = 1");
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();

    		while(rs.next()){
    			String msg = ChatColor.GOLD + "[psa]: " + ChatColor.RED + rs.getString("announcement");
    			announces.add(msg);
			}
    		rs.close();
    		
    		// pull forum announcements
    		s = plugin.conn.prepareStatement ("SELECT * FROM posts WHERE category_id = 9 AND announcement = 1 AND closed = 0 AND hidden = 0");
    		s.executeQuery();
    		ResultSet rs1 = s.getResultSet();

    		while(rs1.next()){
    			String msg = ChatColor.GOLD + "[forums]: " + ChatColor.RED + rs1.getString("title") + " http://dhmc.us/r/"+rs1.getString("id")+"";
    			announces.add(msg);
			}
    		rs1.close();
    		
    		// pull recent blog posts announcements
    		s = plugin.conn.prepareStatement ("SELECT * FROM blog_posts WHERE TO_DAYS(NOW()) - TO_DAYS(date_created) < 7");
    		s.executeQuery();
    		ResultSet rs2 = s.getResultSet();

    		while(rs2.next()){
    			String msg = ChatColor.GOLD + "[news]: " + ChatColor.RED + rs2.getString("title") + " http://dhmc.us/blog/";
    			announces.add(msg);
			}
    		rs2.close();
    		
    		s.close();
            plugin.conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return announces;
	}
	
}
