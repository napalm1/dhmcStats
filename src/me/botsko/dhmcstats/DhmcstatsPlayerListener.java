package me.botsko.dhmcstats;

import java.sql.PreparedStatement;
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
    public void onPlayerJoin(PlayerJoinEvent event){
        
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
			
			String s = String.format("UPDATE joins SET player_quit = '%s' WHERE username = '%s' AND player_quit IS NULL", ts, username);
	        PreparedStatement pstmt = plugin.c.prepareStatement(s);
	        pstmt.executeUpdate();
	
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
    }
}