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
 * Version 0.1.1
 * - Added playtime calculations on quit event
 * - Added core playtime reading function
 * - Added forum registration check to alert user on join
 * - Added total/today/joined player stats command
 * Version 0.1.2
 * - Added forced playtime calcs for crashed join records
 * Version 0.1.3a
 * - Fixing numerous playtime bugs
 * - Added code that will import the Playtime plugin hashmap data
 * - Added "seen" command for first/last seen data
 * - Adding basic promotion qualification system
 * Version 0.1.3
 * - Removed temporary code
 * - Adding IP tracking
 * - Adding player count tracking, player count messaging on login
 * Version 0.1.4
 * - Added /ison [player] command
 * - Added partial name matching to most options
 * Version 0.1.5
 * - Playtime for current online session now added to totalplaytime checks
 * Version 0.1.5.1
 * - Database result/statement closing
 * - Minor bugfix in playerstats
 * - Trying to hide legendary/ask viv promo notifications
 * Version 0.1.5.2
 * - Minor sql statement close missed
 * - Disabled join data, since we don't actually log first-joins yet
 * Version 0.1.5.3
 * - Adding auto-reconnect settings to database connection
 * Version 0.1.6
 * - "Not awaiting" promo messages now hidden from joins
 * - Adding rankall command
 * Version 0.1.6.1
 * - Removing inventory save code, since Duties plugin does it better
 * - /rankall ignores people not awaiting, so the list won't explode chat
 * - Adding basic info on how long until next rank
 * - Attempting to fix promo announcements not sending to lead mods
 * Version 0.1.7
 * - Fixing commands so they can be run from the console.
 * - Adding newmod score checking
 * - Adding more connection close/open commands, better connection management
 * Version 0.1.8
 * - Fixing playtime remaining messages to avoid player confusion
 * Version 0.1.8.1
 * - Updated to the new bukkit events
 * Version 0.2
 * - Massive refactor
 * 
 * BUGS:
 * - Rank doesn't count current session?
 * 
 * 
 */


import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class Dhmcstats extends JavaPlugin {
	
	Logger log = Logger.getLogger("Minecraft");
	private me.botsko.dhmcstats.db.DbDAO dao;
	java.sql.Connection conn;
	PermissionManager permissions;
    
    
    /**
     * Connects to the MySQL database
     */
	protected void dbc(){
		
		String mysql_user = this.getConfig().getString("mysql.username");
		String mysql_pass = this.getConfig().getString("mysql.password");
		String mysql_hostname = this.getConfig().getString("mysql.hostname");
		String mysql_database = this.getConfig().getString("mysql.database");
		String mysql_port = this.getConfig().getString("mysql.port");
		
		this.dao = new me.botsko.dhmcstats.db.DbDAOMySQL(mysql_hostname+":"+mysql_port, mysql_database, mysql_user, mysql_pass);

    }
	
	
	/**
	 * Get the Data Access Object for the plugin
	 * @return the DAO of the plugin
	 */
	public me.botsko.dhmcstats.db.DbDAO getDbDAO(){
		return dao;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public PermissionManager getPermissions(){
		return permissions;
	}
	
	
	/**
	 * 
	 */
	private void handleConfig(){
		
		// database configs
		this.getConfig().set("mysql.hostname", 	this.getConfig().getString("mysql.hostname", "127.0.0.1"));
		this.getConfig().set("mysql.port", 		this.getConfig().getString("mysql.port", "3306"));
		this.getConfig().set("mysql.database", 	this.getConfig().getString("mysql.database", "minecraft"));
		this.getConfig().set("mysql.username", 	this.getConfig().getString("mysql.username", "root"));
		this.getConfig().set("mysql.password", 	this.getConfig().getString("mysql.password", ""));
		
		saveConfig();
		
	}


    /**
     * Enables the plugin and activates our player listeners
     */
	public void onEnable(){
		
		this.log("Initializing player listeners");
		
		handleConfig();
		dbc();
		
        // Force a timestamp for any null player_quits, which should only
		// happen if the server crashed and the player_quit even never fired. Since
		// we auto-reboot it's fairly safe to assume to the quit time isn't very far off.
		getDbDAO().forceDateForNullQuits();
		getDbDAO().forcePlaytimeForNullQuits();
		
		// Init event listeners
		getServer().getPluginManager().registerEvents(new DhmcstatsPlayerListener(this), this);
		
		// Init command listeners
		getCommand("played").setExecutor( (CommandExecutor) new PlayedCommandExecutor(this) );
		getCommand("player").setExecutor( (CommandExecutor) new PlayerCommandExecutor(this) );
		getCommand("playerstats").setExecutor( (CommandExecutor) new PlayerstatsCommandExecutor(this) );
		getCommand("rank").setExecutor( (CommandExecutor) new RankCommandExecutor(this) );
		getCommand("rankall").setExecutor( (CommandExecutor) new RankallCommandExecutor(this) );
		getCommand("seen").setExecutor( (CommandExecutor) new SeenCommandExecutor(this) );
		getCommand("ison").setExecutor( (CommandExecutor) new IsonCommandExecutor(this) );
		getCommand("scores").setExecutor( (CommandExecutor) new ScoresCommandExecutor(this) );
		
		// Load PEX
		PluginManager pm = this.getServer().getPluginManager();
		if(pm.isPluginEnabled("PermissionsEx")){
			permissions = PermissionsEx.getPermissionManager();
			this.log("PermissionsEx found.");
		} else {
			this.log("PermissionsEx plugin was not found.");
	    }
	}
 
	
	/**
	 * Shutdown
	 */
	public void onDisable(){
		this.log("Stopping player listeners.");
	}

    
    /**
     * 
     */
    public int getOnlineCount(){
    	return getServer().getOnlinePlayers().length;
    }
    

    /**
     * Partial username matching
     * @param Name
     * @return
     */
    public String expandName(String Name) {
        int m = 0;
        String Result = "";
        for (int n = 0; n < getServer().getOnlinePlayers().length; n++) {
            String str = getServer().getOnlinePlayers()[n].getName();
            if (str.matches("(?i).*" + Name + ".*")) {
                m++;
                Result = str;
                if(m==2) {
                    return null;
                }
            }
            if (str.equalsIgnoreCase(Name))
                return str;
        }
        if (m == 1)
            return Result;
        if (m > 1) {
            return null;
        }
        if (m < 1) {
            return null;
        }
        return null;
    }
    

    /**
	 * 
	 * @param msg
	 * @return
	 */
	public String playerMsg(String msg){
		return ChatColor.GOLD + "[dhmc]: " + ChatColor.WHITE + msg;
	}
	
	
	/**
	 * 
	 * @param msg
	 * @return
	 */
	public String playerError(String msg){
		return ChatColor.GOLD + "[dhmc]: " + ChatColor.RED + msg;
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public void log(String message){
		log.info("[dhmcStats]: " + message);
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public void debug(String message){
		if(this.getConfig().getBoolean("debug")){
			log.info("[dhmcStats]: " + message);
		}
	}
    
    
	/**
	 * Disable the plugin
	 */
	public static void disablePlugin(){
//		this.setEnabled(false);
	}
}