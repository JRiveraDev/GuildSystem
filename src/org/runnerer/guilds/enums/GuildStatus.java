package org.runnerer.guilds.enums;

public enum GuildStatus
{

	OPEN("OPEN"),
	INVITEONLY("INVITE_ONLY"),
	CLOSED("CLOSED");

    String _name; 

    GuildStatus(String name)
    {
        _name = name;
    }

    public static GuildStatus matchName(String name)
    {
        for (GuildStatus display : values())
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