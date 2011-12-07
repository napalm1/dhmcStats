package me.botsko.dhmcstats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

import ru.tehkode.permissions.PermissionUser;

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
        
        // identify ip
        String ip = player.getAddress().getAddress().getHostAddress().toString();

        try {
			if (plugin.c == null || plugin.c.isClosed()) plugin.dbc();
			
			String s = String.format("INSERT INTO joins (username,player_join,ip,player_count) VALUES ('%s','%s','%s','%d')", username, ts, ip, plugin.getOnlineCount());
	        PreparedStatement pstmt = plugin.c.prepareStatement(s);
	        pstmt.executeUpdate();
	        
	        player.sendMessage(ChatColor.AQUA + "Welcome " + username + "! We have " + plugin.getOnlineCount() + " players online.");
	        
	        pstmt.close();
	
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}

        try {
			plugin.checkForums(player);
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        
        // Check the user qualifies for any rank, alert mods
        String promo = "";
        try {
			promo = plugin.checkQualifiesFor(username);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
        // if string not empty, notify lead mods
        if(promo != "" && promo.indexOf(" admin") == -1 && promo.indexOf(" Ask Vive") == -1 && promo.indexOf(" Legendary") == -1 && promo.indexOf(" not awaiting") == -1){
	        for(Player pl: plugin.getServer().getOnlinePlayers()) {
	        	PermissionUser user = plugin.permissions.getUser( pl.getName() );
	            if(user.inGroup( "LeadModerator" ) || user.inGroup( "Admin" )) {
	            	pl.sendMessage(promo);
	            }
	        }
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
				pstmt.close();
	        
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
					pstmt1.close();
					
				}
				
				trs.close();
				ts1.close();
			}
			
			rs.close();
			s.close();
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
    }
}