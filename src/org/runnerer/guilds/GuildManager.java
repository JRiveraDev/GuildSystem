package org.runnerer.guilds;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.runnerer.database.MySQL;
import org.runnerer.guilds.common.utils.C;
import org.runnerer.guilds.common.utils.F;
import org.runnerer.guilds.enums.GuildStatus;
import org.runnerer.guilds.enums.Permission;
import org.runnerer.guilds.repository.GuildRepository;

public class GuildManager implements Listener
{
	public static CopyOnWriteArrayList<Player> _guildChatUsers = new CopyOnWriteArrayList<>();

	public static String getPluginName()
    {
    	return "Guild Manager";
    }
	
	@EventHandler
	public void addPlayerToPlayerGuilds(PlayerJoinEvent event) throws ClassNotFoundException, SQLException
    {
		GuildManager.isInGuildDatabase(event.getPlayer());
    }
    
	public static void isInGuildDatabase(Player player)
	{
	      ResultSet res3;
		try {
			res3 = MySQL
			            .querySQL("SELECT * FROM playerGuild WHERE uuid = '" + player.getUniqueId().toString() + "';");
	        if (!res3.next())

	            MySQL
	                    .updateSQL("INSERT INTO playerGuild (`uuid`, `guild`, `status`, `permission`) VALUES ('" + player.getUniqueId().toString() + "', '" + "NONE" + "', '" + "NONE" + "', '" + "NONE" +  "');");
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static CopyOnWriteArrayList<Player> getChatUsers()
	{
		return _guildChatUsers;	
	}
	
	public static void addGuildUserInChat(Player player)
	{
		_guildChatUsers.add(player);
	}
	
	public static void removeGuildUserInChat(Player player)
	{
		_guildChatUsers.remove(player);
	}
	
	public static void createGuild(String name, Player leader)
	{
		try {
			GuildRepository.CreateGuild(leader, name);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			leader.sendMessage(F.main(getPluginName(), C.Red + "Error while creating guild."));
		}
	}
	
	public static void removeGuild(Player leader)
	{
		try {
			GuildRepository.DeleteGuild(leader, GuildRepository.GetPlayerGuild(leader));
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			leader.sendMessage(F.main(getPluginName(), C.Red + "Error while deleting guild."));
		}
	}
	
	public static void invitePlayerToGuild(Player invitee, Player inviter)
	{
		try {
			GuildRepository.InviteToGuild(invitee, inviter);
		} catch (ClassNotFoundException | SQLException e) {
			inviter.sendMessage(F.main(getPluginName(), C.Red + "Error while inviting player to guild."));
		}
	}
	
	public static void guildAnnounce(String message, String guild)
	{
    	
    		try {
	        	for (Player players : Bukkit.getOnlinePlayers())
	        	{
				
	        		if (GuildRepository.GetPlayerGuild(players).equalsIgnoreCase(guild))
	        		{
	        			if ((GuildRepository.getPlayerStatus(players.getUniqueId().toString()) == "INVITED") || (GuildRepository.getPlayerStatus(players.getUniqueId().toString()) == "NONE")) return;
	        				players.sendMessage(F.main("Guild Alert", message));
	        			}
	        		}
	        	
    		} catch (Exception e)  		
    		{
    			e.printStackTrace();
    		}	
	}
	
	public static GuildStatus getStatus(String guildName)
	{
		return GuildRepository.GetGuildStatusEnum(guildName);
	}
	
	public static boolean isOpen(String guildName)
	{
		if (getStatus(guildName) == GuildStatus.OPEN) return true;
		
		return false;
	}
	
	public static boolean isInviteOnly(String guildName)
	{
		if (getStatus(guildName) == GuildStatus.INVITEONLY) return true;
		
		return false;
	}
	
	public static boolean isClosed(String guildName)
	{
		if (getStatus(guildName) == GuildStatus.CLOSED) return true;
		
		return false;
	}
	
	public static void setStatus(Player player, GuildStatus status)
	{
		try {
			GuildRepository.setGuildStatus(player, status);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void acceptGuildInvite(Player caller, String guildName) 
	{
		GuildRepository.AcceptInvite(caller, GuildRepository.GetLeaderUuid(guildName));
	}

	public static void leaveGuild(Player caller) {
		GuildRepository.LeaveGuild(caller);
		
	}
	
	public static boolean isWithOtherPlayer(Player playerOne, Player playerTwo)
	{
		if (GuildRepository.IsInClanWithOther(playerOne, playerTwo)) return true;
		
		return false;
	}
	
	public static boolean hasPermission(Player player, Permission perm, boolean v)
	{
		if (GuildRepository.HasPermission(player, perm, v)) return true;
		
		return false;
	}

	public static void joinGuild(Player caller, String guildName) 
	{
        try {
			if (GuildRepository.IsInGuild(caller))
			{
				caller.sendMessage(F.main(getPluginName(), "You're already apart of a guild."));
				return;
			}
        
		GuildRepository.JoinGuild(caller, guildName);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String> getGuildLeaderboards(int i)
	{
		if (GuildRepository.GetMaxLeaderboardPosition() < i)
		{
		return GuildRepository.GetTopGuildLeaderboard(GuildRepository.GetMaxLeaderboardPosition() + 1);
		}
		
		return GuildRepository.GetTopGuildLeaderboard(i);
	}

	public static void kickFromGuild(Player caller, String string) 
	{
		try {
			GuildRepository.KickGuild(caller, string);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean hasPermission(String uuid, Permission moderator, boolean v) {
		if (GuildRepository.HasPermission(uuid, moderator, v)) return true;
		
		return false;
	}
}
