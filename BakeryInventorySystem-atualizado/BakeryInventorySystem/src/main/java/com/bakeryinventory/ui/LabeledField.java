package com.bakeryinventory.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

public class LabeledField extends JPanel {
    public LabeledField(String label, JTextField field) {
        super(new BorderLayout(4, 4));
        setOpaque(false);
        add(new JLabel(label), BorderLayout.NORTH);
        add(field, BorderLayout.CENTER);
    }
}
