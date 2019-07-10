package org.runnerer.guilds.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.mysql.jdbc.Connection;
import org.runnerer.database.MySQL;
import org.runnerer.guilds.GuildManager;
import org.runnerer.guilds.common.utils.C;
import org.runnerer.guilds.common.utils.F;
import org.runnerer.guilds.common.utils.UUIDFetcher;
import org.runnerer.guilds.enums.GuildStatus;
import org.runnerer.guilds.enums.Permission;
import org.runnerer.guilds.enums.PlayerGuildStatus;

public class GuildRepository
{
	private static String RETRIEVE_MAX_LEADERBOARDS = "SELECT MAX(leaderboardPosition) AS leaderboardPosition FROM guilds;";
	private static String DELETE_GUILD = "DELETE FROM guilds WHERE guildName = ?;";
	private static String DELETE_GUILD_PLAYERS = "DELETE FROM playerGuild WHERE guild = ?;";
    
    private static String UPDATE = "SELECT uuid FROM playerGuild;";
    
    public static String getPluginName()
    {
    	return "Guild Manager";
    }
    
	@EventHandler
	public void AddPlayerToPlayerGuilds(PlayerJoinEvent event) throws ClassNotFoundException, SQLException 
    {
   
        ResultSet res3 = MySQL
                .querySQL("SELECT * FROM playerGuild WHERE uuid = '" + event.getPlayer().getUniqueId().toString() + "';");
        if (!res3.next())

            MySQL
                    .updateSQL("INSERT INTO playerGuild (`uuid`, `guild`, `status`, `permission`) VALUES ('" + event.getPlayer().getUniqueId().toString() + "', '" + "None" + "', '" + "None" + "', '" + "None" +  "');");
    }
	
	public static void CreateGuild(Player leader, String name) throws ClassNotFoundException, SQLException
	{
        ResultSet res3 = MySQL
                .querySQL("SELECT * FROM guilds WHERE guildName = '" + name + "';");
        
        if (IsInGuild(leader))
        {
        	leader.sendMessage(F.main(getPluginName(), "You're already apart of a guild."));
        	return;
        }
        
        if (res3.next())
        {
        	leader.sendMessage(F.main(getPluginName(), "That guild name is already taken."));
        	return;
        }
        
        if (name.length() < 3)
        {
        	leader.sendMessage(F.main(getPluginName(), "You can't make a guild name with less than three characters."));
        	return;
        }
        
        if (name.length() > 16)
        {
        	leader.sendMessage(F.main(getPluginName(), "You can't make a guild name larger than sixteen characters."));
        	return;
        }
        
        if (name.equalsIgnoreCase("fuck") || name.equalsIgnoreCase("shit") || name.equalsIgnoreCase("name") || name.equalsIgnoreCase("dick") || name.equalsIgnoreCase("penis") || name.equalsIgnoreCase("nigga") || name.equalsIgnoreCase("boobs")|| name.equalsIgnoreCase("slut")|| name.equalsIgnoreCase("dickhead"))
        {
        	leader.sendMessage(F.main(getPluginName(), "You can't name your guild that."));
        	return;
        }
        
        // Sets the guild.      
        MySQL
        .updateSQL("INSERT INTO guilds (`guildName`, `leaderUuid`, `status`, `size`, `leaderboardPosition`, `xp`, `level`) VALUES ('" + name + "', '" + leader.getUniqueId().toString() + "', '" + "Open" + "', '" + 1 + "', '" + String.valueOf(GetMaxLeaderboardPosition() + 1) + "', '" + "0" + "', '" + "1" + "');");
        
        MySQL
        .updateSQL("UPDATE playerGuild SET guild = '" + name + "' WHERE uuid = '" + leader.getUniqueId().toString() + "';");
        
        setPermission(leader, Permission.LEADER);
        setStatus(leader, PlayerGuildStatus.JOINED);
        setGuildStatus(leader, GuildStatus.INVITEONLY);
        
        // Any other checks? No sir Overdog.
        leader.sendMessage(F.main(getPluginName(), "You created a guild named " + C.Green + name + C.Gray + "."));
        leader.sendMessage(F.main(getPluginName(), "To invite players, do " + C.Green + "/guild invite <name>" + C.Gray + "."));
       
        
	}
	
