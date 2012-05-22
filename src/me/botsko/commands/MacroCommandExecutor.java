package me.botsko.commands;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class MacroCommandExecutor implements CommandExecutor  {
	
	private Dhmcstats plugin;
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public MacroCommandExecutor(Dhmcstats plugin) {
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
		if(sender instanceof ConsoleCommandSender || (player != null && plugin.getPermissions().has(player, "dhmcstats.macro")) ){
			String send_to_player = "";
			if(args.length >= 1){
				if(args.length == 2){
					send_to_player = args[1];
				}
				getMacro(args[0], send_to_player);
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
	protected void getMacro(String choice, String player){
		
		if(choice.equalsIgnoreCase("promo")){
			say(player, "&dRank and Promotion Questions? Please read: http://dhmc.us/help/promo");
		}
		if(choice.equalsIgnoreCase("faq")){
			say(player, "&dMap Change Questions? Please read: http://dhmc.us/help/faq");
		}
		if(choice.equalsIgnoreCase("mods")){
			say(player, "&dVive isn't the only mod here. ;)");
		}
		if(choice.equalsIgnoreCase("ban")){
			say(player, "&dBan appeals go on the forums. Admins do not handle appeals here.");
		}
		if(choice.equalsIgnoreCase("myth")){
			say(player, "&dThink you qualify for myth? File a modreq stating your qualifications. Find them here: http://dhmc.us/help/promo/#mythical");
		}
		if(choice.equalsIgnoreCase("poi")){
			say(player, "&dSubmit POIs from our website. We review them all at once. Approved say 'POI' in gallery: http://www.flickr.com/photos/botskonet/");
		}
		if(choice.equalsIgnoreCase("crap")){
			say(player, "&dSorry but we're not falling for your crap.");
		}
	}
	
	
	/**
	 * 
	 * @param msg
	 */
	protected void say(String player, String msg){
		
		for(Player pl : plugin.getServer().getOnlinePlayers()) {
			if( player.equalsIgnoreCase("") || player.equalsIgnoreCase(pl.getName()) ){
				pl.sendMessage( ChatColor.AQUA + "[dhmcRemindsYou]: " + plugin.colorize(msg) );
			}
    	}
	}
}