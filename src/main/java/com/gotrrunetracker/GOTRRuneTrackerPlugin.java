package com.gotrrunetracker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
		name = "GOTR Rune Tracker",
		description = "Track your total profit and the amount of runes you have crafted during GOTR",
		tags = {"rc", "rune", "craft", "runecraft", "runecrafting", "track", "tracker", "gotr", "guardians"}
)
public class GOTRRuneTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private GOTRRuneTrackerConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	private GOTRRuneTrackerPanel uiPanel;
	private NavigationButton uiNavigationButton;
	private GOTRRuneTrackerManager manager;

	// GOTR region tracking
    boolean isInGOTRRegion = false;
	private long lastTimeInGOTR = 0;
	private int ticksOutsideGOTR = 0;

	// GOTR related region IDs
	private static final int TEMPLE_OF_EYE_REGION = 14484; // Main GOTR area
	private static final int[] ALTAR_REGIONS = {
			11339, // Air altar
			12875, // Blood altar
			10059, // Body altar
			9035,  // Chaos altar
			8523,  // Cosmic altar
			8779,  // Death altar
			10571, // Earth altar
			10315, // Fire altar
			9803,  // Law altar
			11083, // Mind altar
			9547,  // Nature altar
			10827  // Water altar
	};

	// Rune item IDs and tracking
	private static final Map<Integer, String> RUNE_IDS = new HashMap<>();
	static {
		RUNE_IDS.put(556, "Air Runes");
		RUNE_IDS.put(565, "Blood Runes");
		RUNE_IDS.put(559, "Body Runes");
		RUNE_IDS.put(562, "Chaos Runes");
		RUNE_IDS.put(564, "Cosmic Runes");
		RUNE_IDS.put(560, "Death Runes");
		RUNE_IDS.put(557, "Earth Runes");
		RUNE_IDS.put(554, "Fire Runes");
		RUNE_IDS.put(563, "Law Runes");
		RUNE_IDS.put(558, "Mind Runes");
		RUNE_IDS.put(561, "Nature Runes");
		RUNE_IDS.put(555, "Water Runes");
	}

	// Store previous inventory to detect rune changes
	private Map<Integer, Integer> previousInventory = new HashMap<>();
	private boolean inventoryInitialized = false;

	// Animation tracking for runecrafting
	private boolean isRunecrafting = false;
	private int runecraftingTicks = 0;

	@Override
	protected void startUp() throws Exception
	{
		log.info("GOTR Rune Tracker started!");

		// Initialize the manager
		manager = new GOTRRuneTrackerManager();

		// Create the panel
		uiPanel = new GOTRRuneTrackerPanel(manager, this);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

		// Create the navigation button
		uiNavigationButton = NavigationButton.builder()
				.tooltip("Guardians of the Rift Rune Tracker")
				.icon(icon)
				.priority(10)
				.panel(uiPanel)
				.build();

		// Add to toolbar
		clientToolbar.addNavigation(uiNavigationButton);

		// Reset tracking variables
		isInGOTRRegion = false;
		lastTimeInGOTR = 0;
		ticksOutsideGOTR = 0;
		previousInventory.clear();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("GOTR Rune Tracker stopped!");

		// Clean up
		if (uiNavigationButton != null)
		{
			clientToolbar.removeNavigation(uiNavigationButton);
		}

		// Clear tracking data
		previousInventory.clear();
		inventoryInitialized = false;
		isRunecrafting = false;
		runecraftingTicks = 0;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// Optional: Add a chat message when plugin loads
			if (config.showLoadMessage())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "GOTR Rune Tracker Loaded!", null);
			}
		}
		else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			// Reset inventory tracking when logged out
			inventoryInitialized = false;
			previousInventory.clear();
			isRunecrafting = false;
			runecraftingTicks = 0;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// Initialize inventory tracking on first tick when logged in
		if (!inventoryInitialized)
		{
			updatePreviousInventory();
			inventoryInitialized = true;
		}

		// Check if player is in GOTR region
		boolean currentlyInGOTR = isPlayerInGOTRRegion();

		if (currentlyInGOTR)
		{
			// Player is in GOTR region
			if (!isInGOTRRegion)
			{
				// Just entered GOTR
				log.info("Player entered GOTR region");
				manager.setInGOTRRegion(true);
				if (config.showRegionMessages())
				{
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Entered GOTR region - tracking enabled!", null);
				}
			}

			isInGOTRRegion = true;
			lastTimeInGOTR = System.currentTimeMillis();
			ticksOutsideGOTR = 0;
		}
		else
		{
			// Player is not in GOTR region
			if (isInGOTRRegion)
			{
				// Just left GOTR
				log.info("Player left GOTR region");
				manager.setInGOTRRegion(false);
				if (config.showRegionMessages())
				{
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Left GOTR region - tracking paused", null);
				}
			}

			isInGOTRRegion = false;
			ticksOutsideGOTR++;

			// Reset due to being outside GOTR for too long
			checkForAutoReset();
		}

		// Update panel
		if (uiPanel != null)
		{
			uiPanel.updateDisplay();
		}
	}

	private boolean isPlayerInGOTRRegion()
	{
		if (client.getLocalPlayer() == null)
		{
			return false;
		}

		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		if (playerLocation == null)
		{
			return false;
		}

		int regionId = playerLocation.getRegionID();

		// Check if player is in the main GOTR region
		if (regionId == TEMPLE_OF_EYE_REGION)
		{
			return true;
		}

		// Check if player is in any of the altar regions
		for (int altarRegion : ALTAR_REGIONS)
		{
			if (regionId == altarRegion)
			{
				return true;
			}
		}

		return false;
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		// Only track inventory changes when in GOTR region
		if (!isInGOTRRegion || !inventoryInitialized || event.getContainerId() != InventoryID.INVENTORY.getId())
		{
			return;
		}

		// Get current inventory
		Map<Integer, Integer> currentInventory = getCurrentInventory();

		// Compare with previous inventory to detect changes
		for (Map.Entry<Integer, String> runeEntry : RUNE_IDS.entrySet())
		{
			int runeId = runeEntry.getKey();
			String runeName = runeEntry.getValue();

			int previousCount = previousInventory.getOrDefault(runeId, 0);
			int currentCount = currentInventory.getOrDefault(runeId, 0);

			if (currentCount > previousCount)
			{
				int runesGained = currentCount - previousCount;
				manager.addRunes(runeName, runesGained, 0);

				log.debug("Detected {} {} gained", runesGained, runeName);

				if (config.showRuneGainedMessages())
				{
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
							"+" + runesGained + " " + runeName + " tracked", null);
				}
			}
		}

		// Update inventory
		previousInventory = currentInventory;
	}

	private Map<Integer, Integer> getCurrentInventory()
	{
		Map<Integer, Integer> inventory = new HashMap<>();

		if (client.getItemContainer(InventoryID.INVENTORY) != null)
		{
			Item[] items = client.getItemContainer(InventoryID.INVENTORY).getItems();

			for (Item item : items)
			{
				if (item != null && RUNE_IDS.containsKey(item.getId()))
				{
					inventory.put(item.getId(), inventory.getOrDefault(item.getId(), 0) + item.getQuantity());
				}
			}
		}

		return inventory;
	}

	private void updatePreviousInventory()
	{
		previousInventory = getCurrentInventory();
	}

	private void checkForAutoReset()
	{
		if (!config.enableAutoReset())
		{
			return;
		}

		int resetTimeMinutes = config.autoResetTimeMinutes();
		int resetTimeTicks = resetTimeMinutes * 100;

		if (ticksOutsideGOTR >= resetTimeTicks)
		{
			log.info("Auto-resetting GOTR tracker after {} minutes outside region", resetTimeMinutes);
			manager.reset();
			ticksOutsideGOTR = 0;

			// Reset inventory tracking
			if (inventoryInitialized)
			{
				updatePreviousInventory();
			}

			if (config.showResetMessage())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
						"GOTR Tracker auto-reset after " + resetTimeMinutes + " minutes outside region", null);
			}
		}
	}

	@Provides
	GOTRRuneTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GOTRRuneTrackerConfig.class);
	}

	// Get time remaining before auto-reset
	public int getTimeUntilAutoReset()
	{
		if (!config.enableAutoReset())
		{
			return -1; // Auto-reset disabled
		}

		int resetTimeMinutes = config.autoResetTimeMinutes();
		int resetTimeTicks = resetTimeMinutes * 100;
		int ticksRemaining = resetTimeTicks - ticksOutsideGOTR;

		return Math.max(0, ticksRemaining / 100); // Convert back to minutes
	}

	public boolean isInGOTRRegion() {
        return false;
    }
}