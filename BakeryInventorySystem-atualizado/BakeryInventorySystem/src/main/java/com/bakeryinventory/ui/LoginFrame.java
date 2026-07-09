package com.bakeryinventory.ui;

import com.bakeryinventory.model.User;
import com.bakeryinventory.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        super("Estoque da Padaria - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(390, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        UiTheme.pad(root);

        JLabel title = new JLabel("Estoque da Padaria");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(new LabeledField("Usuario", usernameField));
        form.add(new LabeledField("Senha", passwordField));
        root.add(form, BorderLayout.CENTER);

        JButton loginButton = UiTheme.primaryButton("Entrar");
        loginButton.addActionListener(event -> login());
        getRootPane().setDefaultButton(loginButton);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(loginButton);
        root.add(actions, BorderLayout.SOUTH);

        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setContentPane(root);
    }

    private void login() {
        try {
            User user = authService.login(usernameField.getText(), new String(passwordField.getPassword()));
            MainFrame mainFrame = new MainFrame(user);
            mainFrame.setVisible(true);
            dispose();
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }
}
