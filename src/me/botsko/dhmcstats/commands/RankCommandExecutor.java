package me.botsko.dhmcstats.commands;

import java.text.ParseException;

import me.botsko.dhmcstats.Dhmcstats;
import me.botsko.dhmcstats.rank.Rank;
import me.botsko.dhmcstats.rank.RankUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class RankCommandExecutor implements CommandExecutor  {
	
	/**
	 * 
	 */
	private Dhmcstats plugin;
	
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public RankCommandExecutor(Dhmcstats plugin) {
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
    		if(player.hasPermission("dhmcstats.rank")){
    			
				// Expand partials
    			String user = (args.length == 1 ? args[0] : player.getName());
		    	String tmp = plugin.expandName(user);
		    	
		    	Rank rank = null;
				try {
					rank = RankUtil.getPlayerRank( plugin, tmp );
				} catch (ParseException e) {
					e.printStackTrace();
				}
		    	
				if(rank != null){
			    	// msg the player time remaining
					player.sendMessage( plugin.playerMsg( rank.getPromotionStatusMessage( tmp, (player.getName().equalsIgnoreCase(tmp)) ) ) );
					
					if(rank.getPlayerQualifiesForPromo()){
						try{
							// auto promote
							plugin.permissions.getUser(tmp).promote( plugin.permissions.getUser("viveleroi"), "default" );
						
							// announce the promotion
							plugin.messageAllPlayers( plugin.playerMsg( "Congratulations, " + ChatColor.AQUA + tmp + ChatColor.WHITE + " on your promotion to " + ChatColor.AQUA + rank.getNextRank().getNiceName() ) );
						
							// log the promotion
							plugin.log("Auto promoted " + tmp + " to " + rank.getNextRank().getNiceName());
						} catch(Exception e){
							e.printStackTrace();
						}
					}
				}
				
				return true;
				
			}
    	}
		return false; 
	}
}