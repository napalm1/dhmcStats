package me.botsko.dhmcstats.commands;

import java.sql.SQLException;
import java.text.ParseException;

import me.botsko.dhmcstats.Dhmcstats;
import me.botsko.dhmcstats.playtime.Playtime;
import me.botsko.dhmcstats.playtime.PlaytimeUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class PlayedCommandExecutor implements CommandExecutor  {
	
	/**
	 * 
	 */
	private Dhmcstats plugin;
	
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public PlayedCommandExecutor(Dhmcstats plugin) {
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
    		if(player.hasPermission("dhmcstats.played")){
	    		String user = (args.length == 1 ? args[0] : player.getName());
				try {
					checkPlayTime( user, sender );
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
    		}
    	}
		return false; 
	}
	
	
	/**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     * @throws ParseException 
     */
    public void checkPlayTime(String username, CommandSender sender) throws SQLException, ParseException {

    	username = plugin.expandName(username);
		Playtime playtime = PlaytimeUtil.getPlaytime( plugin, username );
		sender.sendMessage(ChatColor.GOLD + username + " has played for " + playtime.getHours() + " hours, " + playtime.getMinutes() + " minutes, and " + playtime.getSeconds() + " seconds. Nice!");
		
    }
}