package com.gotrrunetracker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
public class GOTRRuneTrackerManager
{
    // Map to store rune type -> count
    private final Map<String, Integer> runeCounts = new HashMap<>();

    // Session start time
    private long sessionStartTime = 0;

    // Region tracking
    private boolean inGOTRRegion = false;

    public GOTRRuneTrackerManager()
    {
        reset();
    }

    /**
     * Add runes to the tracker (only if in GOTR region)
     *
     * @param runeName The name of the rune
     * @param count    How many were crafted
     * @param i
     */
    public void addRunes(String runeName, int count, int i)
    {
        if (!inGOTRRegion)
        {
            log.debug("Not adding runes - not in GOTR region");
            return;
        }

        runeCounts.put(runeName, runeCounts.getOrDefault(runeName, 0) + count);

        log.debug("Added {} {} runes", count, runeName);
    }

    /**
     * Set whether the player is in GOTR region
     */
    public void setInGOTRRegion(boolean inRegion)
    {
        this.inGOTRRegion = inRegion;
        log.debug("GOTR region status: {}", inRegion);
    }

    /**
     * Get the count for a specific rune type
     */
    public int getRuneCount(String runeName)
    {
        return runeCounts.getOrDefault(runeName, 0);
    }

    /**
     * Get total runes crafted (all types combined)
     */
    public int getTotalRunesCrafted()
    {
        return runeCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Reset all tracked data
     */
    public void reset()
    {
        runeCounts.clear();
        sessionStartTime = System.currentTimeMillis();
        log.info("GOTR Rune Tracker data reset");
    }

    /**
     * Get session duration in minutes
     */
    public long getSessionDurationMinutes()
    {
        return (System.currentTimeMillis() - sessionStartTime) / (1000 * 60);
    }

    /**
     * Get all rune types that have been crafted
     */
    public Map<String, Integer> getAllRuneCounts()
    {
        return new HashMap<>(runeCounts);
    }
}