	// Every few minutes, it should give 48 XP.
	
	public static void AddXp(int xp, String guildName)
	{
		int i = 1;
		try {
			i = Integer.valueOf(GetGuildXP(guildName) + xp);
		} catch (NumberFormatException | ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
        	
        	Integer answer = Integer.valueOf(GetGuildXP(guildName));
        	Integer answer2 = xp + answer;
        	SetXp(answer2, guildName);

			if (Integer.valueOf(GetGuildXP(guildName)) % 2312 == 0)
			{
				Integer ans = GetLevel(guildName);
				Integer answerrr = ans + 1;
				MySQL
				.updateSQL("UPDATE guilds SET level = '" + answerrr + "' WHERE guildName = '" + guildName + "';");
				GuildManager.guildAnnounce("Guild Level Reached: " + C.Green + answerrr, guildName);
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void SubtractXp(int xp, String guildName)
	{
        try {
        	Integer answer = Integer.valueOf(GetGuildXP(guildName));
        	Integer answer2 = answer - xp;
        	SetXp(answer2, guildName);
			

			if (Integer.valueOf(GetGuildXP(guildName)) % 2312 == 0)
			{
				MySQL
				.updateSQL("UPDATE guilds SET level = '" + GetLevel(guildName) + 1 + "' WHERE guildName = '" + guildName + "';");
				GuildManager.guildAnnounce("Guild Level Reached: " + C.Green + GetLevel(guildName), guildName);
			}
			
		} catch (ClassNotFoundException | SQLException e) {			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void SetXp(int xp, String guildName)
	{
        try {
			MySQL
			.updateSQL("UPDATE guilds SET xp = '" + String.valueOf(xp) + "' WHERE guildName = '" + guildName + "';");
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void LeaveGuild(Player leaver)
	{
		try {
			if (!IsInGuild(leaver))
			{
				leaver.sendMessage(F.main(getPluginName(), "You aren't in a guild."));
				return;
			}
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		String guildName = "";
		try {
			guildName = GetPlayerGuild(leaver);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (GetLeaderUuid(guildName).equalsIgnoreCase(leaver.getUniqueId().toString()))
		{
			leaver.sendMessage(F.main(getPluginName(), "You are the leader of the guild. Please disband or pass leadership on to someone else in order to leave!"));
			return;
		}
		
		leaver.sendMessage(F.main(getPluginName(), "You left " + C.Green + guildName + C.Gray + "!"));
		
		if (GetGuildSize(guildName) <= 0)
		{
			try {
				DeleteGuild(guildName);
			} catch (ClassNotFoundException | SQLException e) 
			{
				e.printStackTrace();
			}
		}
		try {
			try {
				GuildManager.guildAnnounce(C.Yellow + leaver.getName() + C.Gray + " has left the guild!", GetPlayerGuild(leaver));
			} catch (ClassNotFoundException | SQLException e) 
			{
				e.printStackTrace();
			}
			
			SetGuild(leaver, "NONE");
			setPermission(leaver, Permission.NONE);
			setStatus(leaver, PlayerGuildStatus.NONE);
		
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void SetGuild(Player player, String name) throws ClassNotFoundException, SQLException
	{
        MySQL
       .updateSQL("UPDATE playerGuild SET guild = '" + name + "' WHERE uuid = '" + player.getUniqueId().toString() + "';");       	
	}
	
	public static void setPermission(Player player, Permission permission) throws ClassNotFoundException, SQLException
	{
	        MySQL
	        .updateSQL("UPDATE playerGuild SET permission = '" + permission.getName() + "' WHERE uuid = '" + player.getUniqueId().toString() + "';");    
	}
	
	public static void setStatus(Player player, PlayerGuildStatus permission) throws ClassNotFoundException, SQLException
	{

	        MySQL
	        .updateSQL("UPDATE playerGuild SET status = '" + permission.getName() + "' WHERE uuid = '" + player.getUniqueId().toString() + "';");    
	}
	
	public static void setGuildStatus(Player player, GuildStatus permission) throws ClassNotFoundException, SQLException
	{

	        MySQL
	        .updateSQL("UPDATE guilds SET status = '" + permission.getName() + "' WHERE leaderUuid = '" + player.getUniqueId().toString() + "';");    
		
	}
	
	public static String GetGuildStatus(String name) throws ClassNotFoundException, SQLException
	{
        ResultSet res3 = MySQL
                .querySQL("SELECT * FROM guilds WHERE guildName = '" + name + "';");
        
        res3.next();
        
        return res3.getString(2);
	}
	
	public static String GetGuildXP(String name) throws ClassNotFoundException, SQLException
	{
        ResultSet res3 = MySQL
                .querySQL("SELECT * FROM guilds WHERE guildName = '" + name + "';");
        
        res3.next();
        
        return res3.getString(6);
	}
	
	public static String getPlayerStatus(String name) throws ClassNotFoundException, SQLException
	{
        ResultSet res3 = MySQL
                .querySQL("SELECT * FROM playerGuild WHERE uuid = '" + name + "';");
        
        res3.next();
        
        return res3.getString(3);
	}
	
	public static String getPlayerPermission(String name) throws ClassNotFoundException, SQLException
	{
        ResultSet res3 = MySQL
                .querySQL("SELECT * FROM playerGuild WHERE uuid = '" + name + "';");
        
        res3.next();
        
        return res3.getString(4);
	}
	
	public static GuildStatus GetGuildStatusEnum(String name)
	{
		try {
			return GuildStatus.valueOf(GetGuildStatus(name));
		} catch (ClassNotFoundException | SQLException e) {

		}
		return null;
	}
	
	public static void DeleteGuild(Player leader, String name) throws ClassNotFoundException, SQLException
	{
        ResultSet res3 = MySQL
                .querySQL("SELECT * FROM guilds WHERE guildName = '" + name + "';");
        if (!res3.next())
        {
        	leader.sendMessage(F.main(getPluginName(), "That guild does not exist."));
        	return;
        }
        
		try {
			if (!IsInGuild(leader))
			{
				leader.sendMessage(F.main("Guild Manager", "You aren't in a guild."));
				return;
			}
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        if (!GetLeaderUuid(name).equalsIgnoreCase(leader.getUniqueId().toString()))
        {
        	leader.sendMessage(F.main(getPluginName(), "You don't have " + C.Green + "Leader" + C.Gray + " permissions!"));
        	return;
        }
        
        int result;
        block24:
        {
            PreparedStatement preparedStatement;
            result = 0;

            preparedStatement = null;
            try
            {
                try
                {
                    Throwable throwable = null;
                    Object var6_8 = null;
                    try
                    {
                        java.sql.Connection connection = MySQL.getConnection();
                        try
                        {
                        	//Restores default permissions/status.
                        	setPermission(leader, Permission.NONE);
                        	setStatus(leader, PlayerGuildStatus.NONE);
                        	
                        	GuildManager.guildAnnounce("Your guild was deleted.", name);
                        	
                        	//Removes players from guild.
                            preparedStatement = connection.prepareStatement(DELETE_GUILD_PLAYERS);
                            preparedStatement.setString(1, name);
                            result = preparedStatement.executeUpdate();
                            
                        	//Deletes the guild from the database.
                            preparedStatement = connection.prepareStatement(DELETE_GUILD);
                            preparedStatement.setString(1, name);
                            result = preparedStatement.executeUpdate();

                        }
                        finally
                        {
                            if (connection != null)
                            {
                                connection.close();
                            }
                        }
                    }
                    catch (Throwable throwable2)
                    {
                        if (throwable == null)
                        {
                            throwable = throwable2;
                        } else if (throwable != throwable2)
                        {
                            throwable.addSuppressed(throwable2);
                        }
                        throw throwable;
                    }
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                    if (preparedStatement != null)
                    {
                        try
                        {
                            preparedStatement.close();
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    break block24;
                }
            }
            catch (Throwable throwable)
            {
                if (preparedStatement != null)
                {
                    try
                    {
                        preparedStatement.close();
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
                }
                try
                {
                    throw throwable;
                }
                catch (Throwable e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null)
            {
                try
                {
                    preparedStatement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return;
	}
	
	public static void DeleteGuild(String name) throws ClassNotFoundException, SQLException
	{
        ResultSet res3 = MySQL
                .querySQL("SELECT * FROM guilds WHERE guildName = '" + name + "';");



        int result;
        block24:
        {
            PreparedStatement preparedStatement;
            result = 0;

            preparedStatement = null;
            try
            {
                try
                {
                    Throwable throwable = null;
                    Object var6_8 = null;
                    try
                    {
                        java.sql.Connection connection = MySQL.getConnection();
                        try
                        {


                        }
                        finally
                        {
                            if (connection != null)
                            {
                                connection.close();
                            }
                        }
                    }
                    catch (Throwable throwable2)
                    {
                        if (throwable == null)
                        {
                            throwable = throwable2;
                        } else if (throwable != throwable2)
                        {
                            throwable.addSuppressed(throwable2);
                        }
                        throw throwable;
                    }
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                    if (preparedStatement != null)
                    {
                        try
                        {
                            preparedStatement.close();
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    break block24;
                }
            }
            catch (Throwable throwable)
            {
                if (preparedStatement != null)
                {
                    try
                    {
                        preparedStatement.close();
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
                }
                try
                {
                    throw throwable;
                }
                catch (Throwable e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null)
            {
                try
                {
                    preparedStatement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return;
	}
	
	
	public static String GetLeaderUuid(String guildName)
	{
        String result;

        result = "eOverdog";
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
		java.sql.Connection connection = MySQL.getConnection();
		
		try
		{
            preparedStatement = connection.prepareStatement("SELECT leaderUuid FROM guilds WHERE guildName = '" + guildName + "';");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                result = resultSet.getString(1);
            }
		} catch (Exception e)
		{
			
		}
		
		try 
		{
			preparedStatement.close();
		} catch (SQLException e) 
		{
	
		}
		
		return result;
	}
	
	public static int GetMaxLeaderboardPosition()
	{
        int result;

        result = 0;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
		java.sql.Connection connection = MySQL.getConnection();
		
		try
		{
            preparedStatement = connection.prepareStatement(RETRIEVE_MAX_LEADERBOARDS);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                result = Integer.parseInt(resultSet.getString(1));
            }
		} catch (Exception e)
		{
			
		}
		
		try 
		{
			preparedStatement.close();
		} catch (SQLException e) 
		{
	
		}
		
		return result;
	}
	
	public static int GetGuildSize(String name)
	{
        int result;

        result = 0;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
		java.sql.Connection connection = MySQL.getConnection();
		
		try
		{
            preparedStatement = connection.prepareStatement("SELECT size FROM guilds WHERE guildName = '" + name + "';");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                result = Integer.parseInt(resultSet.getString(1));
            }
		} catch (Exception e)
		{
			
		}
		
		try 
		{
			preparedStatement.close();
		} catch (SQLException e) 
		{
	
		}
		
		return result;
	}
	
	public static int GetLevel(String name)
	{
        int result;

        result = 0;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
		java.sql.Connection connection = MySQL.getConnection();
		
		try
		{
            preparedStatement = connection.prepareStatement("SELECT level FROM guilds WHERE guildName = '" + name + "';");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                result = Integer.parseInt(resultSet.getString(1));
            }
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static ArrayList<String> GetTopGuildLeaderboard(int amount)
	{
		ArrayList<String> result = null;
		result = new ArrayList<>();
		
		for (int i = 1; i < amount; i++)
		{

			ResultSet resultSet = null;
			PreparedStatement preparedStatement = null;
			java.sql.Connection connection = MySQL.getConnection();
		
			try
			{
				preparedStatement = connection.prepareStatement("SELECT guildName FROM guilds WHERE leaderboardPosition = '" + i + "';");
				resultSet = preparedStatement.executeQuery();
				resultSet.next();
				result.add(resultSet.getString(1));
				
				
			} catch (Exception e)
			{

			}
			}
		return result;
	}

	public static String GetPlayerGuild(Player player) throws ClassNotFoundException, SQLException 
	{
	       ResultSet res3 = MySQL
	                .querySQL("SELECT * FROM playerGuild WHERE uuid = '" + player.getUniqueId().toString() + "';");
	        if (res3.next())
	        {
	            if (res3.getString(2).equalsIgnoreCase("None"))
	            {
	            	return "None";
	            }
	            
	            return res3.getString(2);
	        }
	        return "None";
	}
	
	public static String GetPlayerGuild(String player) throws ClassNotFoundException, SQLException 
	{
	       ResultSet res3 = MySQL
	                .querySQL("SELECT * FROM playerGuild WHERE uuid = '" + player + "';");
	        if (res3.next())
	        {
	            if (res3.getString(2).equalsIgnoreCase("None"))
	            {
	            	return "None";
	            }
	            
	            return res3.getString(2);
	        }
	        return "None";
	}
	
	public static boolean IsInGuild(Player player) throws ClassNotFoundException, SQLException 
	{
	       ResultSet res3 = MySQL
	                .querySQL("SELECT * FROM playerGuild WHERE uuid = '" + player.getUniqueId().toString() + "';");
	        if (res3.next())
	        {
	            if (res3.getString(2).equalsIgnoreCase("None"))
	            {
	            	return false;
	            }
	            
	            return true;
	        }
	        return false;
	}

	public static void InviteToGuild(Player invitee, Player inviter) throws ClassNotFoundException, SQLException 
	{
		if (invitee == null)
		{
			inviter.sendMessage(F.main(getPluginName(), "Player is not online."));
			return;
		}
		
		if (IsInGuild(invitee))
		{
			inviter.sendMessage(F.main(getPluginName(), "Player is already in a guild!"));
			return;
		}
		
		if (!IsInGuild(inviter))
		{
			inviter.sendMessage(F.main(getPluginName(), "You aren't in a guild."));
			return;
		}
		
		setStatus(invitee, PlayerGuildStatus.INVITED);
		SetGuild(invitee, GetPlayerGuild(inviter));
		
		inviter.sendMessage(F.main(getPluginName(), "You sent an invite to " + C.Yellow + invitee.getName() + C.Gray + " to join " + C.Green + GetPlayerGuild(inviter) + C.Gray + "."));
		invitee.sendMessage(F.main(getPluginName(), "You were invited to " + C.Green + GetPlayerGuild(inviter) + C.Gray + ". Do " + C.Green + "/guild accept " + GetPlayerGuild(inviter) + C.Gray + " to accept the invite!"));
		invitee.sendMessage(F.main(getPluginName(), "To reject the invite, just ignore the invite!"));
	}

	public static boolean HasPermission(Player player, Permission permission, boolean alert)
	{
		try 
		{
		if (getPlayerPermission(player.getUniqueId().toString()).equalsIgnoreCase("NONE"))
		{
			player.sendMessage(F.main(getPluginName(), "You aren't in a guild."));
			return false;
		}
		if (permission == Permission.MEMBER)
		{
			if (!getPlayerPermission(player.getUniqueId().toString()).equalsIgnoreCase("MEMBER"))
			{
				if (alert)
				player.sendMessage(F.main(getPluginName(), "You don't have " + C.Green + "Member" + C.Gray + " permissions!"));
				return false;
			}
			
			return true;
		}
		
		if (permission == Permission.MODERATOR)
		{
			if (!Permission.MODERATOR.Has(player, Permission.MODERATOR))
			{
				if (alert)
				player.sendMessage(F.main(getPluginName(), "You don't have " + C.Green + "Moderator" + C.Gray + " permissions!"));
				return false;
			}
			
			return true;
		}
		
		if (permission == Permission.COLEADER)
		{
			if (!Permission.COLEADER.Has(player, Permission.COLEADER))
			{
				if (alert)
				player.sendMessage(F.main(getPluginName(), "You don't have " + C.Green + "Co-Leader" + C.Gray + " permissions!"));
				return false;
			}
			
			return true;
		}
		
		if (permission == Permission.LEADER)
		{
			if (!Permission.LEADER.Has(player, Permission.LEADER))
			{
				if (alert)
				player.sendMessage(F.main(getPluginName(), "You don't have " + C.Green + "Leader" + C.Gray + " permissions!"));
				return false;
			}
			return true;
		}
		
		} catch (Exception e)
		{		
			return false;
		}
		return false;
	}
	
	public static boolean IsAGuild(String guild)
	{
        ResultSet res3;
		try {
			res3 = MySQL
			        .querySQL("SELECT * FROM guilds WHERE guildName = '" + guild + "';");
        

        if (!res3.next())
        {
        	return false;
        }
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return true;
	}
	
	public static void JoinGuild(Player invitee, String guild)
	{
        ResultSet res3;
		try {
			res3 = MySQL
			        .querySQL("SELECT * FROM guilds WHERE guildName = '" + guild + "';");
        

        if (!res3.next())
        {
        	invitee.sendMessage(F.main(getPluginName(), "That isn't a guild."));
        	return;
        }
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try 
		{
			setPermission(invitee, Permission.MEMBER);
			setStatus(invitee, PlayerGuildStatus.JOINED);
			SetGuild(invitee, guild);
			try {
				GuildManager.guildAnnounce(C.Yellow + invitee.getName() + C.Gray + " has joined the guild!", GetPlayerGuild(invitee));
			} catch (ClassNotFoundException | SQLException e) 
			{
				e.printStackTrace();
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean IsInClanWithOther(Player one, Player two)
	{
		try 
		{
    		String otherGuildName = GuildRepository.GetPlayerGuild(two);
		
    		if (GuildRepository.GetPlayerGuild(one).equalsIgnoreCase(otherGuildName))
    		{
    			return true;
    		}
		} catch (Exception e)
		{
			return false;
		}
		
		return false;
	}
	public static void AcceptInvite(Player caller, Player player) 
	{
		
		player.sendMessage(F.main(getPluginName(), C.Yellow + caller.getName() + C.Gray + " has accepted your invite!"));
		caller.sendMessage(F.main(getPluginName(), "You accepted " + C.Yellow + player.getName() + C.Gray + "'s guild invite."));
		try {
			setStatus(caller, PlayerGuildStatus.JOINED);
			setPermission(caller, Permission.MEMBER);
		} catch (ClassNotFoundException | SQLException e) {
            caller.sendMessage(F.main(getPluginName(), "Error, will post logs in console."));
			e.printStackTrace();
		}
		
	}

	public static void AcceptInvite(Player caller, String getLeaderUuid) 
	{
		
		
		caller.sendMessage(F.main(getPluginName(), "You accepted " + C.Gray + "the " + C.Gray + "guild invite."));
		try {
			setStatus(caller, PlayerGuildStatus.JOINED);
			setPermission(caller, Permission.MEMBER);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			GuildManager.guildAnnounce(C.Yellow + caller.getName() + C.Gray + " has joined the guild!", GetPlayerGuild(caller));
		} catch (ClassNotFoundException | SQLException e) 
		{
            caller.sendMessage(F.main(getPluginName(), "Error, will post logs in console."));
			e.printStackTrace();
		}
		
	}

	public static void KickGuild(Player caller, String string) throws SQLException 
	{
		try {
			if (!IsInGuild(caller))
			{
				caller.sendMessage(F.main(getPluginName(), "You aren't in a guild."));
				return;
			}
		} catch (ClassNotFoundException | SQLException e1) {
            caller.sendMessage(F.main(getPluginName(), "Error, will post logs in console."));
			e1.printStackTrace();
		}
		
		
		String guildName = "";
		try {
			guildName = GetPlayerGuild(string);
		} catch (ClassNotFoundException | SQLException e) {
            caller.sendMessage(F.main(getPluginName(), "Error, will post logs in console."));
			e.printStackTrace();
		}

		try
        {
            if (GetLeaderUuid(guildName).equalsIgnoreCase(UUIDFetcher.getUUIDOf(string).toString()))
            {
                caller.sendMessage(F.main(getPluginName(), "You can't kick the leader of the guild!"));
                return;
            }
        } catch (Exception e)
        {
            caller.sendMessage(F.main(getPluginName(), "Unhandled error."));
            return;
        }
		try {
			GuildManager.guildAnnounce(C.Yellow + string + C.Gray + " has been kicked from the guild by " + C.Yellow + caller.getName() + C.Gray + "!", GetPlayerGuild(caller));
		} catch (ClassNotFoundException | SQLException e) 
		{
            caller.sendMessage(F.main(getPluginName(), "Error, will post logs in console."));
			e.printStackTrace();
		}
		
		Player player = Bukkit.getPlayer(string);
		
		if (player !=  null)
		{
			player.sendMessage(F.main("Guild Moderation", "You were kicked from " + C.Green + guildName + C.Gray + "!"));
		}

		try
        {
            SetGuild(UUIDFetcher.getUUIDOf(string).toString(), "NONE");
            setPermission(UUIDFetcher.getUUIDOf(string).toString(), Permission.NONE);
            setStatus(UUIDFetcher.getUUIDOf(string).toString(), PlayerGuildStatus.NONE);
        } catch (Exception e)
        {
            player.sendMessage(F.main("Guild", "Error!"));
        }
	}

	private static void SetGuild(String uuid, String name) {
        try {
			MySQL
      .updateSQL("UPDATE playerGuild SET guild = '" + name + "' WHERE uuid = '" + uuid + "';");
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       	
		
	}

	private static void setStatus(String uuid, PlayerGuildStatus none) {
        try {
			MySQL
			.updateSQL("UPDATE playerGuild SET status = '" + none.getName() + "' WHERE uuid = '" + uuid + "';");
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
		
	}

	public static void setPermission(String uuid, Permission none) {
        try {
			MySQL
			.updateSQL("UPDATE playerGuild SET permission = '" + none.getName() + "' WHERE uuid = '" + uuid + "';");
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
	}

	public static boolean HasPermission(String uuid, Permission permission, boolean alert) {
		try 
		{
		if (getPlayerPermission(uuid ).equalsIgnoreCase("NONE"))
		{
			return false;
		}
		if (permission == Permission.MEMBER)
		{
			if (!getPlayerPermission(uuid ).equalsIgnoreCase("MEMBER"))
			{
				return false;
			}
			
			return true;
		}
		
		if (permission == Permission.MODERATOR)
		{
			if (!getPlayerPermission(uuid ).equalsIgnoreCase("MODERATOR"))
			{
				return false;
			}
			
			
			if (!Permission.MODERATOR.Has(uuid, Permission.MODERATOR))
			{
				return false;
			}
			
			return true;
		}
		
		if (permission == Permission.COLEADER)
		{
			if (!getPlayerPermission(uuid ).equalsIgnoreCase("COLEADER"))
			{
				return false;
			}
			
			
			if (!Permission.COLEADER.Has(uuid, Permission.COLEADER))
			{
				return false;
			}
			
			return true;
		}
		
		if (permission == Permission.LEADER)
		{
			if (!getPlayerPermission(uuid ).equalsIgnoreCase("LEADER"))
			{
				return false;
			}
			
			
			if (!Permission.LEADER.Has(uuid, Permission.LEADER))
			{
				return false;
			}
			return true;
		}
		
		} catch (Exception e)
		{		
			return false;
		}
		return false;
	}
}
