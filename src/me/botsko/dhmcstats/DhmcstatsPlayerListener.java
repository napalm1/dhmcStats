package me.botsko.dhmcstats;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ru.tehkode.permissions.PermissionUser;

public class DhmcstatsPlayerListener implements Listener {

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
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        
        Player player = event.getPlayer();
        String username = player.getName();
 
        // Save join date
        java.util.Date date= new java.util.Date();
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
        String ip = player.getAddress().getAddress().getHostAddress().toString();
		plugin.getDbDAO().registerPlayerJoin( username, ts, ip, plugin.getOnlineCount() );
        

      // CHECK FORUMS
        
        
        // Check the user qualifies for any rank, alert mods
        String promo = "";
        try {
        	RankCommandExecutor rce = new RankCommandExecutor(plugin);
			promo = rce.checkQualifiesFor(username);
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
	            	pl.sendMessage( promo);
	            }
	        }
        }
    }
    
    
    /**
     * Save the timestamp and player data upon the QUIT event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event){
        
        Player player = event.getPlayer();
        String username = player.getName();
       
        java.util.Date date= new java.util.Date();
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
        
        plugin.getDbDAO().registerPlayerQuit( username, ts );
        
    }
}