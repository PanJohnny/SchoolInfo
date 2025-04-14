package me.panjohnny;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {
    public Window() {
        super("School Info Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(300, 150));
        moveToRightBottomCorner();
        setResizable(false);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setOpacity(0.9f);
        setBackground(new Color(30, 30, 30, 230));
        setFocusableWindowState(false);
        setFocusable(false);
        setType(Type.UTILITY);
        setLayout(new BorderLayout());

        // Header panel - použití FlowLayout pro lepší kontrolu uspořádání
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 40, 40));

        JLabel infoLabel = new JLabel("Informace o výuce");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        headerPanel.add(infoLabel, BorderLayout.WEST);

        // Close button
        JButton closeButton = new JButton("X");
        closeButton.addActionListener(e -> {
            System.exit(0);
        });
        closeButton.setFocusable(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setOpaque(false);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Arial", Font.PLAIN, 16));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setFocusPainted(false);
        closeButton.setToolTipText("Zavřít");

        // Přidání tlačítka do malého panelu pro lepší zarovnání
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(new JLabel("Načítání..."), BorderLayout.CENTER);

        pack();
        setVisible(true);
    }

    /**
     * Zobrazí data ve dvou sloupcích v okně.
     * @param left Pole řetězců pro levý sloupec
     * @param right Pole řetězců pro pravý sloupec
     */
    public void displayData(String[] left, String[] right) {
        // Odstranění předchozího obsahu z centrální části
        Component oldContent = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (oldContent != null) {
            remove(oldContent);
        }

        // Určení počtu řádků (použijeme větší z obou polí)
        int rowCount = Math.max(left.length, right.length);

        // Vytvoření panelu s GridLayout pro zobrazení dat
        JPanel dataPanel = new JPanel(new GridLayout(rowCount, 2, 10, 5));
        dataPanel.setBackground(new Color(30, 30, 30, 230));
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Přidání dat do panelu
        for (int i = 0; i < rowCount; i++) {
            // Levý sloupec
            JLabel leftLabel = new JLabel(i < left.length ? left[i] : "");
            leftLabel.setForeground(Color.WHITE);
            leftLabel.setHorizontalAlignment(SwingConstants.LEFT);
            dataPanel.add(leftLabel);

            // Pravý sloupec
            JLabel rightLabel = new JLabel(i < right.length ? right[i] : "");
            rightLabel.setForeground(Color.WHITE);
            rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            dataPanel.add(rightLabel);
        }

        // Přidání panelu do okna
        add(dataPanel, BorderLayout.CENTER);

        // Překreslení okna
        revalidate();
        repaint();
        pack();

        moveToRightBottomCorner();
    }

    /**
     * Přidá nový řádek dat k existujícímu obsahu.
     * @param left Text pro levý sloupec
     * @param right Text pro pravý sloupec
     */
    public void appendData(String left, String right) {
        // Získáme aktuální panel s daty
        Component centerComponent = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);

        // Pokud není žádný panel s daty, vytvoříme nový
        if (centerComponent == null || !(centerComponent instanceof JPanel) ||
                !(((JPanel) centerComponent).getLayout() instanceof GridLayout)) {
            displayData(new String[]{left}, new String[]{right});
            return;
        }

        JPanel currentPanel = (JPanel) centerComponent;
        GridLayout layout = (GridLayout) currentPanel.getLayout();
        int rowCount = layout.getRows();

        // Vytvoříme nový panel s větším počtem řádků
        JPanel newPanel = new JPanel(new GridLayout(rowCount + 1, 2, 10, 5));
        newPanel.setBackground(new Color(30, 30, 30, 230));
        newPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Zkopírujeme existující komponenty
        Component[] components = currentPanel.getComponents();
        for (Component component : components) {
            newPanel.add(component);
        }

        // Přidáme nové hodnoty
        JLabel leftLabel = new JLabel(left);
        leftLabel.setForeground(Color.WHITE);
        leftLabel.setHorizontalAlignment(SwingConstants.LEFT);
        newPanel.add(leftLabel);

        JLabel rightLabel = new JLabel(right);
        rightLabel.setForeground(Color.WHITE);
        rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        newPanel.add(rightLabel);

        // Odstraníme starý panel a přidáme nový
        remove(currentPanel);
        add(newPanel, BorderLayout.CENTER);

        // Aktualizujeme okno
        revalidate();
        repaint();
        pack();
        moveToRightBottomCorner();
    }

    public void moveToRightBottomCorner() {
        // Set location relative to bottom right corner of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) (screenSize.getWidth() - getWidth());
        int y = (int) (screenSize.getHeight() - getHeight());
        setLocation(x, y);
    }
}