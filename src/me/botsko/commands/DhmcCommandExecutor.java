package me.botsko.commands;

import java.util.ArrayList;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class DhmcCommandExecutor implements CommandExecutor  {
	
	private Dhmcstats plugin;
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public DhmcCommandExecutor(Dhmcstats plugin) {
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
		if(sender instanceof ConsoleCommandSender || (player != null && plugin.getPermissions().has(player, "dhmcstats.admin")) ){
			if(args.length == 1){
				if(args[0].equalsIgnoreCase("sanity"))
				testSanity(player);
				return true;
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
	protected void testSanity(Player player){
		
		ArrayList<String> users = plugin.getDbDAO().getPlayersJoinedNow();
		Player[] players = plugin.getServer().getOnlinePlayers();
		
		player.sendMessage( plugin.playerMsg("Checking sanity:") );
		if(!users.isEmpty()){
			for(String user : users){
				for(Player pl : players){
					if(!pl.getName().equalsIgnoreCase(user)){
						player.sendMessage( plugin.playerError("User " + user + " has open join, but is not online.") );
					}
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param msg
	 */
	protected void say(String msg){
		
		for(Player pl : plugin.getServer().getOnlinePlayers()) {
    		pl.sendMessage( ChatColor.AQUA + "[dhmcRemindsYou]: " + plugin.colorize(msg) );
    	}
	}
}