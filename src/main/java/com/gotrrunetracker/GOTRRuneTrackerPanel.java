package com.gotrrunetracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GOTRRuneTrackerPanel extends PluginPanel
{
    private final GOTRRuneTrackerManager manager;
    private final GOTRRuneTrackerPlugin plugin;

    // UI Components
    private JLabel totalRunesLabel;
    private JPanel runeGridPanel;
    private JButton resetButton;

    // Rune icon mapping
    private static final String[] RUNE_ORDER = {
            "Air Runes", "Mind Runes", "Water Runes",
            "Earth Runes", "Fire Runes", "Body Runes",
            "Cosmic Runes", "Chaos Runes", "Nature Runes",
            "Law Runes", "Death Runes", "Blood Runes"
    };

    private static final Map<String, String> RUNE_ICON_FILES = new HashMap<>();
    static {
        RUNE_ICON_FILES.put("Air Runes", "air_rune.png");
        RUNE_ICON_FILES.put("Mind Runes", "mind_rune.png");
        RUNE_ICON_FILES.put("Water Runes", "water_rune.png");
        RUNE_ICON_FILES.put("Earth Runes", "earth_rune.png");
        RUNE_ICON_FILES.put("Fire Runes", "fire_rune.png");
        RUNE_ICON_FILES.put("Body Runes", "body_rune.png");
        RUNE_ICON_FILES.put("Cosmic Runes", "cosmic_rune.png");
        RUNE_ICON_FILES.put("Chaos Runes", "chaos_rune.png");
        RUNE_ICON_FILES.put("Nature Runes", "nature_rune.png");
        RUNE_ICON_FILES.put("Law Runes", "law_rune.png");
        RUNE_ICON_FILES.put("Death Runes", "death_rune.png");
        RUNE_ICON_FILES.put("Blood Runes", "blood_rune.png");
    }

    // Store rune panels for easy updating
    private Map<String, JLabel> runeCountLabels = new HashMap<>();

    public GOTRRuneTrackerPanel(GOTRRuneTrackerManager manager)
    {
        this.manager = manager;
        this.plugin = null; // We'll need to pass this in later

        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        initializeComponents();
        updateDisplay();
    }

    // Constructor that accepts plugin reference
    public GOTRRuneTrackerPanel(GOTRRuneTrackerManager manager, GOTRRuneTrackerPlugin plugin)
    {
        this.manager = manager;
        this.plugin = plugin;

        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        initializeComponents();
        updateDisplay();
    }

    private void initializeComponents()
    {
        // Create main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("GOTR Rune Tracker");
        titleLabel.setForeground(ColorScheme.BRAND_ORANGE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createVerticalStrut(10));

        // Session info panel
        JPanel sessionPanel = createInfoPanel();
        mainPanel.add(sessionPanel);

        mainPanel.add(Box.createVerticalStrut(10));

        // Rune grid panel
        runeGridPanel = createRuneGridPanel();
        mainPanel.add(runeGridPanel);

        mainPanel.add(Box.createVerticalStrut(10));

        // Reset button
        resetButton = new JButton("Reset Session");
        resetButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                resetSession();
            }
        });
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(resetButton);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createInfoPanel()
    {
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        infoPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Total runes
        totalRunesLabel = new JLabel("Total Runes: 0");
        totalRunesLabel.setForeground(Color.WHITE);
        infoPanel.add(totalRunesLabel);

        return infoPanel;
    }

    private JPanel createRuneGridPanel()
    {
        JPanel gridPanel = new JPanel(new GridLayout(4, 3, 5, 5));
        gridPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create panels for each rune in the defined order
        for (String runeName : RUNE_ORDER)
        {
            JPanel runePanel = createRunePanel(runeName);
            gridPanel.add(runePanel);
        }

        return gridPanel;
    }

    private JPanel createRunePanel(String runeName)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new EmptyBorder(8, 5, 8, 5));
        panel.setPreferredSize(new Dimension(80, 70));

        // Try to load the rune icon
        String iconFile = RUNE_ICON_FILES.get(runeName);
        BufferedImage runeIcon = null;

        try
        {
            runeIcon = ImageUtil.loadImageResource(getClass(), iconFile);
        }
        catch (Exception e)
        {
            log.warn("Could not load icon for {}: {}", runeName, iconFile);
        }

        // Create icon label
        JLabel iconLabel;
        if (runeIcon != null)
        {
            Image scaledIcon = runeIcon.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(scaledIcon));
        }
        else
        {
            // if icon not found
            String shortName = runeName.replace(" Runes", "").substring(0, 1);
            iconLabel = new JLabel(shortName, SwingConstants.CENTER);
            iconLabel.setForeground(Color.LIGHT_GRAY);
            iconLabel.setFont(iconLabel.getFont().deriveFont(Font.BOLD, 16f));
        }

        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(iconLabel);

        // Create label
        JLabel countLabel = new JLabel("0", SwingConstants.CENTER);
        countLabel.setForeground(Color.WHITE);
        countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD, 12f));
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        runeCountLabels.put(runeName, countLabel);

        panel.add(Box.createVerticalStrut(2));
        panel.add(countLabel);
        panel.setToolTipText(runeName);

        return panel;
    }

    /**
     * Update the display with current data
     */
    public void updateDisplay()
    {
        SwingUtilities.invokeLater(() -> {
            // Update summary labels
            totalRunesLabel.setText("Total Runes: " + QuantityFormatter.formatNumber(manager.getTotalRunesCrafted()));

            // Update rune grid
            updateRuneGrid();

            revalidate();
            repaint();
        });
    }

    private void updateRuneGrid()
    {
        Map<String, Integer> runeCounts = manager.getAllRuneCounts();

        // Update each rune count label
        for (String runeName : RUNE_ORDER)
        {
            JLabel countLabel = runeCountLabels.get(runeName);
            if (countLabel != null)
            {
                int count = runeCounts.getOrDefault(runeName, 0);
                countLabel.setText(QuantityFormatter.formatNumber(count));

                // Change color based on whether runes have been crafted
                if (count > 0)
                {
                    countLabel.setForeground(Color.WHITE);
                }
                else
                {
                    countLabel.setForeground(Color.GRAY);
                }
            }
        }
    }

    private void resetSession()
    {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset all session data?",
                "Reset Session",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION)
        {
            manager.reset();
            updateDisplay();
        }
    }

}