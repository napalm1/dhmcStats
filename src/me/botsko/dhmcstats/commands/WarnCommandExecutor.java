package me.botsko.dhmcstats.commands;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class WarnCommandExecutor implements CommandExecutor  {
	
	/**
	 * 
	 */
	private Dhmcstats plugin;
	
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public WarnCommandExecutor(Dhmcstats plugin) {
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
		
		// /warn [player] [msg]
		if(sender instanceof ConsoleCommandSender || (player != null && player.hasPermission("dhmcstats.warn")) ){
			if(args[0].equalsIgnoreCase("delete")){
				if(args.length == 2){
				
					// delete the warning
					plugin.getDbDAO().deleteWarning( new Integer(args[1]) );
					sender.sendMessage( plugin.playerMsg("Warning deleted successfully."));
					return true;
					
				}
			} else {
				if(args.length >= 2){
					
					String reason = "";
					for (int i = 1; i < args.length; i = i + 1){
						reason += args[i]+" ";
					}
					
					String warned_by = "console";
					if(sender instanceof Player){
						warned_by = player.getName();
					}
					
					sender.sendMessage( plugin.playerMsg("Warning file successfully."));
					
					fileWarning(args[0], reason, warned_by);
					return true;
				}
			}
		}
		return false; 
	}
	
	
	/**
	 * 
	 * @param username
	 * @param reason
	 * @param reporter
	 */
	protected void fileWarning(String username, String reason, String reporter){
		plugin.getDbDAO().fileWarning(username, reason, reporter);
		
		for(Player pl: plugin.getServer().getOnlinePlayers()) {
			if(username.equalsIgnoreCase(pl.getName())){
				pl.sendMessage( plugin.playerMsg("Warning filed for your account: " + ChatColor.RED + reason) );
			}
		}
	}
}