package me.botsko.dhmcstats.joins;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import me.botsko.dhmcstats.Dhmcstats;

public class JoinUtil {

	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static void registerPlayerJoin( Dhmcstats plugin, String username, String timestamp, String ip, int online_count ){
		try {
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
            String str = String.format("INSERT INTO joins (username,player_join,ip,player_count) VALUES ('%s','%s','%s','%d')", username, timestamp, ip, online_count );
	        PreparedStatement s = plugin.conn.prepareStatement(str);
	        s.executeUpdate();
    		s.close();
            plugin.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static void registerPlayerQuit( Dhmcstats plugin, String username, String timestamp ){
		try {
            
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
            
 			PreparedStatement s;
 			s = plugin.conn.prepareStatement ("SELECT id FROM joins WHERE username = ? AND player_quit IS NULL");
 			s.setString(1, username);
 			s.executeQuery();
 			ResultSet rs = s.getResultSet();
 			
 			while( rs.next() ){
 				
 				Integer id = rs.getInt(1);
 			
 				String upd = String.format("UPDATE joins SET player_quit = '%s' WHERE id = '%d'", timestamp, id);
 				PreparedStatement pstmt = plugin.conn.prepareStatement(upd);
 				pstmt.executeUpdate();
 				pstmt.close();
 	        
 				// now calculate the time spent online between this quit and join
 				PreparedStatement ts1;
 				ts1 = plugin.conn.prepareStatement ("SELECT TIME_TO_SEC(TIMEDIFF(player_quit,player_join)) AS playtime FROM joins WHERE id = ?");
 				ts1.setInt(1, id);
 				ts1.executeQuery();
 				ResultSet trs = ts1.getResultSet();
 				
 				while( trs.next() ){
 					
 					int playtime = trs.getInt(1);
 					
 					String upd1 = String.format("UPDATE joins SET playtime = '%s' WHERE id = '%d'", playtime, id);
 					PreparedStatement pstmt1 = plugin.conn.prepareStatement(upd1);
 					pstmt1.executeUpdate();
 					pstmt1.close();
 					
 				}
 				
 				trs.close();
 				ts1.close();
 			}
    
 			rs.close();
    		s.close();
            plugin.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
	}

	
	/**
	 * 
	 * @param plugin
	 */
	public static void startupDbChecks( Dhmcstats plugin ){
		forceDateForNullQuits(plugin);
		forcePlaytimeForNullQuits(plugin);
	}

	
	/**
	 * Force a timestamp for any null player_quits, which should only
	 * happen if the server crashed and the player_quit even never fired.
	 * @param person
	 * @param account_name
	 */
	protected static void forceDateForNullQuits( Dhmcstats plugin ){
		try {
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
            java.util.Date date= new java.util.Date();
    		String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
			String str = String.format("UPDATE joins SET player_quit = '%s' WHERE player_quit IS NULL", ts);
	        PreparedStatement s = plugin.conn.prepareStatement( str );
    		s.executeUpdate();
    		s.close();
            plugin.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	protected static void forcePlaytimeForNullQuits( Dhmcstats plugin ){
		try {
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
			String str = String.format("SELECT id, TIME_TO_SEC(TIMEDIFF(player_quit,player_join)) AS playtime FROM joins WHERE playtime IS NULL");
	        PreparedStatement s = plugin.conn.prepareStatement( str );
    		s.executeQuery();
			ResultSet trs = s.getResultSet();
			// Update all null playtime records with playtime
			while( trs.next() ){
				Integer id = trs.getInt(1);
				int playtime = trs.getInt(2);
				String upd1 = String.format("UPDATE joins SET playtime = '%s' WHERE id = '%d'", playtime, id);
				PreparedStatement pstmt1 = plugin.conn.prepareStatement(upd1);
				pstmt1.executeUpdate();
				pstmt1.close();
			}
			trs.close();
    		s.close();
            plugin.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
	}
	
	
	/**
	 * 
	 * @param plugin
	 * @param users
	 */
	public static void catchDisconnects(Dhmcstats plugin, String users){
		forceDateForOfflinePlayers( plugin, users );
		forcePlaytimeForOfflinePlayers( plugin, users );
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	protected static void forceDateForOfflinePlayers( Dhmcstats plugin, String users ){
		try {
			
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
			
			if(!users.isEmpty()){
				users = " AND username NOT IN ("+users+")";
			}
            
            java.util.Date date= new java.util.Date();
    		String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
			String str = String.format("UPDATE joins SET player_quit = '%s' WHERE player_quit IS NULL%s", ts, users);
	        PreparedStatement s = plugin.conn.prepareStatement( str );
    		s.executeUpdate();
    		s.close();
            plugin.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	protected static void forcePlaytimeForOfflinePlayers( Dhmcstats plugin, String users ){
		try {
			
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
			
			if(!users.isEmpty()){
				users = " AND username NOT IN ("+users+")";
			}
            
			String str = String.format("SELECT id, TIME_TO_SEC(TIMEDIFF(player_quit,player_join)) AS playtime FROM joins WHERE playtime IS NULL%s", users);
	        PreparedStatement s = plugin.conn.prepareStatement( str );
    		s.executeQuery();
			ResultSet trs = s.getResultSet();
			// Update all null playtime records with playtime
			while( trs.next() ){
				Integer id = trs.getInt(1);
				int playtime = trs.getInt(2);
				String upd1 = String.format("UPDATE joins SET playtime = '%s' WHERE id = '%d'", playtime, id);
				PreparedStatement pstmt1 = plugin.conn.prepareStatement(upd1);
				pstmt1.executeUpdate();
				pstmt1.close();
			}
			trs.close();
    		s.close();
            plugin.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public static List<Alts> getPlayerAlts( Dhmcstats plugin, String username ){
		ArrayList<Alts> accounts = new ArrayList<Alts>();
		try {
            
			if (plugin.conn == null || plugin.conn.isClosed() || !plugin.conn.isValid(1)) plugin.dbc();
			
			// Pull the IPs first
            PreparedStatement s;
    		s = plugin.conn.prepareStatement ("SELECT ip FROM join_ips WHERE username = ?");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		while(rs.next()){
    			
    			plugin.log("Finding alt for IP: " + rs.getString("ip"));
    			
    			PreparedStatement s1;
        		s1 = plugin.conn.prepareStatement ("SELECT username FROM join_ips WHERE ip = ? AND username != ?");
        		s1.setString(1, rs.getString("ip"));
        		s1.setString(2, username);
        		s1.executeQuery();
        		ResultSet rs1 = s1.getResultSet();
	        	while(rs1.next()){
	    			accounts.add( new Alts(rs.getString("ip"), rs1.getString("username")) );
				}
        		rs1.close();
        		s1.close();
    		}
    		
    		rs.close();
    		s.close();
            plugin.conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return accounts;
	}
}
