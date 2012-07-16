package me.botsko.dhmcstats.commands;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class MacroCommandExecutor implements CommandExecutor  {
	
	/**
	 * 
	 */
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
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) throws IllegalPluginAccessException {
		
		if(sender instanceof Player){
			Player player = (Player) sender;
			if(player.hasPermission("dhmcstats.macro")){
				if(args.length == 1){
					getMacro(args[0]);
					return true;
				}
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
//		if(choice.equalsIgnoreCase("faq")){
//			say("&dMap Change Questions? Please read: http://dhmc.us/help/faq");
//		}
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
			say("&dSubmit your creation on our website (dhmc.us). Staff vote them up/down. 15 votes you win a Point of Interest.");
		}
		if(choice.equalsIgnoreCase("website")){
			say("&dOur website: http://dhmc.us - Forums, news, gallery, contests, and more! Create account here: http://dhmc.us/users/signup/");
		}
		
		// Temporary ones
		if(choice.equalsIgnoreCase("reset")){
			say("&dSounds like you didn't read the Seriously Important 1.3 Guide: http://bit.ly/S6HYY5");
		}
	}
	
	
	/**
	 * 
	 * @param msg
	 */
	protected void say(String msg){
		plugin.messageAllPlayers(ChatColor.AQUA + "[dhmcRemindsYou]: " + plugin.colorize(msg));
	}
}