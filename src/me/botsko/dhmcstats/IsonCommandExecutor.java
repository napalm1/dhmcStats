package me.botsko.dhmcstats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.IllegalPluginAccessException;

public class IsonCommandExecutor implements CommandExecutor  {
	
	private Dhmcstats plugin;
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public IsonCommandExecutor(Dhmcstats plugin) {
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
		
		if (args.length == 1){
			 String ison = plugin.expandName(args[0]);
			 if(ison != null){
				 sender.sendMessage( plugin.playerMsg( ison + " is online" ) ); 
			 } else {
				 sender.sendMessage( plugin.playerError( args[0] + " is online" ) ); 
			 }
		}
		return false; 
	}
}