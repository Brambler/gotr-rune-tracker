package com.gotrrunetracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GOTRuneTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GOTRRuneTrackerPlugin.class);
		RuneLite.main(args);
	}
}