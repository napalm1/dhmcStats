package me.botsko.dhmcstats.commands;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
    			String user = (args.length == 1 ? args[0] : player.getName());
    			try {
					getQualifyFor( user, sender );
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
     * 
     * @param username
     * @param sender
     * @throws SQLException
     * @throws ParseException
     */
    public void getQualifyFor(String username, CommandSender sender) throws SQLException, ParseException {
    	sender.sendMessage( plugin.playerMsg( checkQualifiesFor( username, sender ) ) );
    }
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     * @throws ParseException 
     */
    public String checkQualifiesFor(String username, CommandSender sender) throws SQLException, ParseException {
    	
    	// Expand partials
    	String tmp = plugin.expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}
    	
    	String msg = "";
    	
    	// get the base join date
    	Date joined = plugin.getDbDAO().getPlayerFirstSeen(username);
    	
    	if(joined != null){
    	
	    	Date today = new Date();
	    	long diff = today.getTime() - joined.getTime();
	    	int days = (int) ((diff / 1000) / 86400);
	    	
	    	// determine who this is for
	    	String string_intro = username + " is";
	    	String string_qual = username + " qualifies ";
	    	String string_remain = username + " already meets";
	    	if (sender instanceof Player) {
	    		Player player = (Player) sender;
	    		if(player.getName().equalsIgnoreCase( username )){
	    			string_intro = "You are";
	    			string_qual = "You qualify";
	    			string_remain = "You already meet";
	    		}
	    	}
	    	
	    	// Get the play time
	    	int hours = plugin.getDbDAO().getPlaytime(username) / 3600;
	    	
	    	// Promotion checks per group
	    	if(username.equalsIgnoreCase("viveleroi")){
	    		msg = "Vive's rank is Pure Awesome. Silly you, checking the owner's rank.";
	    	}
	    	else if(plugin.getPermissions().getUser(username).inGroup("Admin")){
	    		msg = string_intro + " an admin. Nowhere to go man!";
	    	}
	    	else if(plugin.getPermissions().getUser(username).inGroup("LeadModerator")){
	    		msg = string_intro + " a lead mod. A promotion is up to Vive";
	    	}
	    	else if(plugin.getPermissions().getUser(username).inGroup("Moderator")){
	    		msg = string_intro + " a mod. A promotion is up to Vive";
	    	}
	    	else if(plugin.getPermissions().getUser(username).inGroup("NewModerator")){
	    		msg = string_intro + " a new mod. A promotion is up to Vive";
	    	}
	    	else if(plugin.getPermissions().getUser(username).inGroup("MythicalPlayer")){
	    		msg = string_intro + " Myth. A promotion is up to Vive";
	    	}
	    	else if(plugin.getPermissions().getUser(username).inGroup("EternalPlayer")){
	    		msg = string_intro + " Eternal. A promotion is up to Vive";
	    	}
	    	else if(plugin.getPermissions().getUser(username).inGroup("LegendaryPlayer")){
	    		msg = string_intro + " Legendary. Myth rank depends on skills, and other qualifications.";
	    	}
	    	else if(plugin.getPermissions().getUser(username).inGroup("RespectedPlayer")){
	    		if(days >= 25 && hours >= 80){
	    			msg = string_qual + " for: " + ChatColor.AQUA + " Legendary";
	    		} else {
	    			msg = timeRemaining("Legendary", 25, days, 80, hours, string_remain);
	    		}
	    	}
	    	else if(plugin.getPermissions().getUser(username).inGroup("TrustedPlayer")){
	    		if(days >= 5 && hours >= 20){
	    			msg = string_qual + " for: " + ChatColor.AQUA + " Respected";
	    		} else {
	    			msg = timeRemaining("Respected", 5, days, 20, hours, string_remain);
	    		}
	    	}
	    	else {
	    		
	    		Calendar cal1 = Calendar.getInstance();
	    		Calendar cal2 = Calendar.getInstance();
	    		cal1.setTime(today);
	    		cal2.setTime(joined);
	    		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
	    		                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	    		
	    		if(!sameDay && hours >= 5){
	    			msg = string_qual + " for: " + ChatColor.AQUA + " Trusted";
	    		} else {
	    			int remain_hrs = (5 - hours);
	    			if(sameDay){
	    				msg = "Trusted in " + remain_hrs + "hour of playtime, no sooner than tomorrow.";
	    			} else {
	    				msg = "Trusted in " + remain_hrs + "hour of playtime.";
	    			}
	    		}
	    	}
    	} else {
    		msg = "Can't find that player. Try again.";
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
    private String timeRemaining(String rank, int min_days, int days, int min_hours, int hours, String string_remain ){
    	
    	int remain_days = (min_days - days);
		int remain_hrs = (min_hours - hours);
		
		plugin.debug("Rank Check: remaining days: " + remain_days + " remaining hours: " + remain_hrs);
		
		// If days remain, but no hours
		String time_left = " You need " + remain_days + " days, " + remain_hrs + " hours for " + rank; // default
		if(remain_days >= 0 && remain_hrs <= 0){
			time_left = rank+" in "+remain_days+" days. "+string_remain+" the minimum playtime hours requirement.";
		}
		// If hours remain, but no days
		if(remain_days <= 0 && remain_hrs >= 0){
			time_left = rank+" in "+remain_hrs+" hours of playtime. "+string_remain+" the minimum days requirement.";
		}
		// If both remain
		if(remain_days >= 0 && remain_hrs >= 0){
			time_left = rank+" in "+remain_hrs+" hours of playtime, in at least " + remain_days + " more days (since joined).";
		}
		
		return time_left;
    	
    }
}