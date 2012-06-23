package me.botsko.dhmcstats.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import me.botsko.dhmcstats.Dhmcstats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

public class ScoresCommandExecutor implements CommandExecutor  {
	
	/**
	 * 
	 */
	private Dhmcstats plugin;
	
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public ScoresCommandExecutor(Dhmcstats plugin) {
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
    		if(player.hasPermission("dhmcstats.scores")){
    			String user = (args.length == 1 ? args[0] : player.getName());
    			try {
    				checkScores( user, sender );
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
    	}
		return false; 
	}
	
	
	/**
     * Checks the newmod scores of a user
     * 
     * @param username
     * @throws SQLException 
     */
    public void checkScores(String username, CommandSender sender) throws SQLException{
    	
    	sender.sendMessage( plugin.playerMsg( "NewMod Quiz scores for " + username + ": " ) );
    	
    	HashMap<Float,String> scores = getPlayerNewModQuizScores(username);
    	Iterator<Entry<Float, String>> it = scores.entrySet().iterator();

    	while (it.hasNext()) {
    		Map.Entry<Float, String> pairs = (Map.Entry<Float, String>)it.next();
    		sender.sendMessage( plugin.playerMsg( pairs.getValue() + ": " + pairs.getKey() + "%" ) );
    	}
    }
    
    
    /**
     * @todo move this somewhere else
     * @param val
     * @return
     */
    private float round( Float val ){
    	return (float) (Math.round( val *100.0) / 100.0);
    }
    
    
    /**
	 * 
	 * @param person
	 * @param account_name
	 */
	protected HashMap<Float,String> getPlayerNewModQuizScores( String username ){
		try {
			
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
            
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT score, DATE_FORMAT(quiz_newmod.date_created,'%m/%d/%Y') as quizdate FROM quiz_newmod LEFT JOIN users ON users.id = quiz_newmod.user_id WHERE users.username = ? ORDER BY quiz_newmod.date_created;");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();

    		HashMap<Float,String> scores = new HashMap<Float, String>();
    		while(rs.next()){
    			scores.put( (round(rs.getFloat("score")) * 100) , rs.getString("quizdate") );
			}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
            return scores;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return null;
	}
}