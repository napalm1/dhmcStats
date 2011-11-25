package me.botsko.dhmcstats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DhmcstatsPlayerListener extends PlayerListener {

    public Dhmcstats plugin;
    
    
    /**
     * 
     * @param instance
     */
    public DhmcstatsPlayerListener(Dhmcstats instance) {
        plugin = instance;
    }
    
    
    /**
     * Save the timestamp and player data upon the JOIN event
     */
    public void onPlayerJoin(PlayerJoinEvent event) {
        
        Player player = event.getPlayer();
        String username = player.getName();
 
        java.util.Date date= new java.util.Date();
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());

        try {
			if (plugin.c == null || plugin.c.isClosed()) plugin.dbc();
			
			String s = String.format("INSERT INTO joins (username,player_join) VALUES ('%s','%s')", username, ts);
			//plugin.log.info(s);
	        PreparedStatement pstmt = plugin.c.prepareStatement(s);
	        pstmt.executeUpdate();
	
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
        
        try {
			plugin.checkForums(player);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    /**
     * Save the timestamp and player data upon the QUIT event
     */
    public void onPlayerQuit(PlayerQuitEvent event){
        
        Player player = event.getPlayer();
        String username = player.getName();
       
        java.util.Date date= new java.util.Date();
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());

        try {
			if (plugin.c == null || plugin.c.isClosed()) plugin.dbc();
			
			// query for the null quit record for this player
			PreparedStatement s;
			s = plugin.c.prepareStatement ("SELECT id FROM joins WHERE username = ? AND player_quit IS NULL");
			s.setString(1, username);
			s.executeQuery();
			ResultSet rs = s.getResultSet();
			
			while( rs.next() ){
				
				Integer id = rs.getInt(1);
			
				String upd = String.format("UPDATE joins SET player_quit = '%s' WHERE id = '%d'", ts, id);
				PreparedStatement pstmt = plugin.c.prepareStatement(upd);
				pstmt.executeUpdate();
	        
				// now calculate the time spent online between this quit and join
				PreparedStatement ts1;
				ts1 = plugin.c.prepareStatement ("SELECT TIME_TO_SEC(TIMEDIFF(player_quit,player_join)) AS playtime FROM joins WHERE id = ?");
				ts1.setInt(1, id);
				ts1.executeQuery();
				ResultSet trs = ts1.getResultSet();
				
				while( trs.next() ){
					
					int playtime = trs.getInt(1);
					
					String upd1 = String.format("UPDATE joins SET playtime = '%s' WHERE id = '%d'", playtime, id);
					PreparedStatement pstmt1 = plugin.c.prepareStatement(upd1);
					pstmt1.executeUpdate();
					
				}
			}
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
    }
}