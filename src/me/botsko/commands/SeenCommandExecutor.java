package me.botsko.commands;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class SeenCommandExecutor implements CommandExecutor  {
	
	private Dhmcstats plugin;
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public SeenCommandExecutor(Dhmcstats plugin) {
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
    		
    		if(sender instanceof ConsoleCommandSender || (player != null && plugin.getPermissions().has(player, "dhmcstats.seen")) ){
    			
    			String user = (args.length == 1 ? args[0] : player.getName());
    			try {
    				checkSeen( user, sender );
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
    private void checkSeen(String username, CommandSender sender) throws SQLException, ParseException{
    	
    	// Expand partials
    	String tmp = plugin.expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}

    	Date joined = plugin.getDbDAO().getPlayerFirstSeen(username);
    	sender.sendMessage( plugin.playerMsg("Joined " + joined) );
    	
    	Date seen = plugin.getDbDAO().getPlayerLastSeen(username);
    	sender.sendMessage( plugin.playerMsg("Last Seen " + seen) );
		
    }
}