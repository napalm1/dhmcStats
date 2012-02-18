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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DbConnection {
    private Connection conn;
    private DbConnectionPool pool;
    
    public DbConnection(Connection conn, DbConnectionPool pool) {
        this.conn = conn;
        this.pool = pool;
    }
    
    public synchronized boolean isClosed() {
        try {
            return conn.isClosed();
        } catch(SQLException e) {
            // Assume it's closed
            return true;
        }
    }
    
    public synchronized void close() {
        pool.returnToPool(this);
    }
    
    public synchronized void closeConnection() throws SQLException {
        conn.close();
    }
    
    public synchronized Statement createStatement() throws SQLException {
        return conn.createStatement();
    }
    
    public synchronized PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
}
