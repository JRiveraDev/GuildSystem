package org.runnerer.guilds.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.runnerer.guilds.GuildManager;
import org.runnerer.guilds.common.utils.C;
import org.runnerer.guilds.common.utils.F;
import org.runnerer.guilds.repository.GuildRepository;

public class ChatManager implements Listener
{

    @EventHandler
    public void guildChat(AsyncPlayerChatEvent event)
    {
        if (!GuildManager.getChatUsers().contains(event.getPlayer())) return;

        for (Player chatUsers : GuildManager.getChatUsers())
        {
            event.setCancelled(true);

            try
            {
                chatUsers.sendMessage(C.Gold + GuildRepository.GetPlayerGuild(event.getPlayer()) + C.Yellow + " " + event.getPlayer().getName() + C.White + " " + event.getMessage());
            } catch (Exception e)
            {
                event.getPlayer().sendMessage(F.main("Guild Chat", "Could not send message."));
            }
        }
    }
}
