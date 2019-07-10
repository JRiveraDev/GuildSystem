package org.runnerer.guilds.enums;

public enum PlayerGuildStatus
{

	NONE("NONE"),
	INVITED("INVITED"),
	JOINED("JOINED");

    String _name; 

    PlayerGuildStatus(String name)
    {
        _name = name;
    }

    public static PlayerGuildStatus matchName(String name)
    {
        for (PlayerGuildStatus display : values())
        {
            if (display.getName().equalsIgnoreCase(name))
                return display;
        }
        return null;
    }

    public String getName()
    {
        return _name;
    }
}