package com.gotrrunetracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("GOTRuneTracker")
public interface GOTRuneTrackerConfig extends Config
{
	@ConfigItem(
			keyName = "tracker_reset",
			name = "Enable Reset Cooldown",
			description = "If Enabled the plugin will reset your tracker after the specified cooldown.")
	default boolean gotrunetracker_enable_reset()
	{
		return false;
	}

	@ConfigItem(
			keyName = "tracker_timeout",
			name = "Reset Cooldown",
			description = "How long the plugin will wait to reset it's tracker when you're away from the GOTR area.")
	default int gotrrunetracker_reset_cooldown()
	{
		return 10;
	}
}