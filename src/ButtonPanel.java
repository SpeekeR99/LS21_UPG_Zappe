import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Trida predstavuje panel, na kterem jsou tlacitka pro prepinani obrazku
 *
 * @author Dominik Zappe
 * @version 2.2.0
 */
public class ButtonPanel extends JPanel {

    /** pocitadlo */
    private int pocitadlo = 0;
    /** pocitadloVrstevnice */
    private int pocitadloVrstevnice = 0;
    /** horni lista ma dulezita tlacitka */
    private final JPanel horniLista;
    /** spodni lista ma prepinaci vedlejsi tlacitka */
    private final JPanel spodniLista;
    /** defaultni barva pozadi tlacitek */
    private final Color defaultBarva;
    /** barva tlacitka, kdyz je toggle a je zmacknute */
    private final Color zmacknutyToggle;

    /**
     * Konstruktor co zarizuje, aby tlacitka prepinali obrazky
     */
    public ButtonPanel() {
        this.horniLista = new JPanel();
        horniLista.setBackground(Color.BLACK);
        horniLista.setLayout(new GridLayout(1,1,2,0));
        this.add(horniLista, BorderLayout.NORTH);
        this.spodniLista = new JPanel();
        spodniLista.setBackground(Color.BLACK);
        spodniLista.setLayout(new GridLayout(1,1,2,0));
        this.add(spodniLista, BorderLayout.SOUTH);
        this.setBackground(Color.BLACK);
        this.setLayout(new GridLayout(2,1));
        this.defaultBarva = new Color(0x2D2D2D);
        this.zmacknutyToggle = new Color(0x5F5F5F);
        nastavTlacitkaHorni();
        nastavTlacitkaSpodni();
    }

