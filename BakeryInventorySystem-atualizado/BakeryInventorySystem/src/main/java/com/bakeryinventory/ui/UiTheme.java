package com.bakeryinventory.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;

public final class UiTheme {
    public static final Color BACKGROUND = new Color(246, 247, 249);
    public static final Color PANEL = Color.WHITE;
    public static final Color PRIMARY = new Color(32, 94, 166);
    public static final Color DANGER = new Color(188, 56, 56);
    public static final Color WARNING_BG = new Color(255, 248, 220);
    public static final Color DANGER_BG = new Color(255, 232, 232);
    public static final Color STOCK_BG = new Color(232, 241, 255);

    private UiTheme() {
    }

    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Use the default look and feel if the system one is unavailable.
        }
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Button.font", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        UIManager.put("Label.font", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        UIManager.put("TextField.font", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    }

    public static void pad(JComponent component) {
        component.setBorder(new EmptyBorder(12, 12, 12, 12));
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    public static JButton dangerButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(DANGER);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    public static void card(JComponent component) {
        component.setBackground(PANEL);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 222, 229)),
                new EmptyBorder(10, 10, 10, 10)
        ));
    }
}
