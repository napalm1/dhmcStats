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
 * Version 0.1.5.1
 * - Database result/statement closing
 * - Minor bugfix in playerstats
 * - Trying to hide legendary/ask viv promo notifications
 * Version 0.1.5.2
 * - Minor sql statement close missed
 * - Disabled join data, since we don't actually log first-joins yet
 * Version 0.1.5.3
 * - Adding auto-reconnect settings to database connection
 * Version 0.1.6
 * - "Not awaiting" promo messages now hidden from joins
 * - Adding rankall command
 * Version 0.1.6.1
 * - Removing inventory save code, since Duties plugin does it better
 * - /rankall ignores people not awaiting, so the list won't explode chat
 * - Adding basic info on how long until next rank
 * - Attempting to fix promo announcements not sending to lead mods
 * Version 0.1.7
 * - Fixing commands so they can be run from the console.
 * - Adding newmod score checking
 * 
 * 
 * BUGS:
 * - Rank doesn't count current session?
 * 
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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
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
    	
        java.util.Properties conProperties = new java.util.Properties();
        conProperties.put("user", "root");
        conProperties.put("password", "");
        conProperties.put("autoReconnect", "true");
        conProperties.put("maxReconnects", "3");

        try {
        c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/minecraft", conProperties);
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
				pstmt1.close();
				
			}
			
			pstmt.close();
			ts1.close();
			trs.close();
			
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
    			if(sender instanceof ConsoleCommandSender || (player != null && permissions.has(player, "dhmcstats.played")) ){
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
    			if(sender instanceof ConsoleCommandSender || (player != null && permissions.has(player, "dhmcstats.playerstats")) ){
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
    			if(sender instanceof ConsoleCommandSender || (player != null && permissions.has(player, "dhmcstats.seen")) ){
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
    			if(sender instanceof ConsoleCommandSender || (player != null && permissions.has(player, "dhmcstats.rank")) ){	
    				if (args.length == 1)
    					getQualifyFor( args[0], sender );
    				else
    					getQualifyFor( player.getName(), sender );
    			}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
    		return true;
    	}
    	
    	
    	// /rankall
    	if (command.getName().equalsIgnoreCase("rankall")){
    		if(sender instanceof ConsoleCommandSender || (player != null && permissions.has(player, "dhmcstats.rank")) ){
				rankAll( sender );
			}
    		return true;
    	}
    	
    	
    	// /ison
    	if (command.getName().equalsIgnoreCase("ison")){
    		if(sender instanceof ConsoleCommandSender || (player != null && permissions.has(player, "dhmcstats.ison")) ){	
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
    	
    	
    	// /scores [player]
    	if (command.getName().equalsIgnoreCase("scores")){
    		try {
				if (args.length == 1 && (sender instanceof ConsoleCommandSender || (player != null && permissions.has(player, "dhmcstats.rank")) ))
					checkScores( args[0], sender );
				else
					if(player != null)
						checkScores( player.getName(), sender );
			} catch (SQLException e) {
				e.printStackTrace();
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
			s1.close();
			
			return (int) (before_current + session_hours);
			
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		
		rs.close();
		s.close();
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
		
		rs.close();
		s.close();
    	
    	// Pull how many players were online today
		PreparedStatement s1;
		s1 = c.prepareStatement ("SELECT COUNT( DISTINCT(username) ) FROM `joins` WHERE DATE_FORMAT(player_join,'%Y-%m-%d') = DATE_FORMAT(NOW(),'%Y-%m-%d')");
		s1.executeQuery();
		ResultSet rs1 = s1.getResultSet();
		
		Integer playedtoday = 0;
		while( rs1.next() ){
			playedtoday = rs1.getInt(1);
		}
		
		rs1.close();
		s1.close();

		sender.sendMessage(ChatColor.GOLD  + "Players Online: " + getOnlineCount());
		sender.sendMessage(ChatColor.GOLD  + "Total Players: " + total);
		sender.sendMessage(ChatColor.GOLD  + "Unique Today: " + playedtoday);
		
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
		s.close();
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
			String join = rs.getString("player_join");
			rs.close();
			s.close();
			return join;
			
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		
		rs.close();
		s.close();
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
			String quit = rs.getString("player_quit");
			rs.close();
			s.close();
			return quit;
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		
		rs.close();
		s.close();
		return "";
		
    }
    
    
    /**
     * Check all online players for promo
     * @param sender
     */
    public void rankAll(CommandSender sender){
    	
    	sender.sendMessage(ChatColor.GOLD + "Checking... (showing only those who qualify)");
    	
    	for(Player pl: getServer().getOnlinePlayers()) {
    	
	    	// Check the user qualifies for any rank, alert mods
	        String promo = "";
	        PermissionUser user = permissions.getUser( pl.getName() );
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
					promo = checkQualifiesFor( pl.getName() );
					if(promo.indexOf(" not awaiting") == -1){
						sender.sendMessage(promo);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
	        }
    	}
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
    	String tmp = expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}
    	
    	String msg = "";
    	
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
    	if(permissions.getUser(username).inGroup("Admin")){
    		msg = ChatColor.GOLD + username + " is an admin. Nowhere to go man!";
    	}
    	else if(permissions.getUser(username).inGroup("LeadModerator")){
    		msg = ChatColor.GOLD + username + " is a lead mod. Ask Vive";
    	}
    	else if(permissions.getUser(username).inGroup("Moderator")){
    		msg = ChatColor.GOLD + username + " is a mod. Ask Vive";
    	}
    	else if(permissions.getUser(username).inGroup("NewModerator")){
    		msg = ChatColor.GOLD + username + " is a new mod. Ask Vive";
    	}
    	else if(permissions.getUser(username).inGroup("MythicalPlayer")){
    		msg = ChatColor.GOLD + username + " is Myth. Ask Vive";
    	}
    	else if(permissions.getUser(username).inGroup("LegendaryPlayer")){
    		msg = ChatColor.GOLD + username + " is Legendary. Check their skills, etc.";
    	}
    	else if(permissions.getUser(username).inGroup("RespectedPlayer")){
    		if(days >= 25 && hours >= 80){
    			msg = ChatColor.GOLD + username + " qualifies for: " + ChatColor.WHITE + " Legendary";
    		} else {
    			String time_left = " Legendary in "+(25 - days)+" days, and "+(80 - hours)+" hours.";
    			msg = ChatColor.GOLD + username + " is not awaiting promotion." + time_left;
    		}
    	}
    	else if(permissions.getUser(username).inGroup("TrustedPlayer")){
    		if(days >= 5 && hours >= 20){
    			msg = ChatColor.GOLD + username + " qualifies for: " + ChatColor.WHITE + " Respected";
    		} else {
    			String time_left = " Respected in "+(5 - days)+" days, and "+(20 - hours)+" hours.";
    			msg = ChatColor.GOLD + username + " is not awaiting promotion."+time_left;
    		}
    	}
    	else {
    		if(days >= 1 && hours >= 5){
    			msg = ChatColor.GOLD + username + " qualifies for: " + ChatColor.WHITE + " Trusted";
    		} else {
    			String time_left = " Trusted in "+(5 - hours)+" hours (day after you joined).";
    			msg = ChatColor.GOLD + username + " is not awaiting promotion."+time_left;
    		}
    	}
		return msg;
    }
    
    
    /**
     * Checks the newmod scores of a user
     * 
     * @param username
     * @throws SQLException 
     */
    public void checkScores(String username, CommandSender sender) throws SQLException{
    	
    	sender.sendMessage(ChatColor.GOLD + "NewMod Quiz scores for " + username + ": ");
    	
		PreparedStatement s;
		s = c.prepareStatement ("SELECT score, DATE_FORMAT(quiz_newmod.date_created,'%m/%d/%Y') as quizdate FROM quiz_newmod LEFT JOIN users ON users.id = quiz_newmod.user_id WHERE users.username = ? ORDER BY quiz_newmod.date_created;");
		s.setString(1, username);
		s.executeQuery();
		ResultSet rs = s.getResultSet();
		
		try {
			while(rs.next()){
				Float score = round(rs.getFloat("score")) * 100;
				sender.sendMessage(ChatColor.GOLD + rs.getString("quizdate") + ": " + score + "%");
			}
			rs.close();
			s.close();
		}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		
		rs.close();
		s.close();

    }
    
    
    /**
     * 
     * @param val
     * @return
     */
    public float round( Float val ){
    	return (float) (Math.round( val *100.0) / 100.0);
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