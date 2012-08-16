package me.botsko.dhmcstats.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class MacroCommandExecutor implements CommandExecutor  {
	
	protected Map<String, String> macros = new HashMap<String, String>();
	
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
		
		macros.put("promo", "&dRank and Promotion Questions? Please read: http://dhmc.us/help/promo");
		macros.put("mods", "&dRemember, staff can answer your questions too, and they're less busy than Vive.");
		macros.put("ban", "&dBan appeals go on the forums. Admins do not handle appeals here.");
		macros.put("myth", "&dThink you qualify for myth? File a modreq stating your qualifications. Find them here: http://dhmc.us/help/promo");
		macros.put("poi", "&dSubmit your creation on our website (dhmc.us). Staff vote them up/down. 15 votes you win a Point of Interest.");
		macros.put("site", "&dOur website: http://dhmc.us - Forums, news, gallery, contests, and more! Create account here: http://dhmc.us/users/signup/");
		macros.put("donate", "&dDonate at our site by going to http://www.dhmc.us/help/donate/");
		macros.put("see", "&dSorry Vive can't see your stuff. We just have too many people asking. Submit it as a Creation on the site!");
		macros.put("plugins", "&dNeed help for Craftys, DarkMythos, etc? http://dhmc.us/wiki/");
		macros.put("rules", "&dMake sure you read the rules at http://www.dhmc.us/help/rules");
		macros.put("snowy", "&dHave feedback, a suggestion, or a problem? Report it at http://bit.ly/Nse5O5");
		
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
					
					if(args[0].equalsIgnoreCase("list")){
						listMacros( player );
					} else {
						getMacro(args[0]);
					}
					return true;
				}
			}
		}
		return false; 
	}
	
	
	/**
	 * 
	 * @param player
	 */
	public void listMacros( Player player ){
		for (Entry<String, String> entry : macros.entrySet()){
		    player.sendMessage( ChatColor.GOLD + entry.getKey().toString() + ": " + plugin.colorize( entry.getValue().toString().substring(0, 35) + "..." ));
		}
	}
	
	
	/**
	 * 
	 * @param username
	 * @param reason
	 * @param reporter
	 */
	protected void getMacro(String choice){
		if(macros.containsKey(choice)){
			say(macros.get(choice));
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