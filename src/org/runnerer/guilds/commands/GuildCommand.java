package org.runnerer.guilds.commands;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.runnerer.database.MySQL;
import org.runnerer.guilds.GuildManager;
import org.runnerer.guilds.common.utils.C;
import org.runnerer.guilds.common.utils.F;
import org.runnerer.guilds.common.utils.UUIDFetcher;
import org.runnerer.guilds.enums.GuildStatus;
import org.runnerer.guilds.enums.Permission;
import org.runnerer.guilds.repository.GuildRepository;

public class GuildCommand implements CommandExecutor
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("guild"))
		{
			if (!(sender instanceof Player)) return false;

			Player player = (Player) sender;

			if (args == null || args.length == 0)
			{
				Help(player);
				return true;
			}

			GuildManager.isInGuildDatabase(player);

			if (args.length < 1)
			{
				Help(player);
				return true;
			}

			if (args[0].equalsIgnoreCase("create"))
			{
				if (args.length < 2 || args.length > 2)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);
				GuildManager.createGuild(args[1], player);
				return true;
			}

			if (args[0].equalsIgnoreCase("setstatus"))
			{
				if (args.length < 2 || args.length > 2)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);
				if (!GuildManager.hasPermission(player, Permission.LEADER, true)) return true;

				String statusString;

				if (args[1].equalsIgnoreCase("open"))
				{
					statusString = "Open";
					GuildManager.setStatus(player, GuildStatus.OPEN);
				} else if (args[1].equalsIgnoreCase("invite") || args[1].equalsIgnoreCase("inviteonly"))
				{
					statusString = "Invite Only";
					GuildManager.setStatus(player, GuildStatus.INVITEONLY);
				} else if (args[1].equalsIgnoreCase("closed"))
				{
					statusString = "Closed";
					GuildManager.setStatus(player, GuildStatus.CLOSED);
				} else
				{
					player.sendMessage(F.main("Guild Manager", "That isn't a valid guild status."));
					return true;
				}

				player.sendMessage(F.main("Guild Manager", "You set your guild status to " + C.Green + statusString + C.Gray + "."));
				return true;
			}

			if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("disband"))
			{
				if (args.length != 1)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);
				if (!GuildManager.hasPermission(player, Permission.LEADER, true)) return true;

				GuildManager.removeGuild(player);
				return true;
			}

			if (args[0].equalsIgnoreCase("join"))
			{
				if (args.length != 2)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);

				GuildManager.joinGuild(player, args[1]);
				return true;
			}

			if (args[0].equalsIgnoreCase("kick"))
			{
				if (args.length != 2)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);

				if (!GuildManager.hasPermission(player, Permission.MODERATOR, true)) return true;

				if (player.getName() == args[1])
				{
					player.sendMessage(F.main("Guild Moderation", "You can't kick yourself!"));
					return true;
				}
				try
				{
					if (!GuildManager.hasPermission(UUIDFetcher.getUUIDOf(args[1]).toString(), Permission.MODERATOR, false) && !GuildManager.hasPermission(player.getUniqueId().toString(), Permission.LEADER, false))
					{
						player.sendMessage(F.main("Guild Moderation", "You can't kick a moderator!"));
						return true;
					}
				}
				catch (Exception e)
				{

				}
				GuildManager.kickFromGuild(player, args[1]);
				return true;
			}

			if (args[0].equalsIgnoreCase("leave"))
			{
				GuildManager.isInGuildDatabase(player);
				GuildManager.leaveGuild(player);
				return true;
			}

			if (args[0].equalsIgnoreCase("accept"))
			{
				if (args.length != 2)
				{
					Help(player);
					return true;
				}
				try
				{
					GuildManager.isInGuildDatabase(player);

					if (GuildManager.getStatus(args[1]) == GuildStatus.CLOSED)
					{
						player.sendMessage(F.main("Guild Manager", "This guild is closed."));
						return true;
					}

					ResultSet res3;
					try
					{
						res3 = MySQL
								.querySQL("SELECT * FROM guilds WHERE guildName = '" + args[1] + "';");


						if (!res3.next())
						{
							player.sendMessage(F.main("Guild Manager", "That isn't a guild."));
							return true;
						}
					}
					catch (ClassNotFoundException | SQLException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					if (GuildRepository.getPlayerStatus(player.getUniqueId().toString()) == "INVITED" && !GuildRepository.GetPlayerGuild(player).equalsIgnoreCase(args[1]))
					{
						player.sendMessage(F.main("Guild Manager", "You were not invited to this guild!"));
						return true;
					}

					GuildManager.acceptGuildInvite(player, GuildRepository.GetPlayerGuild(GuildRepository.GetLeaderUuid(args[1])));
					return true;
				}
				catch (ClassNotFoundException | SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


			if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("inv"))
			{
				if (args.length != 2)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);

				try
				{
					if (GuildManager.getStatus(GuildRepository.GetPlayerGuild(player)) == GuildStatus.CLOSED)
					{
						player.sendMessage(F.main("Guild Manager", "This guild has been set to closed."));
						return true;
					}
				}
				catch (ClassNotFoundException | SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Player invitee = Bukkit.getPlayer(args[1]);

				if (invitee == null)
				{
					player.sendMessage(F.main("Guild Manager", "That player isn't online."));
					return true;
				}
				GuildManager.invitePlayerToGuild(invitee, player);
				return true;
			}

			if (args[0].equalsIgnoreCase("setrank"))
			{
				if (args.length != 3)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);

				if (!GuildManager.hasPermission(player, Permission.LEADER, true)) return true;

				try
				{
					ResultSet res3 = MySQL
							.querySQL("SELECT * FROM playerGuild WHERE uuid = '" + UUIDFetcher.getUUIDOf(args[1]).toString() + "';");

					if (!res3.next())
					{
						player.sendMessage(F.main("Guild Manager", "That player does not exist."));
						return true;
					}

					if (!GuildRepository.GetPlayerGuild(UUIDFetcher.getUUIDOf(args[1]).toString()).equalsIgnoreCase(GuildRepository.GetPlayerGuild(player)))
					{
						player.sendMessage(F.main("Guild Manager", "That player isn't in your guild."));
						return true;
					}

					if (args[1] == player.getName())
					{
						player.sendMessage(F.main("Guild Manager", "You can't set your own rank. If you're passing leadership to someone else, do /guild passleader <name>."));
						return true;
					}

					if (args[2].equalsIgnoreCase("member"))
					{
						GuildRepository.setPermission(UUIDFetcher.getUUIDOf(args[1]).toString(), Permission.MEMBER);
						player.sendMessage(F.main("Guild Manager", "You set " + C.Yellow + args[1] + C.Gray + "'s permission rank to Member."));
						return true;
					}

					if (args[2].equalsIgnoreCase("moderator"))
					{
						GuildRepository.setPermission(UUIDFetcher.getUUIDOf(args[1]).toString(), Permission.MODERATOR);
						player.sendMessage(F.main("Guild Manager", "You set " + C.Yellow + args[1] + C.Gray + "'s permission rank to Moderator."));
						return true;
					}

					if (args[2].equalsIgnoreCase("coleader"))
					{
						GuildRepository.setPermission(UUIDFetcher.getUUIDOf(args[1]).toString(), Permission.COLEADER);
						player.sendMessage(F.main("Guild Manager", "You set " + C.Yellow + args[1] + C.Gray + "'s permission rank to Co-Leader."));
						return true;
					}

					if (args[2].equalsIgnoreCase("leader"))
					{
						player.sendMessage(F.main("Guild Manager", "You can't set another player's rank to Leader. If you're passing leadership to someone else, do /guild passleader <name>."));
						return true;
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("passleader"))
			{
				if (args.length != 2)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);

				if (!GuildManager.hasPermission(player, Permission.LEADER, true)) return true;

				try
				{
					ResultSet res3 = MySQL
							.querySQL("SELECT * FROM playerGuild WHERE uuid = '" + UUIDFetcher.getUUIDOf(args[1]).toString() + "';");

					if (!res3.next())
					{
						player.sendMessage(F.main("Guild Manager", "That player does not exist."));
						return true;
					}


					if (!GuildRepository.GetPlayerGuild(UUIDFetcher.getUUIDOf(args[1]).toString()).equalsIgnoreCase(GuildRepository.GetPlayerGuild(player)))
					{
						player.sendMessage(F.main("Guild Manager", "That player isn't in your guild."));
						return true;
					}

					String uuid = UUIDFetcher.getUUIDOf(args[1]).toString();

					GuildRepository.setPermission(player, Permission.MEMBER);
					GuildRepository.setPermission(uuid, Permission.LEADER);
					player.sendMessage(F.main("Guild Leadership", "You are no longer the leader of the guild."));
					GuildManager.guildAnnounce(GuildRepository.GetPlayerGuild(player), C.Yellow + args[1] + " " + C.Gray + "is the new leader of the guild.");

				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("chat"))
			{
				if (args.length != 1)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);
				if (GuildManager.getChatUsers().contains(player))
				{
					GuildManager.removeGuildUserInChat(player);
					player.sendMessage(F.main("Guild Manager", "You disabled Guild Chat."));
				} else
				{
					GuildManager.addGuildUserInChat(player);
					player.sendMessage(F.main("Guild Manager", "You enabled Guild Chat."));
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("leaderboards"))
			{
				if (args.length != 1)
				{
					Help(player);
					return true;
				}
				GuildManager.isInGuildDatabase(player);
				int it = 5;

				if (GuildRepository.GetMaxLeaderboardPosition() < 5)
					it = GuildRepository.GetMaxLeaderboardPosition() + 1;

				int i = 1;
				for (String leaderboard : GuildManager.getGuildLeaderboards(it))
				{
					player.sendMessage(F.main("Guild Leaderboards", C.Green + i + "." + C.Gray + " " + leaderboard));
					i++;
				}
				return true;
			}

			Help(player);
		}

		return true;
	}

	private void Help(Player caller) 
	{
   		caller.sendMessage(F.main(GuildManager.getPluginName(), "Invalid arguments."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild create <name> - Creates a guild."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild delete - Deletes the guild you are currently in."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild invite <player> - Invites a player to the guild you are currently in."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild kick <player> - Kicks a player from the guild."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild setrank <player> <rank> - Kicks a player from the guild."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild createserver - Creates a private server for only the guild."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild passleader <player> - Kicks a player from the guild."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild leaderboards - Displays the guild leaderboards."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild chat - Toggles guild chat."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild accept <guild> - Accepts a guild invite."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild join <guild> - Joins a guild if they are open."));
		caller.sendMessage(F.main(GuildManager.getPluginName(), "/guild setstatus <status> - Sets the guild status of a guild."));
	}

}