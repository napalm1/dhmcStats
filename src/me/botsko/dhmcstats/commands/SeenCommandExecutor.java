package me.botsko.dhmcstats.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class SeenCommandExecutor implements CommandExecutor  {
	
	/**
	 * 
	 */
	private Dhmcstats plugin;
	
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public SeenCommandExecutor(Dhmcstats plugin) {
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
    		if(player.hasPermission("dhmcstats.seen")){
    			String user = (args.length == 1 ? args[0] : player.getName());
    			try {
    				checkSeen( user, sender );
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
     * Checks the total playtime of a user
     * 
     * @param username
     * @throws SQLException 
     * @throws ParseException 
     */
    public void checkSeen(String username, CommandSender sender) throws SQLException, ParseException{
    	
    	// Expand partials
    	String tmp = plugin.expandName(username);
    	if(tmp != null){
    		username = tmp;
    	}

    	Date joined = getPlayerFirstSeen(username);
    	sender.sendMessage( plugin.playerMsg("Joined " + joined) );
    	
    	Date seen = getPlayerLastSeen(username);
    	sender.sendMessage( plugin.playerMsg("Last Seen " + seen) );
		
    }
    
    
	/**
	 * 
	 * @param person
	 * @param account_name
	 * @throws ParseException 
	 */
	public Date getPlayerFirstSeen( String username ) throws ParseException{
		Date joined = null;
		try {
			
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
            
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT player_join FROM joins WHERE username = ? ORDER BY player_join LIMIT 1;");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		if(rs.first()){
    			String join = rs.getString("player_join");
	    		DateFormat formatter ;
	        	formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	joined = (Date)formatter.parse( join );
    		}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
            return joined;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return null;
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 * @throws ParseException 
	 */
	public Date getPlayerLastSeen( String username ) throws ParseException{
		Date seen = null;
		try {
			
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
            
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT player_quit FROM joins WHERE username = ? AND player_quit IS NOT NULL ORDER BY player_quit DESC LIMIT 1;");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		if(rs.first()){
	    		String join = rs.getString("player_quit");
	    		DateFormat formatter ;
	        	formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	seen = (Date)formatter.parse( join );
    		}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
            return seen;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return null;
	}
}