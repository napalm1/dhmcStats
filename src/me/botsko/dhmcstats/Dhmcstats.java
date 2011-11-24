package me.botsko.dhmcstats;

/**
 * 
 * dhmcStats
 * 
 * This plugin is specific to Mike's DarkHelmet Minecraft server. dhmc.us
 * 
 * Logs player join and quit so that we may track their daily activity and better
 * know how many active players we have over time.
 * 
 * Version 0.1
 * - Player join/quit listeners logging the timestamps
 * 0.1.1:
 * - : Added playtime calculations on quit event
 * - : Adding core playtime reading function
 * 
 * 
 * FUTURE:
 * 
 * - Check if the user has registered for the forums, alert them on join
 * - Take over tracking of playtime (ignoring AFK time)
 * - Alert lead moderators when a user qualifies for a promotion
 * - Add commands to see if a player is online
 * - Reward users who sign up for the forums with something, or who post replies
 * 
 * 
 *     for(Player player: getServer().getOnlinePlayers()) {
     
        if(player.hasPermission("send.me.message")) {
            player.sendMessage("You were sent a message");
        }
     
    }


 * 
 */

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class Dhmcstats extends JavaPlugin {
	
	Logger log = Logger.getLogger("Minecraft");
	java.sql.Connection c;
	PermissionManager permissions;
    private final DhmcstatsPlayerListener playerListener = new DhmcstatsPlayerListener(this);
    
    
    /**
     * Connects to the MySQL database
     */
    public void dbc(){
    	try {
			c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/minecraft","root","");
		} catch (SQLException e) {
			log.throwing("me.botsko.dhmcstats", "dbConnect()", e);
		}
	}


    /**
     * Enables the plugin and activates our player listeners
     */
	public void onEnable(){
		
		// @todo look for any null records, assume they're from a crash, fill in time as now
		
		log.info("[Dhmcstats]: Initializing player listeners");
		
		dbc();
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
		
		if(pm.isPluginEnabled("PermissionsEx")){
			permissions = PermissionsEx.getPermissionManager();
			log.info("[Dhmcstats]: PermissionsEx found.");
		} else {
			log.warning("[Dhmcstats]: PermissionsEx plugin was not found.");
	    }
	}
 
	
	/**
	 * Shutdown
	 */
	public void onDisable(){
		log.info("[Dhmcstats]: Stopping player listeners");
	}
	
	
    /**
     * Handles all of the commands.
     * 
     * 
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	
    	Player player = null;
    	if (sender instanceof Player) {
    		player = (Player) sender;
    	}
    	
    	if (command.getName().equalsIgnoreCase("played")){
    		try {
    			if(permissions.has(player, "dhmcstats.played")){
    				checkPlayTime( player.getName(), sender );
    			}
			} catch (SQLException e) {
				e.printStackTrace();
			}
    		return true;
    	}
    	return false;
    }
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     */
    public void checkPlayTime(String username, CommandSender sender) throws SQLException{
    	
    	// query for the null quit record for this player
		PreparedStatement s;
		s = c.prepareStatement ("SELECT SUM(playtime) as playtime FROM joins WHERE username = ?");
		s.setString(1, username);
		s.executeQuery();
		ResultSet rs = s.getResultSet();
		
		while( rs.next() ){
			
			Float playtime = rs.getFloat(1);
			
			// @todo add current time
			
			int[] times = splitToComponentTimes(playtime);
			
			// display to user
			sender.sendMessage(ChatColor.GOLD + "You've played for " + times[0] + " hours, " + times[1] + " minutes, and " + times[2] + " seconds. Nice!");
			
		}
    }
    
    
    /**
     * Convert seconds into hours/mins/secs
     * 
     * @param biggy
     * @return
     */
    public static int[] splitToComponentTimes(Float biggy){
        long longVal = biggy.longValue();
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }
}