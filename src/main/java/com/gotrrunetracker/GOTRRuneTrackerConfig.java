package com.gotrrunetracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("gotrrunetracker")
public interface GOTRRuneTrackerConfig extends Config
{
	@ConfigItem(
			keyName = "showLoadMessage",
			name = "Show load message",
			description = "Show a chat message when the plugin loads"
	)
	default boolean showLoadMessage()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showRegionMessages",
			name = "Show region messages",
			description = "Show chat messages when entering/leaving GOTR region"
	)
	default boolean showRegionMessages()
	{
		return true;
	}

	@ConfigItem(
			keyName = "enableAutoReset",
			name = "Enable auto-reset",
			description = "Automatically reset tracker after being outside GOTR region for specified time"
	)
	default boolean enableAutoReset()
	{
		return true;
	}

	@ConfigItem(
			keyName = "autoResetTimeMinutes",
			name = "Auto-reset time (minutes)",
			description = "How long to wait outside GOTR region before auto-resetting (0 = disabled)"
	)
	default int autoResetTimeMinutes()
	{
		return 10;
	}

	@ConfigItem(
			keyName = "showResetMessage",
			name = "Show reset message",
			description = "Show a chat message when auto-reset occurs"
	)
	default boolean showResetMessage()
	{
		return true;
	}

	@ConfigItem(
			keyName = "resetOnNewSession",
			name = "Reset on new session",
			description = "Reset rune counts when starting a new GOTR session"
	)
	default boolean resetOnNewSession()
	{
		return false;
	}

	@ConfigItem(
			keyName = "showRuneGainedMessages",
			name = "Show rune gained messages",
			description = "Show chat messages when runes are detected and tracked"
	)
	default boolean showRuneGainedMessages()
	{
		return false;
	}
}