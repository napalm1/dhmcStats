package me.botsko.dhmcstats.commands;

import java.sql.SQLException;
import java.text.ParseException;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
    	
    	// Expand partials
    	String tmp = plugin.expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}
    
		int playtime = plugin.getDbDAO().getPlaytime(username);
		int[] times = splitToComponentTimes(playtime);
		sender.sendMessage(ChatColor.GOLD + username + " has played for " + times[0] + " hours, " + times[1] + " minutes, and " + times[2] + " seconds. Nice!");
		
    }
    
    
    /**
     * Convert seconds into hours/mins/secs
     * 
     * @param biggy
     * @return
     */
    private static int[] splitToComponentTimes(int biggy){
        int hours = (int) biggy / 3600;
        int remainder = (int) biggy - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;
        int[] ints = {hours , mins , secs};
        return ints;
    }
}