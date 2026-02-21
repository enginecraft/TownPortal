package org.enginecraft.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlUtil {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:file:./data/town-portal;AUTO_SERVER=TRUE");
    }
}
