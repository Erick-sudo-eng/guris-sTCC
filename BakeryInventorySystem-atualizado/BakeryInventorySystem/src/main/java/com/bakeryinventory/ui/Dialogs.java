package com.bakeryinventory.ui;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class Dialogs {
    private Dialogs() {
    }

    public static void error(Component parent, Exception ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Informacao", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirmar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
