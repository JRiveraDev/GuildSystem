package org.runnerer.guilds.enums;

import org.bukkit.entity.Player;

public enum Permission
{

	NONE("NONE", 1),
	MEMBER("MEMBER", 2),
	MODERATOR("MODERATOR", 3),
	COLEADER("COLEADER", 4),
	LEADER("LEADER", 5);

    String name;
	private int permissionLevel;

    Permission(String stringName, int permLevel)
    {
        name = name;
        permissionLevel = permLevel;
    }

    public static Permission matchName(String name)
    {
        for (Permission display : values())
        {
            if (display.getName().equalsIgnoreCase(name))
                return display;
        }
        return null;
    }


    public String getName()
    {
        return name;
    }

    public int getPermissionLevel()
    {
        return permissionLevel;
    }

    public boolean Has(Player player, Permission perm)
    {
    	if (compareTo(perm) <= 0)
    	{
    		return true;
    	}
    	
    	return false;
    }
    
    public boolean Has(String uuid, Permission perm)
    {
    	if (compareTo(perm) <= 0)
    	{
    		return true;
    	}
    	
    	return false;
    }
}