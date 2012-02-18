/*
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
* Packaged pooling code borrowed from Jobs Plugin for Bukkit
* Copyright (C) 2011 Zak Ford <zak.j.ford@gmail.com>
*/

package me.botsko.dhmcstats.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import me.botsko.dhmcstats.Dhmcstats;

public class DbDAOMySQL extends DbDAO {
	
	/**
	 * 
	 * @param url
	 * @param dbName
	 * @param username
	 * @param password
	 */
    public DbDAOMySQL(String url, String dbName, String username, String password) {
        super("com.mysql.jdbc.Driver", "jdbc:mysql://"+url+"/"+dbName, username, password);
        setUp();
    }
    
    /**
     * Create tables if they do not exist
     */
    public void setUp(){
//        try{
//            DbConnection conn = getConnection();
//            if(conn != null){
//                Statement st = conn.createStatement();
//                String table = "CREATE TABLE IF NOT EXISTS `finance_account_users` (`id` int(10) unsigned NOT NULL auto_increment,`account_name` varchar(155) NOT NULL,`username` varchar(55) NOT NULL,`is_owner` tinyint(1) NOT NULL default '0', PRIMARY KEY  (`id`)) ENGINE=MyISAM  DEFAULT CHARSET=latin1;";
//                st.executeUpdate(table);
//                conn.close();
//            }
//            else{
//                System.err.println("[finance] - MySQL connection problem");
//                Dhmcstats.disablePlugin();
//            }
//        }
//        catch (SQLException e){
//            e.printStackTrace();
//            Dhmcstats.disablePlugin();
//        }
    }
    
    
    /**
	 * 
	 * @param person
	 * @param account_name
	 */
	public void removeInvalidJoins(){
		try {
            DbConnection conn = getConnection();
			String str = String.format("DELETE FROM joins WHERE player_join = '0000-00-00 00:00:00'");
	        PreparedStatement s = conn.prepareStatement( str );
    		s.executeUpdate();
    		s.close();
            conn.close();
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
	public void forceDateForNullQuits(){
		try {
            DbConnection conn = getConnection();
            java.util.Date date= new java.util.Date();
    		String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime());
			String str = String.format("UPDATE joins SET player_quit = '%s' WHERE player_quit IS NULL", ts);
	        PreparedStatement s = conn.prepareStatement( str );
    		s.executeUpdate();
    		s.close();
            conn.close();
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
	public void forcePlaytimeForNullQuits(){
		try {
            DbConnection conn = getConnection();
			String str = String.format("SELECT id, TIME_TO_SEC(TIMEDIFF(player_quit,player_join)) AS playtime FROM joins WHERE playtime IS NULL");
	        PreparedStatement s = conn.prepareStatement( str );
    		s.executeQuery();
			ResultSet trs = s.getResultSet();
			// Update all null playtime records with playtime
			while( trs.next() ){
				Integer id = trs.getInt(1);
				int playtime = trs.getInt(2);
				String upd1 = String.format("UPDATE joins SET playtime = '%s' WHERE id = '%d'", playtime, id);
				PreparedStatement pstmt1 = conn.prepareStatement(upd1);
				pstmt1.executeUpdate();
				pstmt1.close();
			}
    		s.close();
            conn.close();
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
	public void registerPlayerJoin( String username, String timestamp, String ip, int online_count ){
		try {
            DbConnection conn = getConnection();
            String str = String.format("INSERT INTO joins (username,player_join,ip,player_count) VALUES ('%s','%s','%s','%d')", username, timestamp, ip, online_count );
	        PreparedStatement s = conn.prepareStatement(str);
	        s.executeUpdate();
    		s.close();
            conn.close();
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
	public void registerPlayerQuit( String username, String timestamp ){
		try {
            DbConnection conn = getConnection();
            
 			PreparedStatement s;
 			s = conn.prepareStatement ("SELECT id FROM joins WHERE username = ? AND player_quit IS NULL");
 			s.setString(1, username);
 			s.executeQuery();
 			ResultSet rs = s.getResultSet();
 			
 			while( rs.next() ){
 				
 				Integer id = rs.getInt(1);
 			
 				String upd = String.format("UPDATE joins SET player_quit = '%s' WHERE id = '%d'", timestamp, id);
 				PreparedStatement pstmt = conn.prepareStatement(upd);
 				pstmt.executeUpdate();
 				pstmt.close();
 	        
 				// now calculate the time spent online between this quit and join
 				PreparedStatement ts1;
 				ts1 = conn.prepareStatement ("SELECT TIME_TO_SEC(TIMEDIFF(player_quit,player_join)) AS playtime FROM joins WHERE id = ?");
 				ts1.setInt(1, id);
 				ts1.executeQuery();
 				ResultSet trs = ts1.getResultSet();
 				
 				while( trs.next() ){
 					
 					int playtime = trs.getInt(1);
 					
 					String upd1 = String.format("UPDATE joins SET playtime = '%s' WHERE id = '%d'", playtime, id);
 					PreparedStatement pstmt1 = conn.prepareStatement(upd1);
 					pstmt1.executeUpdate();
 					pstmt1.close();
 					
 				}
 				
 				trs.close();
 				ts1.close();
 			}
    
 			rs.close();
    		s.close();
            conn.close();
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
	public int getPlayerJoinCount(){
		try {
            DbConnection conn = getConnection();
            PreparedStatement s;
    		s = conn.prepareStatement ("SELECT COUNT( DISTINCT(username) ) FROM `joins`");
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		Integer total = 0;
    		while( rs.next() ){
    			total = rs.getInt(1);
    		}
    		rs.close();
    		s.close();
            conn.close();
            
            return total;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return 0;
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 */
	public int getPlayerJoinTodayCount(){
		try {
            DbConnection conn = getConnection();
            PreparedStatement s;
    		s = conn.prepareStatement ("SELECT COUNT( DISTINCT(username) ) FROM `joins` WHERE DATE_FORMAT(player_join,'%Y-%m-%d') = DATE_FORMAT(NOW(),'%Y-%m-%d')");
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();
    		
    		Integer total = 0;
    		while( rs.next() ){
    			total = rs.getInt(1);
    		}
    		rs.close();
    		s.close();
            conn.close();
            
            return total;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return 0;
	}
	
	
	/**
	 * 
	 * @param person
	 * @param account_name
	 * @throws ParseException 
	 */
	public int getPlaytime( String username ) throws ParseException{
		try {
			
			
			DbConnection conn = getConnection();
			// query for the null quit record for this player
			PreparedStatement s;
			s = conn.prepareStatement ("SELECT SUM(playtime) as playtime FROM joins WHERE username = ?");
			s.setString(1, username);
			s.executeQuery();
			ResultSet rs = s.getResultSet();
			
			try {
				rs.first();
				int before_current = rs.getInt(1);
				
				// We also need to pull any incomplete join and calc up-to-the-minute playtime
				PreparedStatement s1;
				s1 = conn.prepareStatement ("SELECT player_join FROM joins WHERE username = ? AND player_quit IS NULL");
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
			conn.close();
			return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return 0;
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
            DbConnection conn = getConnection();
            PreparedStatement s;
    		s = conn.prepareStatement ("SELECT player_join FROM joins WHERE username = ? ORDER BY player_join LIMIT 1;");
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
            conn.close();
            
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
            DbConnection conn = getConnection();
            PreparedStatement s;
    		s = conn.prepareStatement ("SELECT player_quit FROM joins WHERE username = ? AND player_quit IS NOT NULL ORDER BY player_quit DESC LIMIT 1;");
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
            conn.close();
            
            return seen;
            
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
	 */
	public HashMap<Float,String> getPlayerNewModQuizScores( String username ){
		try {
            DbConnection conn = getConnection();
            PreparedStatement s;
    		s = conn.prepareStatement ("SELECT score, DATE_FORMAT(quiz_newmod.date_created,'%m/%d/%Y') as quizdate FROM quiz_newmod LEFT JOIN users ON users.id = quiz_newmod.user_id WHERE users.username = ? ORDER BY quiz_newmod.date_created;");
    		s.setString(1, username);
    		s.executeQuery();
    		ResultSet rs = s.getResultSet();

    		HashMap<Float,String> scores = new HashMap<Float, String>();
    		while(rs.next()){
    			scores.put( (round(rs.getFloat("score")) * 100) , rs.getString("quizdate") );
			}
    		
    		rs.close();
    		s.close();
            conn.close();
            
            return scores;
            
        } catch (SQLException e) {
            e.printStackTrace();
            Dhmcstats.disablePlugin();
        }
		return null;
	}
	
	
	/**
     * 
     * @param val
     * @return
     */
    private float round( Float val ){
    	return (float) (Math.round( val *100.0) / 100.0);
    }
	
    
    
//  /**
//  * Checks the total playtime of a user
//  * 
//  * @param username
//  * @throws SQLException 
//  */
// public void checkForums(Player player) throws SQLException{
// 	
// 	if (c == null || c.isClosed()) dbc();
// 	
// 	String username = player.getName();
// 	
// 	// query for the null quit record for this player
//		PreparedStatement s;
//		s = c.prepareStatement ("SELECT id FROM users WHERE username = ?");
//		s.setString(1, username);
//		s.executeQuery();
//		ResultSet rs = s.getResultSet();
//		
//		if(!rs.next()){
//			player.sendMessage(ChatColor.AQUA + "You haven't joined our forums at http://dhmc.us. Ideas? Concerns? You really need to join!");
//		}
//		
//		rs.close();
//		s.close();
//		c.close();
//		
// }
}