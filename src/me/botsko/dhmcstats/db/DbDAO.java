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

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import me.botsko.dhmcstats.Dhmcstats;


/**
* Data Access Object interface
*
* Interface that holds all methods that a DAO needs to have
*
*/
public abstract class DbDAO {
    
    private DbConnectionPool pool;
    
    public DbDAO(String driver, String url, String username, String password) {
        try {
            pool = new DbConnectionPool(driver, url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[finance] - database connection error.");
            Dhmcstats.disablePlugin();
        }
    }
    
    public abstract void removeInvalidJoins();
    public abstract void forceDateForNullQuits();
    public abstract void forcePlaytimeForNullQuits();
    public abstract void registerPlayerJoin( String username, String timestamp, String ip, int online_count );
    public abstract void registerPlayerQuit( String username, String timestamp );
    public abstract int getPlaytime( String username ) throws ParseException;
    public abstract int getPlayerJoinCount();
    public abstract int getPlayerJoinTodayCount();
    public abstract Date getPlayerFirstSeen( String username ) throws ParseException;
    public abstract Date getPlayerLastSeen( String username ) throws ParseException;
    public abstract HashMap<Float,String> getPlayerNewModQuizScores( String username );
    
    /**
	 * Get a database connection
	 * @return DbConnection object
	 * @throws SQLException
	 */
    protected DbConnection getConnection() throws SQLException {
        return pool.getConnection();
    }
    
    /**
* Close all active database handles
*/
    public void closeConnections() {
        pool.closeConnections();
    }
}