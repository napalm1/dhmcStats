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

import java.sql.Connection;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

import me.botsko.dhmcstats.announcements.AnnouncementUtil;
import me.botsko.dhmcstats.commands.FromCommandExecutor;
import me.botsko.dhmcstats.commands.IsonCommandExecutor;
import me.botsko.dhmcstats.commands.MacroCommandExecutor;
import me.botsko.dhmcstats.commands.PlayedCommandExecutor;
import me.botsko.dhmcstats.commands.PlayerCommandExecutor;
import me.botsko.dhmcstats.commands.PlayerstatsCommandExecutor;
import me.botsko.dhmcstats.commands.PlayhistoryCommandExecutor;
import me.botsko.dhmcstats.commands.RankCommandExecutor;
import me.botsko.dhmcstats.commands.SeenCommandExecutor;
import me.botsko.dhmcstats.commands.WarnCommandExecutor;
import me.botsko.dhmcstats.commands.WarningsCommandExecutor;
import me.botsko.dhmcstats.joins.JoinUtil;
import me.botsko.dhmcstats.listeners.DhmcstatsPlayerListener;
import me.botsko.dhmcstats.rank.Rank;
import me.botsko.dhmcstats.rank.RankUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.exceptions.RankingException;


public class Dhmcstats extends JavaPlugin {
	
	Logger log = Logger.getLogger("Minecraft");
	public PermissionManager permissions;
	int last_announcement = 0;
	public Dhmcstats dhmc;
	
	public Connection conn = null;
	public String mysql_user;
	public String mysql_pass;
	public String mysql_hostname;
	public String mysql_database;
	public String mysql_port;
    
	
	/**
     * Setup a generic connection all non-scheduled methods may share
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @return true if we successfully connected to the db.
     */
	public void dbc(){
		Mysql mysql = new Mysql(mysql_user, mysql_pass, mysql_hostname, mysql_database, mysql_port);
		conn = mysql.getConn();
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
		
		mysql_user = this.getConfig().getString("mysql.username");
		mysql_pass = this.getConfig().getString("mysql.password");
		mysql_hostname = this.getConfig().getString("mysql.hostname");
		mysql_database = this.getConfig().getString("mysql.database");
		mysql_port = this.getConfig().getString("mysql.port");
		
	}


    /**
     * Enables the plugin and activates our player listeners
     */
	public void onEnable(){
		
		dhmc = this;
		
		this.log("Initializing player listeners");
		
		handleConfig();
		dbc();
		
		// Ensure the join data is clean
		JoinUtil.startupDbChecks(this);
		
		
		/**
		 * Event listeners
		 */
		getServer().getPluginManager().registerEvents(new DhmcstatsPlayerListener(this), this);
		
		
		/**
		 * Commands
		 */
		getCommand("played").setExecutor( (CommandExecutor) new PlayedCommandExecutor(this) );
		getCommand("playhist").setExecutor( (CommandExecutor) new PlayhistoryCommandExecutor(this) );
		getCommand("player").setExecutor( (CommandExecutor) new PlayerCommandExecutor(this) );
		getCommand("playerstats").setExecutor( (CommandExecutor) new PlayerstatsCommandExecutor(this) );
		getCommand("rank").setExecutor( (CommandExecutor) new RankCommandExecutor(this) );
		getCommand("seen").setExecutor( (CommandExecutor) new SeenCommandExecutor(this) );
		getCommand("ison").setExecutor( (CommandExecutor) new IsonCommandExecutor(this) );
		getCommand("warn").setExecutor( (CommandExecutor) new WarnCommandExecutor(this) );
		getCommand("warnings").setExecutor( (CommandExecutor) new WarningsCommandExecutor(this) );
		getCommand("z").setExecutor( (CommandExecutor) new MacroCommandExecutor(this) );
		getCommand("from").setExecutor( (CommandExecutor) new FromCommandExecutor(this) );
		
		/**
		 * Scheduled events
		 */
		catchUncaughtDisconnects();
		runAnnouncements();
		rankAll();
		
		
		/**
		 * Load PermissionsEX
		 */
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
				JoinUtil.catchDisconnects( dhmc, on_users );
		    }
		}, 1200L, 1200L);
	}
	
	
	/**
	 * If a user disconnects in an unknown way that is never caught by onPlayerQuit,
	 * this will force close all records except for players currently online.
	 */
	public void runAnnouncements(){
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

		    public void run() {

		    	// Pull all items matching this name
				List<String> announces = AnnouncementUtil.getActiveAnnouncements( dhmc );
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
	 * Run auto-promotion rank checks on all players, every fifteen minutes
	 */
	public void rankAll(){
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

		    public void run() {
		    	for(Player pl : getServer().getOnlinePlayers()) {
		            try {
		            	String username = pl.getName();
		    			Rank rank = RankUtil.getPlayerRank( dhmc, username );
		    			if(rank.getPlayerQualifiesForPromo()){
		    				
		    				// auto promote
		    				permissions.getUser( username ).promote( permissions.getUser("viveleroi"), "default" );
		    				
		    				// announce the promotion
		    				messageAllPlayers( playerMsg( "Congratulations, " + ChatColor.AQUA + username + ChatColor.WHITE + " on your promotion to " + ChatColor.AQUA + rank.getNextRank().getNiceName() ) );
		    				
		    				// log the promotion
		    				log("Auto promoted " + username + " to " + rank.getNextRank().getNiceName());
		    				
		    			}
		    		} catch (ParseException e) {
		    			e.printStackTrace();
		    		} catch (RankingException e) {
						e.printStackTrace();
					}
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
            return Name;
        }
        if (m < 1) {
            return Name;
        }
        return Name;
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
	 */
	public void messageAllPlayers(String msg){
		for(Player pl : getServer().getOnlinePlayers()) {
    		pl.sendMessage( msg );
    	}
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