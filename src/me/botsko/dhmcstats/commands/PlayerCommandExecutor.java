package me.botsko.dhmcstats.commands;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import me.botsko.dhmcstats.Dhmcstats;
import me.botsko.dhmcstats.joins.Alts;
import me.botsko.dhmcstats.joins.JoinUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

public class PlayerCommandExecutor implements CommandExecutor  {
	
	/**
	 * 
	 */
	private Dhmcstats plugin;
	
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public PlayerCommandExecutor(Dhmcstats plugin) {
		this.plugin = plugin;
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
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) throws IllegalPluginAccessException {
		
		// Is a player issue this command?
    	if (sender instanceof Player) {
    		
    		Player player = (Player) sender;
    		
    		if(player.hasPermission("dhmcstats.player")){
    			
    			// Player alts
    			if (args.length == 2 && args[0].equalsIgnoreCase("alts")){
    				
					playerAlts( args[1], sender);
					return true;
					
    			} else {
    				
    				// Checking stats of player
    				String user = (args.length == 1 ? args[0] : player.getName());
					try {
						playerStats( user, sender );
						return true;
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
    			}
			}
    	}

		return false; 
		
	}

	
    /**
     * Returns a bunch of player stats
     * 
     * @param username
     * @throws SQLException 
     * @throws ParseException 
     */
    private void playerStats(String username, CommandSender sender) throws SQLException, ParseException {
    	
    	// Expand partials
    	String tmp = plugin.expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}
    	
    	SeenCommandExecutor seen = new SeenCommandExecutor(plugin);
    	seen.checkSeen(username, sender);
    	
    	PlayedCommandExecutor played = new PlayedCommandExecutor(plugin);
    	played.checkPlayTime(username, sender);
    	
    	Bukkit.dispatchCommand(sender, "lookup " + username);
    	Bukkit.dispatchCommand(sender, "warnings " + username);
    	
    	PermissionUser user = plugin.permissions.getUser( username );
    	
    	String delim = "";
        for ( PermissionGroup group : user.getGroups()) {
            delim += group.getName() + ", ";
        }

        sender.sendMessage(ChatColor.GOLD + "Groups: " + delim);
    	
    }
    
    /**
     * 
     * @param username
     * @param sender
     */
    private void playerAlts(String username, CommandSender sender){
    	
    	sender.sendMessage( plugin.playerMsg( "Comparing IPs for " + username + " - showing *possible* alt accounts: " ) );
    	
    	// Pull all items matching this name
		List<Alts> alt_accts = JoinUtil.getPlayerAlts( plugin, username );
		if(!alt_accts.isEmpty()){
			for(Alts alt : alt_accts){
				sender.sendMessage( plugin.playerMsg( "["+ alt.ip + "] " + ChatColor.GRAY + alt.username ));
			}
		}
    }
}
