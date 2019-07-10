package org.runnerer.database;

import org.bukkit.plugin.Plugin;

import java.sql.*;

public class MySQL
{

    public static MySQL Instance;
    protected static Connection connection;
    /**
     * Plugin instance, use for plugin.getDataFolder()
     */
    protected Plugin plugin;

    public MySQL()
    {
        Instance = this;
    }

    public static Connection openConnection() throws SQLException, ClassNotFoundException
    {
        if (checkConnection())
        {
            return connection;
        }
        Class.forName("com.mysql.jdbc.Driver");
        /*
         * connection = DriverManager.getConnection("jdbc:mysql://" +
         * this.hostname + ":" + this.port + "/" + this.database, this.user,
         * this.password);
         */
        connection = DriverManager
                .getConnection("jdbc:mysql://" + "localhost" + ":" + "3306" + "/" + "Account?zeroDateTimeBehavior=convertToNull&useUnicode=true&useFastDateParsing=false&characterEncoding=UTF-8", "root", "password");
        return connection;
    }

    public static boolean checkConnection() throws SQLException
    {
        return connection != null && !connection.isClosed();
    }

    public static Connection getConnection()
    {
        return connection;
    }

    public static ResultSet querySQL(String query) throws SQLException, ClassNotFoundException
    {
        if (!checkConnection())
        {
            openConnection();
        }

        Statement statement = connection.createStatement();

        ResultSet result = statement.executeQuery(query);

        return result;
    }

    public static int updateSQL(String query) throws SQLException, ClassNotFoundException
    {
        if (!checkConnection())
        {
            openConnection();
        }

        Statement statement = connection.createStatement();

        int result = statement.executeUpdate(query);

        return result;
    }

    public boolean closeConnection() throws SQLException
    {
        if (connection == null)
        {
            return false;
        }
        connection.close();
        return true;
    }
}
