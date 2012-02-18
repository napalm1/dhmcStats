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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

public class DbConnectionPool {
    private LinkedList<DbConnection> pooledConnections;
    private String url;
    private String username;
    private String password;
    public DbConnectionPool(String driver, String url, String username, String password) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.pooledConnections = new LinkedList<DbConnection>();
        Class.forName(driver).newInstance();
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    public synchronized DbConnection getConnection() throws SQLException {
        // Try to get a pooled connection
        while(!pooledConnections.isEmpty()) {
            DbConnection conn = pooledConnections.remove();
            if(!conn.isClosed()) {
                // test the connection to make sure it's not stale
                String sql = "SELECT 1";
                PreparedStatement prest;
                try {
                    prest = conn.prepareStatement(sql);
                    prest.executeQuery();
                    return conn;
                } catch (SQLException e) {
                    try {
                        conn.closeConnection();
                    } catch (SQLException ex) { }
                }
            }
        }
        // create a new connection
        Connection conn = DriverManager.getConnection(url, username, password);
        return new DbConnection(conn, this);
    }
    
    public synchronized void returnToPool(DbConnection conn) {
        pooledConnections.add(conn);
    }
    
    public synchronized void closeConnections() {
        while(!pooledConnections.isEmpty()) {
            DbConnection conn = pooledConnections.remove();
            try {
                conn.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}