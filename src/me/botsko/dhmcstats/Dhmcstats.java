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
 * 
 * - Player join/quit listeners logging the timestamps
 * 
 * 
 * FUTURE:
 * 
 * - Check if the user has registered for the forums, alert them on join
 * - Take over tracking of playtime (ignoring AFK time)
 * - Alert lead moderators when a user qualifies for a promotion
 * - Add commands to see if a player is online
 * 
 */

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class Dhmcstats extends JavaPlugin {
	
	Logger log = Logger.getLogger("Minecraft");
	java.sql.Connection c;
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
		
		log.info("[Dhmcstats]: Initializing player listeners");
		
		dbc();
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
		
	}
 
	
	/**
	 * Shutdown
	 */
	public void onDisable(){
		log.info("[Dhmcstats]: Stopping player listeners");
	}
}