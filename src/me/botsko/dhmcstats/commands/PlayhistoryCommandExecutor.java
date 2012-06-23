package me.botsko.dhmcstats.commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
    	
    	HashMap<Integer,String> scores = plugin.getDbDAO().getPayerPlaytimeHistory(username);
    	Iterator<Entry<Integer, String>> it = scores.entrySet().iterator();

    	while (it.hasNext()) {
    		Map.Entry<Integer, String> pairs = (Map.Entry<Integer, String>)it.next();
    		int[] times = splitToComponentTimes(pairs.getKey());
    		sender.sendMessage( plugin.playerMsg( pairs.getValue() + ": " + times[0] + "hrs, " + times[1] + " mins"  ) );
    	}
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