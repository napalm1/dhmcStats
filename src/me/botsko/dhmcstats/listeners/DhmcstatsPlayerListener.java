package me.botsko.dhmcstats.listeners;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import me.botsko.dhmcstats.Dhmcstats;
import me.botsko.dhmcstats.joins.JoinUtil;
import me.botsko.dhmcstats.rank.Rank;
import me.botsko.dhmcstats.rank.RankUtil;
import me.botsko.dhmcstats.warnings.WarningUtil;
import me.botsko.dhmcstats.warnings.Warnings;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ru.tehkode.permissions.exceptions.RankingException;

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
     * @throws RankingException 
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) throws RankingException {
        
        Player player = event.getPlayer();
        String username = player.getName();
 
        // Save join date
        java.util.Date date= new java.util.Date();
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
        String ip = player.getAddress().getAddress().getHostAddress().toString();
        JoinUtil.registerPlayerJoin( plugin, username, ts, ip, plugin.getOnlineCount() );
        
        // Check the user qualifies for any rank
        try {
			Rank rank = RankUtil.getPlayerRank( plugin, username );
			if(rank.getPlayerQualifiesForPromo()){
				
				// auto promote
				plugin.permissions.getUser(username).promote( plugin.permissions.getUser("viveleroi"), "default" );
				
				// announce the promotion
				plugin.messageAllPlayers("Congratulations, " + ChatColor.AQUA + username + ChatColor.WHITE + " on your promotion to " + ChatColor.AQUA + rank.getQualifiedPromotionRank() );
				
				// log the promotion
				plugin.log("Auto promoted " + username + " to " + rank.getQualifiedPromotionRank());
				
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        
        // If the user has three or more warnings, alert staff
        List<Warnings> warnings = WarningUtil.getPlayerWarnings( plugin, username );
        if(warnings.size() >= 3){
        	for(Player pl: plugin.getServer().getOnlinePlayers()) {
        		if(pl.hasPermission("dhmcstats.warn")){
        			pl.sendMessage( plugin.playerMsg(username + " now has three warnings. " + ChatColor.RED + "Action must be taken.") );
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
        
        JoinUtil.registerPlayerQuit( plugin, username, ts );
        
    }
}