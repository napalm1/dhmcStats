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
			if(args.length == 1){
				getMacro(args[0]);
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
	protected void getMacro(String choice){
		
		if(choice.equalsIgnoreCase("promo")){
			say("&dRank and Promotion Questions? Please read: http://dhmc.us/help/promo");
		}
		if(choice.equalsIgnoreCase("faq")){
			say("&dMap Change Questions? Please read: http://dhmc.us/help/faq");
		}
		if(choice.equalsIgnoreCase("mods")){
			say("&dVive isn't the only mod here. ;)");
		}
		if(choice.equalsIgnoreCase("ban")){
			say("&dBan appeals go on the forums. Admins do not handle appeals here.");
		}
		if(choice.equalsIgnoreCase("myth")){
			say("&dThink you qualify for myth? File a modreq stating your qualifications. Find them here: http://dhmc.us/help/promo/#mythical");
		}
		if(choice.equalsIgnoreCase("poi")){
			say("&dSubmit POIs from our website. We review them all at once. Approved say 'POI' in gallery: http://www.flickr.com/photos/botskonet/");
		}
		if(choice.equalsIgnoreCase("crap")){
			say("&dSorry but we're not falling for your crap.");
		}
		if(choice.equalsIgnoreCase("donate")){
			say("&dDonate at our site by going to http://www.dhmc.us/help/donate/");	 
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