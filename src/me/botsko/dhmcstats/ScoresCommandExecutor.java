package me.botsko.dhmcstats;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class ScoresCommandExecutor implements CommandExecutor  {
	
	private Dhmcstats plugin;
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public ScoresCommandExecutor(Dhmcstats plugin) {
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
    		
    		if(sender instanceof ConsoleCommandSender || (player != null && plugin.getPermissions().has(player, "dhmcstats.scores")) ){
    			
    			String user = (args.length == 1 ? args[0] : player.getName());
    			try {
    				checkScores( user, sender );
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
    	}

		return false; 
		
	}
	
	
	/**
     * Checks the newmod scores of a user
     * 
     * @param username
     * @throws SQLException 
     */
    public void checkScores(String username, CommandSender sender) throws SQLException{
    	
    	sender.sendMessage( plugin.playerMsg( "NewMod Quiz scores for " + username + ": " ) );
    	
    	HashMap<Float,String> scores = plugin.getDbDAO().getPlayerNewModQuizScores(username);
    	Iterator<Entry<Float, String>> it = scores.entrySet().iterator();

    	while (it.hasNext()) {
    		Map.Entry<Float, String> pairs = (Map.Entry<Float, String>)it.next();
    		sender.sendMessage( plugin.playerMsg( pairs.getValue() + ": " + pairs.getKey() + "%" ) );
    	}
    }
}