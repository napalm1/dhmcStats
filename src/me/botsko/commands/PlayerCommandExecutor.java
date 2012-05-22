package me.botsko.commands;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import me.botsko.dhmcstats.Dhmcstats;
import me.botsko.dhmcstats.db.Alts;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

public class PlayerCommandExecutor implements CommandExecutor  {
	
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
    		
    		if(sender instanceof ConsoleCommandSender || (player != null && plugin.getPermissions().has(player, "dhmcstats.player")) ){
    			
    			if (args.length == 2){
    				
    				if(args[0].equalsIgnoreCase("alts")){
    					playerAlts( args[1], sender);
    					return true;
    				}
    				
    			} else {
    			
	    			if (args.length == 1){
						try {
							playerStats( args[0], sender );
							return true;
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			} else {
						try {
							playerStats( player.getName(), sender );
							return true;
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
    	
    	PermissionUser user = plugin.getPermissions().getUser( username );
    	
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
		List<Alts> alt_accts = plugin.getDbDAO().getPlayerAlts(username);
		if(!alt_accts.isEmpty()){
			for(Alts alt : alt_accts){
				sender.sendMessage( plugin.playerMsg( "["+ alt.ip + "] " + ChatColor.GRAY + alt.username ));
			}
		}
    	
    }
}
