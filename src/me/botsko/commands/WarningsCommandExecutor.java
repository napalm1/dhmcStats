package me.botsko.commands;

import java.sql.SQLException;
import java.util.List;

import me.botsko.dhmcstats.Dhmcstats;
import me.botsko.dhmcstats.db.Warnings;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class WarningsCommandExecutor implements CommandExecutor  {
	
	private Dhmcstats plugin;
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public WarningsCommandExecutor(Dhmcstats plugin) {
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
		
		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
		}
		
		// /warnings (player)
		if(sender instanceof ConsoleCommandSender || (player != null && plugin.getPermissions().has(player, "dhmcstats.warnings")) ){
			
			// If no username found, assume they mean themselves
			String user = "";
			if(args.length == 0){
				if(sender instanceof Player){
					user = player.getName();
				}
			} else {
				user = args[0];
			}

			if(!user.isEmpty()){
				try {
					listWarnings(user, sender);
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				sender.sendMessage( plugin.playerError("Player name must be specified.") );
			}
		}
		return false; 
	}
	
	
	/**
	 * 
	 * @param username
	 * @param sender
	 * @throws SQLException
	 */
    public void listWarnings(String username, CommandSender sender) throws SQLException{
    	
    	sender.sendMessage( plugin.playerMsg( "Warnings filed for " + username + ": " ) );
    	
    	// Pull all items matching this name
		List<Warnings> warnings = plugin.getDbDAO().getPlayerWarnings(username);
		if(!warnings.isEmpty()){
			for(Warnings warn : warnings){
				sender.sendMessage( plugin.playerMsg( "["+ warn.id + "] " + warn.datewarned + ": " + ChatColor.RED + warn.reason ) );
			}
		} else {
			sender.sendMessage( plugin.playerError("No warnings filed.") );
		}
    }
}