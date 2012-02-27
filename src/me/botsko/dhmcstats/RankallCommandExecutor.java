package me.botsko.dhmcstats;

import java.sql.SQLException;
import java.text.ParseException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

import ru.tehkode.permissions.PermissionUser;

public class RankallCommandExecutor implements CommandExecutor  {
	
	private Dhmcstats plugin;
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public RankallCommandExecutor(Dhmcstats plugin) {
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
    		
    		if(sender instanceof ConsoleCommandSender || (player != null && plugin.getPermissions().has(player, "dhmcstats.rank")) ){
				rankAll( sender );
				return true;
			}
    	}

		return false; 
		
	}

	
	/**
     * Check all online players for promo
     * @param sender
     */
    public void rankAll(CommandSender sender){
    	
    	sender.sendMessage( plugin.playerMsg( "Checking... (showing only those who qualify)") );
    	
    	for(Player pl: plugin.getServer().getOnlinePlayers()) {
    	
	    	// Check the user qualifies for any rank, alert mods
	        String promo = "";
	        PermissionUser user = plugin.getPermissions().getUser( pl.getName() );
	        if( 
	        	!user.inGroup( "LegendaryPlayer" ) &&
	        	!user.inGroup( "MythicalPlayer" ) &&
	        	!user.inGroup( "NewModerator" ) &&
	        	!user.inGroup( "Moderator") &&
	        	!user.inGroup( "LeadModerator" ) &&
	        	!user.inGroup( "WorldEditor" ) &&
	        	!user.inGroup( "Admin" )
	        ){
	        	try {
	        		RankCommandExecutor rce = new RankCommandExecutor(plugin);
					promo = rce.checkQualifiesFor( pl.getName(), sender );
					if(promo.indexOf("qualify") > 1 || promo.indexOf("qualifies") > 1){
						sender.sendMessage( plugin.playerMsg( promo ) );
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
	        }
    	}
    }
}