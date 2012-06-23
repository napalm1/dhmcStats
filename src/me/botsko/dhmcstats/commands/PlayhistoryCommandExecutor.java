package me.botsko.dhmcstats.commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import me.botsko.dhmcstats.Dhmcstats;
import me.botsko.dhmcstats.playtime.Playtime;
import me.botsko.dhmcstats.playtime.PlaytimeUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class PlayhistoryCommandExecutor implements CommandExecutor  {
	
	/**
	 * 
	 */
	private Dhmcstats plugin;
	
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public PlayhistoryCommandExecutor(Dhmcstats plugin) {
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
    	if (sender instanceof Player) {
    		Player player = (Player) sender;
    		if(player.hasPermission("dhmcstats.played")){
	    		String user = (args.length == 1 ? args[0] : player.getName());
				try {
					checkPlayHistory( user, sender );
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
				}
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
    public void checkPlayHistory(String username, CommandSender sender) throws SQLException{
    	
    	sender.sendMessage( plugin.playerMsg( "Most recent 7 days of playtime for " + username + ": " ) );
    	
    	HashMap<Playtime,String> scores = PlaytimeUtil.getPlayerPlaytimeHistory( plugin, username );
    	Iterator<Entry<Playtime, String>> it = scores.entrySet().iterator();
    	while (it.hasNext()) {
    		Map.Entry<Playtime, String> pairs = (Map.Entry<Playtime, String>)it.next();
    		Playtime pt = pairs.getKey();
    		sender.sendMessage( plugin.playerMsg( pairs.getValue() + ": " + pt.getHours() + "hrs, " + pt.getMinutes() + " mins"  ) );
    	}
    }
}