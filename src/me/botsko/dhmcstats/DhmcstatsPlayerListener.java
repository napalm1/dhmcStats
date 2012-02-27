package me.botsko.dhmcstats;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
     * Log all commands
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String cmd = event.getMessage();
        double x = Math.floor( player.getLocation().getX() );
        double y = Math.floor( player.getLocation().getY() );
        double z = Math.floor( player.getLocation().getZ() );
        plugin.log( "[Command] " + player.getName() + " " + cmd + " @" + player.getWorld().getName() + " x:" + x + " y:" + y + " z:" + z);
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
        
        
        // Check the user qualifies for any rank, alert mods
        String promo = "";
        try {
        	RankCommandExecutor rce = new RankCommandExecutor(plugin);
			promo = rce.checkQualifiesFor( username, player );
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
        // if string not empty, notify lead mods
        if(promo.indexOf("qualify") > 1 || promo.indexOf("qualifies") > 1){
        	promo = promo.replace("You", username);
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