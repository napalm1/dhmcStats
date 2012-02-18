package me.botsko.dhmcstats;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class RankCommandExecutor implements CommandExecutor  {
	
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
		
		// Is a player issue this command?
    	if (sender instanceof Player) {
    		
    		Player player = (Player) sender;
    		
    		if(sender instanceof ConsoleCommandSender || (player != null && plugin.getPermissions().has(player, "dhmcstats.rank")) ){	
				if (args.length == 1)
					try {
						getQualifyFor( args[0], sender );
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				else
					try {
						getQualifyFor( player.getName(), sender );
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
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
     * @throws ParseException
     */
    public void getQualifyFor(String username, CommandSender sender) throws SQLException, ParseException {
    	sender.sendMessage( checkQualifiesFor(username) );
    }
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     * @throws ParseException 
     */
    public String checkQualifiesFor(String username) throws SQLException, ParseException {
    	
    	// Expand partials
    	String tmp = plugin.expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}
    	
    	String msg = "";
    	
    	// get the base join date
    	Date joined = plugin.getDbDAO().getPlayerFirstSeen(username);
    	Date today = new Date();
    	long diff = today.getTime() - joined.getTime();
    	int days = (int) ((diff / 1000) / 86400);
    	
    	// Get the play time
    	int hours = plugin.getDbDAO().getPlaytime(username) / 3600;
    	
    	// Promotion checks per group
    	if(plugin.getPermissions().getUser(username).inGroup("Admin")){
    		msg = ChatColor.GOLD + username + " is an admin. Nowhere to go man!";
    	}
    	else if(plugin.getPermissions().getUser(username).inGroup("LeadModerator")){
    		msg = ChatColor.GOLD + username + " is a lead mod. Ask Vive";
    	}
    	else if(plugin.getPermissions().getUser(username).inGroup("Moderator")){
    		msg = ChatColor.GOLD + username + " is a mod. Ask Vive";
    	}
    	else if(plugin.getPermissions().getUser(username).inGroup("NewModerator")){
    		msg = ChatColor.GOLD + username + " is a new mod. Ask Vive";
    	}
    	else if(plugin.getPermissions().getUser(username).inGroup("MythicalPlayer")){
    		msg = ChatColor.GOLD + username + " is Myth. Ask Vive";
    	}
    	else if(plugin.getPermissions().getUser(username).inGroup("LegendaryPlayer")){
    		msg = ChatColor.GOLD + username + " is Legendary. Check their skills, etc.";
    	}
    	else if(plugin.getPermissions().getUser(username).inGroup("RespectedPlayer")){
    		if(days >= 25 && hours >= 80){
    			msg = ChatColor.GOLD + username + " qualifies for: " + ChatColor.AQUA + " Legendary";
    		} else {
    			msg = ChatColor.GOLD + username + " is not awaiting promotion. " + timeRemaining("Legendary", 25, days, 80, hours);
    		}
    	}
    	else if(plugin.getPermissions().getUser(username).inGroup("TrustedPlayer")){
    		if(days >= 5 && hours >= 20){
    			msg = ChatColor.GOLD + username + " qualifies for: " + ChatColor.AQUA + " Respected";
    		} else {
    			msg = ChatColor.GOLD + username + " is not awaiting promotion. " + timeRemaining("Respected", 5, days, 20, hours);
    		}
    	}
    	else {
    		if(days >= 1 && hours >= 5){
    			msg = ChatColor.GOLD + username + " qualifies for: " + ChatColor.AQUA + " Trusted";
    		} else {
    			msg = ChatColor.GOLD + username + " is not awaiting promotion. " + timeRemaining("Trusted", 1, days, 5, hours);
    		}
    	}
		return msg;
    }
    
    
    /**
     * Improves the message about remaining time to avoid confusion with the negative numbers.
     * @param rank
     * @param min_days
     * @param days
     * @param min_hours
     * @param hours
     * @return
     */
    private String timeRemaining(String rank, int min_days, int days, int min_hours, int hours){
    	
    	int remain_days = (min_days - days);
		int remain_hrs = (min_hours - hours);
		
		plugin.debug("Rank Check: remaining days: " + remain_days + " remaining hours: " + remain_hrs);
		
		// If days remain, but no hours
		String time_left = " You need " + remain_days + " days, " + remain_hrs + " hours for " + rank; // default
		if(remain_days >= 0 && remain_hrs <= 0){
			time_left = rank+" in "+remain_days+" days. You already meet the minimum playtime hours requirement.";
		}
		// If hours remain, but no days
		if(remain_days <= 0 && remain_hrs >= 0){
			time_left = rank+" in "+remain_hrs+" hours of playtime. You already meet the minimum days requirement.";
		}
		// If both remain
		if(remain_days >= 0 && remain_hrs >= 0){
			time_left = rank+" in "+remain_hrs+" hours of playtime, in at least " + remain_days + " more days (since joined).";
		}
		
		return time_left;
    	
    }
}
