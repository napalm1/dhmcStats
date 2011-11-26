package me.botsko.dhmcstats;

/**
 * 
 * dhmcStats
 * 
 * This plugin is specific to Mike's DarkHelmet Minecraft server. dhmc.us
 * 
 * Logs player join and quit so that we may track their daily activity and better
 * know how many active players we have over time.
 * 
 * Version 0.1
 * - Player join/quit listeners logging the timestamps
 * Version 0.1.1
 * - Added playtime calculations on quit event
 * - Added core playtime reading function
 * - Added forum registration check to alert user on join
 * - Added total/today/joined player stats command
 * Version 0.1.2
 * - Added forced playtime calcs for crashed join records
 * Version 0.1.3a
 * - Fixing numerous playtime bugs
 * - Added code that will import the Playtime plugin hashmap data
 * - Added "seen" command for first/last seen data
 * - Adding basic promotion qualification system
 * Version 0.1.3
 * - Removed temporary code
 * - Adding IP tracking
 * - Adding player count tracking, player count messaging on login
 * Version 0.1.4
 * - Added /ison [player] command
 * - Added partial name matching to most options
 * Version 0.1.5
 * - Playtime for current online session now added to totalplaytime checks
 * 
 * 
 * BUGS:
 * - First joins stat not working on live
 * - Commands need to be accessible from console
 * 
 * FUTURE:
 * 
 * - Add scheduled check for rank ups every fifteen minutes?
 * - Add current playtime to calc for up-to-the-minute numbers
 * - Alert lead moderators when a user qualifies for a promotion
 * - Reward users who sign up for the forums with something, or who post replies
 * 
 * 
 *     for(Player player: getServer().getOnlinePlayers()) {
     
        if(player.hasPermission("send.me.message")) {
            player.sendMessage("You were sent a message");
        }
     
    }


 * 
 */

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class Dhmcstats extends JavaPlugin {
	
	Logger log = Logger.getLogger("Minecraft");
	java.sql.Connection c;
	PermissionManager permissions;
    private final DhmcstatsPlayerListener playerListener = new DhmcstatsPlayerListener(this);
    
    
    /**
     * Connects to the MySQL database
     */
    protected void dbc(){
    	try {
			c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/minecraft","root","");
		} catch (SQLException e) {
			log.throwing("me.botsko.dhmcstats", "dbc()", e);
		}
	}


    /**
     * Enables the plugin and activates our player listeners
     */
	public void onEnable(){
		
		log.info("[Dhmcstats]: Initializing player listeners");
		dbc();
	
        // Force a timestamp for any null player_quits, which should only
		// happen if the server crashed and the player_quit even never fired. Since
		// we auto-reboot it's fairly safe to assume to the quit time isn't very far off.
        try {
        	java.util.Date date= new java.util.Date();
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
			String s = String.format("UPDATE joins SET player_quit = '%s' WHERE player_quit IS NULL", ts);
	        PreparedStatement pstmt = c.prepareStatement(s);
	        pstmt.executeUpdate();
	        
	        // we also need to force a playtime calculation
			PreparedStatement ts1;
			ts1 = c.prepareStatement ("SELECT id, TIME_TO_SEC(TIMEDIFF(player_quit,player_join)) AS playtime FROM joins WHERE playtime IS NULL");
			ts1.executeQuery();
			ResultSet trs = ts1.getResultSet();
			
			while( trs.next() ){
				
				Integer id = trs.getInt(1);
				int playtime = trs.getInt(2);
				
				String upd1 = String.format("UPDATE joins SET playtime = '%s' WHERE id = '%d'", playtime, id);
				PreparedStatement pstmt1 = c.prepareStatement(upd1);
				pstmt1.executeUpdate();
				
			}
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
		
		if(pm.isPluginEnabled("PermissionsEx")){
			permissions = PermissionsEx.getPermissionManager();
			log.info("[Dhmcstats]: PermissionsEx found.");
		} else {
			log.warning("[Dhmcstats]: PermissionsEx plugin was not found.");
	    }
	}
 
	
	/**
	 * Shutdown
	 */
	public void onDisable(){
		log.info("[Dhmcstats]: Stopping player listeners");
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	
    	Player player = null;
    	if (sender instanceof Player) {
    		player = (Player) sender;
    	}
    	
    	
    	// /played [player]
    	if (command.getName().equalsIgnoreCase("played")){
    		try {
    			if(permissions.has(player, "dhmcstats.played")){
    				if (args.length == 1)
    					checkPlayTime( args[0], sender );
    				else
    					checkPlayTime( player.getName(), sender );
    			}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
    		return true;
    	}
    	
    	// /playerstats
    	if (command.getName().equalsIgnoreCase("playerstats")){
    		try {
    			if(permissions.has(player, "dhmcstats.playerstats")){
    				checkPlayerCounts( sender );
    			}
			} catch (SQLException e) {
				e.printStackTrace();
			}
    		return true;
    	}
    	
    	// /seen [player]
    	if (command.getName().equalsIgnoreCase("seen")){
    		try {
    			if(permissions.has(player, "dhmcstats.seen")){
    				if (args.length == 1)
    					checkSeen( args[0], sender );
    				else
    					checkSeen( player.getName(), sender );
    			}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return true;
    	}
    	
    	
    	// /rank
    	if (command.getName().equalsIgnoreCase("rank")){
    		try {
    			if(permissions.has(player, "dhmcstats.rank")){
    				if (args.length == 1)
    					checkQualifiesFor( args[0], sender );
    				else
    					checkQualifiesFor( player.getName(), sender );
    			}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return true;
    	}
    	
    	
    	// /ison
    	if (command.getName().equalsIgnoreCase("ison")){
    		if(permissions.has(player, "dhmcstats.ison")){
				if (args.length == 1){
					 String ison = expandName(args[0]);
					 if(ison != null){
						 sender.sendMessage( ison + " is online" ); 
					 } else {
						 sender.sendMessage( args[0] + " is not online" ); 
					 }
				}
			}
    		return true;
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
    public void checkPlayTime(String username, CommandSender sender) throws SQLException, ParseException {
    	
    	// Expand partials
    	String tmp = expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}
    
		int playtime = getPlayTime(username);
		int[] times = splitToComponentTimes(playtime);
		sender.sendMessage(ChatColor.GOLD + username + " has played for " + times[0] + " hours, " + times[1] + " minutes, and " + times[2] + " seconds. Nice!");
		
    }
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     * @throws ParseException 
     */
    public int getPlayTime(String username) throws SQLException, ParseException{
    	
    	// query for the null quit record for this player
		PreparedStatement s;
		s = c.prepareStatement ("SELECT SUM(playtime) as playtime FROM joins WHERE username = ?");
		s.setString(1, username);
		s.executeQuery();
		ResultSet rs = s.getResultSet();
		
		try {
			rs.first();
			int before_current = rs.getInt(1);
			
			// We also need to pull any incomplete join and calc up-to-the-minute playtime
			PreparedStatement s1;
			s1 = c.prepareStatement ("SELECT player_join FROM joins WHERE username = ? AND player_quit IS NULL");
			s1.setString(1, username);
			s1.executeQuery();
			ResultSet rs1 = s1.getResultSet();
			
			long session_hours = 0;
			try {
				if(rs1.first()){
					String session_started = rs1.getString("player_join");
					
					DateFormat formatter ;
			    	formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    	Date joined = (Date)formatter.parse( session_started );
			    	Date today = new Date();
			    	session_hours = today.getTime() - joined.getTime();
			    	session_hours = session_hours / 1000;
				}
			}
			catch ( SQLException e ) {
				e.printStackTrace();
			}
			
			rs1.close();
			
			return (int) (before_current + session_hours);
			
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		
		rs.close();
		return 0;
		
    }
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     */
    public void checkPlayerCounts(CommandSender sender) throws SQLException{
    	
    	
    	// Pull how many players joined in total
		PreparedStatement s;
		s = c.prepareStatement ("SELECT COUNT( DISTINCT(username) ) FROM `joins`");
		s.executeQuery();
		ResultSet rs = s.getResultSet();
		
		Integer total = 0;
		while( rs.next() ){
			total = rs.getInt(1);
		}
    	
    	// Pull how many players were online today
		PreparedStatement s1;
		s1 = c.prepareStatement ("SELECT COUNT( DISTINCT(username) ) FROM `joins` WHERE DATE_FORMAT(player_join,'%Y-%m-%d') = DATE_FORMAT(NOW(),'%Y-%m-%d')");
		s1.executeQuery();
		ResultSet rs1 = s1.getResultSet();
		
		Integer playedtoday = 0;
		while( rs1.next() ){
			playedtoday = rs1.getInt(1);
		}
		
		// Pull how many players joined
		PreparedStatement s2;
		s2 = c.prepareStatement ("SELECT COUNT( id ) FROM `joins` WHERE DATE_FORMAT(player_join,'%Y-%m-%d') = DATE_FORMAT(NOW(),'%Y-%m-%d')");
		s2.executeQuery();
		ResultSet rs2 = s2.getResultSet();
		
		Integer joinedtoday = 0;
		while( rs1.next() ){
			joinedtoday = rs2.getInt(1);
		}

		sender.sendMessage(ChatColor.GOLD  + "Players Online: " + getOnlineCount());
		sender.sendMessage(ChatColor.GOLD  + "Total Players: " + total);
		sender.sendMessage(ChatColor.GOLD  + "Unique Today: " + playedtoday);
		sender.sendMessage(ChatColor.GOLD  + "First Joins Today: " + joinedtoday);
		
		rs1.close();
		
    }
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     */
    public void checkForums(Player player) throws SQLException{
    	
    	String username = player.getName();
    	
    	// query for the null quit record for this player
		PreparedStatement s;
		s = c.prepareStatement ("SELECT id FROM users WHERE username = ?");
		s.setString(1, username);
		s.executeQuery();
		ResultSet rs = s.getResultSet();
		
		if(!rs.next()){
			player.sendMessage(ChatColor.AQUA + "You haven't joined our forums at http://dhmc.us. Ideas? Concerns? You really need to join!");
		}
		
		rs.close();
    }
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     * @throws ParseException 
     */
    public void checkSeen(String username, CommandSender sender) throws SQLException, ParseException{
    	
    	// Expand partials
    	String tmp = expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}

    	DateFormat formatter ;
    	formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date joined = (Date)formatter.parse( checkFirstSeen(username) );
    	sender.sendMessage(ChatColor.GOLD + "Joined " + joined);
   
    	DateFormat formatter1 ;
    	formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date seen = (Date)formatter1.parse( checkLastSeen(username) );
    	sender.sendMessage(ChatColor.GOLD + "Last Seen " + seen);
		
    }
    
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     */
    public String checkFirstSeen(String username) throws SQLException{
    	
		PreparedStatement s;
		s = c.prepareStatement ("SELECT player_join FROM joins WHERE username = ? ORDER BY player_join LIMIT 1;");
		s.setString(1, username);
		s.executeQuery();
		ResultSet rs = s.getResultSet();
		
		try {
			rs.first();
			return rs.getString("player_join");
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		
		rs.close();
		return "";
		
    }
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     */
    public String checkLastSeen(String username) throws SQLException{
    	
		PreparedStatement s;
		s = c.prepareStatement ("SELECT player_quit FROM joins WHERE username = ? AND player_quit IS NOT NULL ORDER BY player_quit DESC LIMIT 1;");
		s.setString(1, username);
		s.executeQuery();
		ResultSet rs = s.getResultSet();
		
		try {
			rs.first();
			return rs.getString("player_quit");
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		
		rs.close();
		return "";
		
    }
    
    
    /**
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     * @throws ParseException 
     */
    public void checkQualifiesFor(String username, CommandSender sender) throws SQLException, ParseException {
    	
    	// Expand partials
    	String tmp = expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}
    	
    	// get the base join date
    	DateFormat formatter ;
    	formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date joined = (Date)formatter.parse( checkFirstSeen(username) );
    	Date today = new Date();
    	long diff = today.getTime() - joined.getTime();
    	int days = (int) ((diff / 1000) / 86400);
    	
    	// Get the play time
    	int hours = getPlayTime(username) / 3600;
    	
    	// Promotion checks per group
    	if(permissions.getUser(username).inGroup("LeadModerator")){
    		sender.sendMessage(ChatColor.GOLD + username + " is a mod. Ask Vive");
    	}
    	else if(permissions.getUser(username).inGroup("Moderator")){
    		sender.sendMessage(ChatColor.GOLD + username + " is a mod. Ask Vive");
    	}
    	else if(permissions.getUser(username).inGroup("NewModerator")){
    		sender.sendMessage(ChatColor.GOLD + username + " is a mod. Ask Vive");
    	}
    	else if(permissions.getUser(username).inGroup("MythicalPlayer")){
    		// no promo policy here
    	}
    	else if(permissions.getUser(username).inGroup("LegendaryPlayer")){
    		// check mc skills
    		// say "possibly"
    	}
    	else if(permissions.getUser(username).inGroup("RespectedPlayer")){
    		if(days >= 25 && hours >= 80){
    			sender.sendMessage(ChatColor.GOLD + username + " qualifies for: " + ChatColor.WHITE + " Legendary");
    		} else {
    			sender.sendMessage(ChatColor.GOLD + username + " is not awaiting promotion.");
    		}
    	}
    	else if(permissions.getUser(username).inGroup("TrustedPlayer")){
    		if(days >= 5 && hours >= 20){
    			sender.sendMessage(ChatColor.GOLD + username + " qualifies for: " + ChatColor.WHITE + " Respected");
    		} else {
    			sender.sendMessage(ChatColor.GOLD + username + " is not awaiting promotion.");
    		}
    	}
    	else {
    		if(days >= 1 && hours >= 5){
    			sender.sendMessage(ChatColor.GOLD + username + " qualifies for: " + ChatColor.WHITE + " Trusted");
    		} else {
    			sender.sendMessage(ChatColor.GOLD + username + " is not awaiting promotion.");
    		}
    	}
    }
    
    
    /**
     * 
     */
    public int getOnlineCount(){
    	return getServer().getOnlinePlayers().length;
    }
    

    /**
     * Partial username matching
     * @param Name
     * @return
     */
    public String expandName(String Name) {
        int m = 0;
        String Result = "";
        for (int n = 0; n < getServer().getOnlinePlayers().length; n++) {
            String str = getServer().getOnlinePlayers()[n].getName();
            if (str.matches("(?i).*" + Name + ".*")) {
                m++;
                Result = str;
                if(m==2) {
                    return null;
                }
            }
            if (str.equalsIgnoreCase(Name))
                return str;
        }
        if (m == 1)
            return Result;
        if (m > 1) {
            return null;
        }
        if (m < 1) {
            return null;
        }
        return null;
    }
    
    
    /**
     * Convert seconds into hours/mins/secs
     * 
     * @param biggy
     * @return
     */
    public static int[] splitToComponentTimes(int biggy){
        int hours = (int) biggy / 3600;
        int remainder = (int) biggy - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;
        int[] ints = {hours , mins , secs};
        return ints;
    }
}