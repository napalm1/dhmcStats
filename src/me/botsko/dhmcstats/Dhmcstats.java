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
 */

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import me.botsko.commands.IsonCommandExecutor;
import me.botsko.commands.MacroCommandExecutor;
import me.botsko.commands.PlayedCommandExecutor;
import me.botsko.commands.PlayerCommandExecutor;
import me.botsko.commands.PlayerstatsCommandExecutor;
import me.botsko.commands.PlayhistoryCommandExecutor;
import me.botsko.commands.RankCommandExecutor;
import me.botsko.commands.RankallCommandExecutor;
import me.botsko.commands.ScoresCommandExecutor;
import me.botsko.commands.SeenCommandExecutor;
import me.botsko.commands.WarnCommandExecutor;
import me.botsko.commands.WarningsCommandExecutor;
import me.botsko.dhmcstats.db.DbDAOMySQL;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class Dhmcstats extends JavaPlugin {
	
	Logger log = Logger.getLogger("Minecraft");
	private DbDAOMySQL dao;
	public java.sql.Connection conn;
	PermissionManager permissions;
	int last_announcement = 0;
    
	
	/**
     * Connects to the MySQL database
     */
	public void dbc(){
		
		String mysql_user = this.getConfig().getString("mysql.username");
		String mysql_pass = this.getConfig().getString("mysql.password");
		String mysql_hostname = this.getConfig().getString("mysql.hostname");
		String mysql_database = this.getConfig().getString("mysql.database");
		String mysql_port = this.getConfig().getString("mysql.port");
    	
        java.util.Properties conProperties = new java.util.Properties();
        conProperties.put("user", mysql_user );
        conProperties.put("password", mysql_pass );
        conProperties.put("autoReconnect", "true");
        conProperties.put("maxReconnects", "3");
        String uri = "jdbc:mysql://"+mysql_hostname+":"+mysql_port+"/"+mysql_database;
        
        try {
        	conn = DriverManager.getConnection(uri, conProperties);
        	if (conn == null || conn.isClosed() || !conn.isValid(1)){
        		this.log("Mysql connection failed to open");
        	}
        	this.dao = new DbDAOMySQL(this);
        } catch (SQLException e) {
        	e.printStackTrace();
        }
    }
	
	
	/**
	 * Get the Data Access Object for the plugin
	 * @return the DAO of the plugin
	 */
	public DbDAOMySQL getDbDAO(){
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
		//getDbDAO().removeInvalidJoins();
		getDbDAO().forceDateForNullQuits();
		getDbDAO().forcePlaytimeForNullQuits();
		
		// Init event listeners
		getServer().getPluginManager().registerEvents(new DhmcstatsPlayerListener(this), this);
		
		// Init command listeners
		getCommand("played").setExecutor( (CommandExecutor) new PlayedCommandExecutor(this) );
		getCommand("playhist").setExecutor( (CommandExecutor) new PlayhistoryCommandExecutor(this) );
		getCommand("player").setExecutor( (CommandExecutor) new PlayerCommandExecutor(this) );
		getCommand("playerstats").setExecutor( (CommandExecutor) new PlayerstatsCommandExecutor(this) );
		getCommand("rank").setExecutor( (CommandExecutor) new RankCommandExecutor(this) );
		getCommand("rankall").setExecutor( (CommandExecutor) new RankallCommandExecutor(this) );
		getCommand("seen").setExecutor( (CommandExecutor) new SeenCommandExecutor(this) );
		getCommand("ison").setExecutor( (CommandExecutor) new IsonCommandExecutor(this) );
		getCommand("scores").setExecutor( (CommandExecutor) new ScoresCommandExecutor(this) );
		getCommand("warn").setExecutor( (CommandExecutor) new WarnCommandExecutor(this) );
		getCommand("warnings").setExecutor( (CommandExecutor) new WarningsCommandExecutor(this) );
		getCommand("z").setExecutor( (CommandExecutor) new MacroCommandExecutor(this) );
		
		// Init scheduled
		catchUncaughtDisconnects();
		runAnnouncements();
		
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
	 * If a user disconnects in an unknown way that is never caught by onPlayerQuit,
	 * this will force close all records except for players currently online.
	 */
	public void catchUncaughtDisconnects(){
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

		    public void run() {
		        
		    	String on_users = "";
				for(Player pl: getServer().getOnlinePlayers()) {
					on_users += "'"+pl.getName()+"',";
				}
				if(!on_users.isEmpty()){
					on_users = on_users.substring(0, on_users.length()-1);
				}
				getDbDAO().forceDateForOfflinePlayers( on_users );
				getDbDAO().forcePlaytimeForOfflinePlayers( on_users );
				log("Catching uncaught disconnects.");
		    	
		    }
		}, 6000L, 6000L);
	}
	
	
	/**
	 * If a user disconnects in an unknown way that is never caught by onPlayerQuit,
	 * this will force close all records except for players currently online.
	 */
	public void runAnnouncements(){
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

		    public void run() {
		    	
		    	// Pull all items matching this name
				List<String> announces = getDbDAO().getActiveAnnouncements();
				if(!announces.isEmpty()){
					
					if(last_announcement >= announces.size()){
						last_announcement = 0;
					}
					
					String msg = announces.get(last_announcement);
					for(Player pl : getServer().getOnlinePlayers()) {
			    		pl.sendMessage( colorize(msg) );
			    	}
					log(colorize(msg));
					
					last_announcement++;
				}
		    }
		}, 6000L, 6000L);
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
	 * Converts colors place-holders.
	 * @param text
	 * @return
	 */
	public String colorize(String text){
        String colorized = text.replaceAll("(&([a-f0-9A-F]))", "\u00A7$2");
        return colorized;
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