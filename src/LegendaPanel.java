import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Trida predstavuje panel, na kterem je legenda pro vrstevnice
 *
 * @author Dominik Zappe
 * @version 2.2.0
 */
public class LegendaPanel extends JPanel {

    /** paleta barev */
    Color[] paleta;

    /**
     * Konstruktor nastavi paletu a barvu pozadi panelu
     * @param paleta paleta barev do legendy
     */
    public LegendaPanel(Color[] paleta){
        this.paleta = paleta;
        this.setBackground(Color.BLACK);
    }

    /**
     * Vykresli legendu barev s popisky
     * @param g graficky kontext
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        int pocetBarev = paleta.length;
        int sirkaVzorku = 25;
        int mezera = (this.getWidth() - sirkaVzorku*pocetBarev) / (pocetBarev+1);
        int maximum = SpravceOkna.getInstance().drawingPanel.hodnotaMaxima + (50 - SpravceOkna.getInstance().drawingPanel.hodnotaMaxima % 50);
        int minimum = maximum - 50*pocetBarev;
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int x = 0;
        int pocitadlo = 0;
        if((sirkaVzorku*pocetBarev + mezera*(pocetBarev+1)) < this.getWidth()) {
            for (Color barva : paleta) {
                g2.setColor(barva);
                g2.fillRect(x + mezera, 5, sirkaVzorku, sirkaVzorku);
                g2.setColor(Color.WHITE);
                String popisek = (minimum + 50 * pocitadlo) + " - " + (minimum + 50 + pocitadlo * 50);
                g2.drawString(popisek, x + mezera - fm.stringWidth(popisek) / 4, 5 + sirkaVzorku + fm.getHeight());
                x = x + mezera + sirkaVzorku;
                pocitadlo++;
            }
        } else {
            double sirka = this.getWidth() / (double)pocetBarev;
            double pozice = 0;
            for (Color barva : paleta) {
                g2.setColor(barva);
                g2.fill(new Rectangle2D.Double(pozice, 5, sirka, 25));
                pozice += sirka;
            }
            String popisek = (minimum) + " - " + (maximum);
            g2.setColor(Color.WHITE);
            g2.drawString(popisek, this.getWidth()/2 - fm.stringWidth(popisek) / 4, 30 + fm.getHeight());
        }
    }
}
