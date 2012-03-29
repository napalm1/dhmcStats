package me.botsko.commands;

import java.sql.SQLException;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import com.nijikokun.register.payment.Methods;

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
				if (args.length == 1)
					try {
						playerStats( args[0], sender );
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				else
					try {
						playerStats( player.getName(), sender );
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
    private void playerStats(String username, CommandSender sender) throws SQLException {
    	
    	// Expand partials
    	String tmp = plugin.expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}
    
//    	checkPlayTime(username, sender);
//    	checkSeen(username, sender);
//    	getQualifyFor(username, sender);
    	
    	Double bal = Methods.getMethod().getAccount( username ).balance();
    	sender.sendMessage(ChatColor.GOLD + "Money: $" + bal);
    	
    	PermissionUser user = plugin.getPermissions().getUser( username );
    	
    	String delim = "";
        for ( PermissionGroup group : user.getGroups()) {
            delim += group.getName() + ", ";
        }

        sender.sendMessage(ChatColor.GOLD + "Groups: " + delim);
    	
    }
}