    /**
     * Nastavi tlacitka, abz hezky vypadala, a hlavne aby fungovala spravne pro horni listu
     */
    private void nastavTlacitkaHorni() {
        // Tlacitko pro export (tisk, PNG, SVG, ASCII)
        JButton export = new JButton("Export...");
        export.addActionListener(e -> {
            JOptionPane pane = new JOptionPane("Vyberte typ exportu");
            Object[] options = {"Tisk", "PNG","SVG", "ASCII", "Cancel"};
            pane.setOptions(options);
            JDialog dialog = pane.createDialog("Export");
            dialog.setVisible(true);
            Object zvolenaVolba = pane.getValue();
            if (zvolenaVolba != null) {  // Krizek dialogu nenastavi zvolenou volbu (null)
                // Tisk
                if (zvolenaVolba.equals("Tisk")) {
                    PrinterJob job = PrinterJob.getPrinterJob();
                    if (job.printDialog()) {
                        job.setPrintable(SpravceOkna.getInstance().drawingPanel);
                        try {
                            job.print();
                            JOptionPane.showMessageDialog(null, "Tisk proběhl úspěšně.", "ÚSPĚCH!", JOptionPane.INFORMATION_MESSAGE);
                        } catch (PrinterException printerException) {
                            JOptionPane.showMessageDialog(null, "Tisk se nezdařil.", "ERROR", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    // Export do PNG
                } else if (zvolenaVolba.equals("PNG")) {
                    String sirka = JOptionPane.showInputDialog(null, "Zadejte požadovanou šířku", NacitaniDat.sirka);
                    if(sirka == null) return;
                    String vyska = JOptionPane.showInputDialog(null, "Zadejte požadovanou výšku", NacitaniDat.vyska);
                    if(vyska == null) return;
                    try{
                        String soubor = Mapa_SP2021.soubor;
                        soubor = soubor.replace(".pgm", ".png");
                        BufferedImage im = new BufferedImage(Integer.parseInt(sirka), Integer.parseInt(vyska), BufferedImage.TYPE_3BYTE_BGR);
                        SpravceOkna.getInstance().drawingPanel.exportDoPNG(im.createGraphics(), Integer.parseInt(sirka), Integer.parseInt(vyska));
                        try {
                            ImageIO.write(im, "png", new File(soubor));
                            JOptionPane.showMessageDialog(null, "Zápis do PNG proběhl úspěšně.\nSoubor uložen: "+soubor, "ÚSPĚCH!", JOptionPane.INFORMATION_MESSAGE);
                            pocitadlo = 0;
                        } catch (IOException exception) {
                            JOptionPane.showMessageDialog(null, "Zápis do PNG se nezdařil.", "ERROR", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception excep) {
                        if(pocitadlo < 2) {
                            JOptionPane.showMessageDialog(null, "Nevalidní input.", "ERROR", JOptionPane.ERROR_MESSAGE);
                        } else {
                            ImageIcon icon = null;
                            try {
                                icon = new ImageIcon(new URL("https://i.imgur.com/rsis7V7.png"));
                            } catch (MalformedURLException ignored) {}
                            JOptionPane.showMessageDialog(null, "WUT?! Co to zadáváš?\nNevalidní input.", "ERROR", JOptionPane.ERROR_MESSAGE, icon);
                        }
                        JOptionPane.showMessageDialog(null, "Zápis do PNG se nezdařil.", "ERROR", JOptionPane.ERROR_MESSAGE);
                        pocitadlo++;
                    }
                } else if (zvolenaVolba.equals("SVG")) {
                    try {
                        String soubor = Mapa_SP2021.soubor;
                        soubor = soubor.replace(".pgm", ".svg");
                        String sirka = JOptionPane.showInputDialog(null, "Zadejte požadovanou šířku", NacitaniDat.sirka);
                        if (sirka == null) return;
                        String vyska = JOptionPane.showInputDialog(null, "Zadejte požadovanou výšku", NacitaniDat.vyska);
                        if (vyska == null) return;
                        try {
                            Integer.parseInt(sirka);
                            Integer.parseInt(vyska);
                        } catch (Exception exception) {
                            JOptionPane.showMessageDialog(null, "Nevalidní input.", "ERROR", JOptionPane.ERROR_MESSAGE);
                        }
                        SpravceOkna.getInstance().drawingPanel.exportDoSVG(soubor, Integer.parseInt(sirka), Integer.parseInt(vyska));
                        JOptionPane.showMessageDialog(null, "Zápis do SVG proběhl úspěšně.\nSoubor uložen: "+soubor, "ÚSPĚCH!", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception excep) {
                        JOptionPane.showMessageDialog(null, "Zápis do SVG se nezdařil.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (zvolenaVolba.equals("ASCII")) {
                    try{
                        String soubor = Mapa_SP2021.soubor;
                        soubor = soubor.replace(".pgm", ".txt");
                        SpravceOkna.getInstance().drawingPanel.exportDoASCII(soubor);
                        JOptionPane.showMessageDialog(null, "Zápis do ASCII proběhl úspěšně.\nSoubor uložen: "+soubor, "ÚSPĚCH!", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception excep) {
                        JOptionPane.showMessageDialog(null, "Zápis do ASCII se nezdařil.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        // Tlacitka pro skryvani sipek, vrstevnic
        JButton skrytSipky = new JButton("Skrýt šipky");
        skrytSipky.addActionListener(e -> {
            SpravceOkna.getInstance().drawingPanel.skryt[0] = !SpravceOkna.getInstance().drawingPanel.skryt[0];
            if(skrytSipky.getBackground().equals(defaultBarva)) {    // Zmena barvy pokud je tlacitko zamackle
                skrytSipky.setBackground(zmacknutyToggle);
            } else {
                skrytSipky.setBackground(defaultBarva);
            }
            SpravceOkna.getInstance().drawingPanel.repaint();
        });
        JButton skrytVrstevnice = new JButton("Skrýt barvy / vrstevnice");
        skrytVrstevnice.addActionListener(e -> {
            if(pocitadloVrstevnice == 0) {
                SpravceOkna.getInstance().drawingPanel.skryt[2] = !SpravceOkna.getInstance().drawingPanel.skryt[2];
            } else if(pocitadloVrstevnice == 1) {
                SpravceOkna.getInstance().drawingPanel.skryt[1] = !SpravceOkna.getInstance().drawingPanel.skryt[1];
            } else {
                SpravceOkna.getInstance().drawingPanel.skryt[1] = !SpravceOkna.getInstance().drawingPanel.skryt[1];
                SpravceOkna.getInstance().drawingPanel.skryt[2] = !SpravceOkna.getInstance().drawingPanel.skryt[2];
                pocitadloVrstevnice = -1;
            }
            pocitadloVrstevnice++;
            if(pocitadloVrstevnice > 0) {    // Zmena barvy pokud je tlacitko zamackle
                skrytVrstevnice.setBackground(zmacknutyToggle);
            } else {
                skrytVrstevnice.setBackground(defaultBarva);
            }
            SpravceOkna.getInstance().drawingPanel.repaint();
        });
        // Tlacitka pro zviditelneni oken s grafy
        JButton grafPrevyseni = new JButton("Graf převýšení");
        grafPrevyseni.addActionListener(e -> SpravceOkna.graf.setVisible(true));
        JButton histogram = new JButton("Histogram převýšení");
        histogram.addActionListener(e -> SpravceOkna.histogram.setVisible(true));
        // Uprava tlacitek, aby byla hezka
        JButton[] tlacitka = {export, grafPrevyseni, histogram, skrytSipky, skrytVrstevnice};
        for(JButton tlacitko: tlacitka) {
            tlacitko.setFont(new Font("Arial", Font.BOLD, 12));
            tlacitko.setBackground(defaultBarva);
            tlacitko.setForeground(Color.WHITE);
            tlacitko.setFocusPainted(false);
            tlacitko.setFocusable(false);
            horniLista.add(tlacitko);
        }
    }

    /**
     * Nastavi tlacitka, abz hezky vypadala, a hlavne aby fungovala spravne pro dolni listu
     */
    private void nastavTlacitkaSpodni() {
        // Tlacitka pro prepinani obrazku
        JButton ambulance = new JButton("ambulance");
        JButton carovyKod = new JButton("carovy_kod");
        JButton horizont = new JButton("horizont");
        JButton hory = new JButton("hory");
        JButton jedna = new JButton("jedna");
        JButton lenna = new JButton("lenna");
        JButton plzen = new JButton("plzen");
        JButton random = new JButton("random");
        JButton random2 = new JButton("random2");
        JButton rbf = new JButton("rbf");
        JButton upg = new JButton("upg");
        JButton jiny = new JButton("Dalsi...");
        JButton[] tlacitka = {ambulance, carovyKod, horizont, hory, jedna, lenna, plzen, random, random2, rbf, upg, jiny};
        // Menic oken
        ActionListener menicOken = e -> {
            String[] soubor = new String[1];
            if(e.getActionCommand().equals("Dalsi...")) {
                String cesta = JOptionPane.showInputDialog(null, "Zadejte cestu k požadovanému souboru", "data/");
                if(cesta == null) return;
                if(cesta.contains(".pgm")) {
                    soubor[0] = cesta;
                } else {
                    JOptionPane.showMessageDialog(null, "Zadaný soubor není formátu .pgm.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else soubor[0] = "data/"+e.getActionCommand()+".pgm";
            SpravceOkna.okno.dispose();
            SpravceOkna.graf.dispose();
            SpravceOkna.histogram.dispose();
            Mapa_SP2021.main(soubor);
        };
        // Uprava tlacitek, aby byla hezka a fungovala :)
        for(JButton tlacitko: tlacitka) {
            tlacitko.setFont(new Font("Arial", Font.BOLD, 12));
            tlacitko.setBackground(defaultBarva);
            tlacitko.setForeground(Color.WHITE);
            tlacitko.setFocusPainted(false);
            tlacitko.setFocusable(false);
            tlacitko.addActionListener(menicOken);
            spodniLista.add(tlacitko);
        }
    }

}
