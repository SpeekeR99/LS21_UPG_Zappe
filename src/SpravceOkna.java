import javax.swing.*;
import java.awt.*;

/**
 * Trida predstavuje spravce oken, navrhovy typ jedinacek
 *
 * @author Dominik Zappe
 * @version 2.2.0
 */
public class SpravceOkna {

    /** instance jedinacka */
    public static SpravceOkna Instance = new SpravceOkna();
    /** privatni konstruktor, jedinacek */
    private SpravceOkna() {}
    /** getInstance, jedinacek */
    public static SpravceOkna getInstance() {
        return Instance;
    }
    /** JFrame okno */
    public static JFrame okno;
    /** graf prevyseni */
    public static JFrame graf;
    /** histogram prevyseni */
    public static JFrame histogram;
    /** DrawingPanel */
    public DrawingPanel drawingPanel;
    /** tutorialovy dialog, nechceme aby vyskakoval vicekrat*/
    boolean tutorial = true;

    /**
     * Metoda zalozi okno a prida si na nej DrawingPanel
     */
    public void zalozOkno() {
        okno = new JFrame();
        okno.setTitle("SP Dominik Zappe, A20B0279P");
        okno.setSize(800, 600);

        drawingPanel = new DrawingPanel(Mapa_SP2021.data, NacitaniDat.dataOriginal, NacitaniDat.sirka, NacitaniDat.vyska);
        okno.add(drawingPanel, BorderLayout.CENTER);

        JPanel lista = new JPanel();
        lista.setLayout(new GridLayout(2,1,0,0));
        lista.setBackground(Color.BLACK);
        okno.add(lista, BorderLayout.SOUTH);
        LegendaPanel legenda = new LegendaPanel(DrawingPanel.paletaVrstevnic);
        lista.add(legenda);
        ButtonPanel buttonPanel = new ButtonPanel();
        lista.add(buttonPanel);

        okno.pack();

        okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        okno.setLocationRelativeTo(null);
        okno.setVisible(true);

        SpravceGrafu grafik = SpravceGrafu.getInstance();

        graf = new JFrame();
        graf.setTitle("Graf převýšení");
        graf.setSize(800, 600);
        graf.setLocationRelativeTo(null);
        graf.add(grafik.vytvorGrafPrevyseni(NacitaniDat.dataOriginal));

        histogram = new JFrame();
        histogram.setTitle("Histogram převýšení");
        histogram.setSize(800, 600);
        histogram.setLocationRelativeTo(null);
        histogram.add(grafik.vytvorHistogramPrevyseni(NacitaniDat.dataOriginal));

        if(tutorial)JOptionPane.showMessageDialog(okno,
                "Levým tlačítkem myši lze zobrazit převýšení v určitém bodě a označit výškově nejbližší vrstevnici.\n"+
                        "Pravým tlačítkem lze tento výběr zrušit.\n"+
                        "Kolečkem myši lze přiblížit / oddálit obrázek a levým tlačítkem lze taháním posouvat přiblížený obrázek.\n"+
                        "Pod tlačítkem Export... je dostupný tisk, export do PNG, export do SVG a export do ASCII Art. (Exportované soubory se vytvoří ve stejném adresáři jako zdrojový .pgm soubor)\n"+
                        "Dostupná jsou také tlačítka pro zobrazení grafu a histogramu převýšení.\n"+
                        "Dále jsou dostupná tlačítka pro skrývání šipek a vrstevnic (vrstevnice nejdřív skryje barvy, pak i vrstevnice jako takové).\n"+
                        "Je také možnost přepínat obrázky zapomocí tlačítek úplně dole.",
                "VÍTEJTE!",
                JOptionPane.INFORMATION_MESSAGE);
        tutorial = false;
    }

    /**
     * Metoda vytvori okno, kdyz soubor neexistuje
     */
    public void oknoError() {
        okno = new JFrame();
        okno.setTitle("ERROR: Soubor Neexistuje");
        okno.setSize(800, 600);
        okno.getContentPane().setBackground(Color.RED);

        JLabel errorMsg = new JLabel("Soubor neexistuje!");
        errorMsg.setFont(new Font("Comic Sans MS", Font.BOLD, 72));
        errorMsg.setForeground(Color.BLACK);
        okno.add(errorMsg, BorderLayout.CENTER);

        ButtonPanel buttonPanel = new ButtonPanel();
        okno.add(buttonPanel, BorderLayout.SOUTH);

        okno.pack();

        okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        okno.setLocationRelativeTo(null);
        okno.setVisible(true);
    }

}
