package org.runnerer.guilds;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.runnerer.database.MySQL;
import org.runnerer.guilds.chat.ChatManager;

import java.sql.SQLException;

public class Guilds extends JavaPlugin
{

    @Override
    public void onEnable()
    {
        new MySQL();
        try
        {
            MySQL.Instance.openConnection();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Bukkit.getPluginManager().registerEvents(new GuildManager(), this);
        Bukkit.getPluginManager().registerEvents(new ChatManager(), this);
    }
}
