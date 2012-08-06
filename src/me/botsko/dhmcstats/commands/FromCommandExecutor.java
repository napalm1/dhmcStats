package me.botsko.dhmcstats.commands;

import me.botsko.dhmcstats.Dhmcstats;
import me.botsko.dhmcstats.from.FromUtil;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;

public class FromCommandExecutor implements CommandExecutor  {
	
	/**
	 * 
	 */
	private Dhmcstats plugin;
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public FromCommandExecutor(Dhmcstats plugin) {
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
		if (args.length >= 1){
			if (sender instanceof Player) {
	    		Player player = (Player) sender;
			 
				String reason = "";
				for (int i = 0; i < args.length; i = i + 1){
					reason += args[i]+" ";
				}
				
				// If the have no existing referral, save it
				if(!FromUtil.hasReferral(plugin, player.getName())){
					
					plugin.log("Referral: " + player.getName() + " came from: " + reason);
					
					// save
					FromUtil.add(plugin, player.getName(), reason);
					
					// reward player
					ItemStack i = new ItemStack(Material.DIAMOND_PICKAXE, 1);
					i.addEnchantment(Enchantment.DURABILITY, 1);
					player.getInventory().addItem( i );
					
					player.sendMessage( plugin.playerMsg("Thanks! Here's your reward!") );
					
				} else {
					player.sendMessage( plugin.playerError("Sorry, only once per player!") );
				}
			}
			return true;
		}
		return false; 
	}
}