import org.jfree.svg.SVGGraphics2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.*;

/**
 * Trida predstavuje panel, na ktery se vykresluje
 *
 * @author Dominik Zappe
 * @version 2.2.0
 */
public class DrawingPanel extends JPanel implements Printable {

    /** obrazek data */
    private final int[] data;
    /** obrazek data original */
    private final int[] dataOriginal;
    /** obrazek original */
    private BufferedImage imgOrig;
    /** obrazek vykresleny */
    private BufferedImage img;
    /** obrazek barevnych vrstev vrstevnic original*/
    private BufferedImage vrstvyOrig;
    /** sirka obrazku */
    private final int sirka;
    /** vyska obrazku */
    private final int vyska;
    /** zvetseni obrazku */
    private double scale;
    /** posun X, Y obrazku */
    private Pozice startXY;
    /** maximalni barva v obrazku X, Y souradnice */
    private Pozice maximumXY;
    /** minimalni barva v obrazku X, Y souradnice */
    private Pozice minimumXY;
    /** maximalni stoupani X, Y souradnice */
    private Pozice stoupaniXY;
    /** pozice kde se kliklo X, Y souradnice */
    private Pozice kliknutiXY;
    /** hodnota prevyseni mista kde se kliklo */
    private String hodnotaKliknuti;
    /** mozne barvy vrstevnic */
    public static Color[] paletaVrstevnic;
    /** 0. index znaci export, 1. index znaci tisk (bila barva) */
    private final boolean[] export;
    /** 0. index znaci skryti sipek, 1. index znaci skryti vrstevnic */
    public final boolean[] skryt;
    /** zoom ovladany koleckem mysi */
    private double zoom = 1;
    /** pozice x y kde uzivatel klik zmackl */
    private Pozice klikZacatek;
    /** posun o x y v obrazku */
    private Pozice posun;
    /** prava hodnota maxima v obrazku (napr. lenna uvadi 255, ale v obrazku nikde 255 neni) */
    public int hodnotaMaxima;
    /** hodnota minima */
    private int hodnotaMinima;

    /**
     * Konstruktor nastavi nejaka data a velikost okna
     */
    public DrawingPanel(int[] data, int[] dataOriginal, int sirka, int vyska) {
        this.setPreferredSize(new Dimension(800, 600));
        this.data = data;
        this.dataOriginal = dataOriginal;
        this.sirka = sirka;
        this.vyska = vyska;
        this.export = new boolean[] {false, false};
        this.skryt = new boolean[] {false, false, false};
        this.posun = new Pozice(0, 0);
        nactiObrazek();
        nactiMinMaxStoupani();
        vytvorBarevnouPaletu();
        nastavBarvyBarevnychPloch();
        // Listenery pro interakci s uživatelem
        this.addMouseListener(new MouseListener() {
            // Klikani a oznacovani vrstevnic
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    int kurzorX = (int) ((e.getX() - startXY.x) / (scale));
                    if(kurzorX < 0 || kurzorX >= sirka) throw new Exception();
                    int kurzorY = (int) ((e.getY() - startXY.y) / (scale));
                    if(kurzorY < 0 || kurzorY >= vyska) throw new Exception();
                    if(e.getButton() == 1) {
                        kliknutiXY = new Pozice((e.getX() - startXY.x - posun.x*zoom) / (scale*zoom*zoom), (e.getY() - startXY.y - posun.y*zoom) / (scale*zoom*zoom));
                        int rgb = img.getRGB((int) ((e.getX() - startXY.x - posun.x*zoom) / zoom), (int) ((e.getY() - startXY.y - posun.y*zoom) / zoom));
                        hodnotaKliknuti = String.valueOf((((rgb >> 16) | (rgb >> 8) | (rgb)) + 256) * NacitaniDat.maximum / 255);
                    } else if(e.getButton() == 3) {
                        kliknutiXY = null;
                    }
                    repaint();
                } catch(Exception excep) {
                    JOptionPane.showMessageDialog(null, "Klikejte prosím do obrázku.", "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }
            // Tahani obrazku, kdyz je priblizeny
            @Override
            public void mousePressed(MouseEvent e) {
                klikZacatek = new Pozice(e.getX(), e.getY());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                posun.x += (e.getX() - klikZacatek.x) / zoom;
                posun.y += (e.getY() - klikZacatek.y) / zoom;
                if(posun.x > 0) posun.x = 0;
                if(posun.y > 0) posun.y = 0;
                repaint();
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        // Priblizovani / oddalovani obrazku
        this.addMouseWheelListener(e -> {
            if(e.getPreciseWheelRotation() < 0) {
                zoom += 0.05;
            } else if(zoom - 1 > 0) {
                zoom -= 0.05;
            }
            repaint();
        });
    }

    /**
     * Kresleni obrazku
     * @param g graficky kontext
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        if(posun.x < zoom*scale*(-sirka+(sirka/zoom/zoom))) posun.x = (zoom*scale*(-sirka+(sirka/zoom/zoom)));  // Kvuli resizu okna, kdyz je zazoomovano a posunuto
        if(posun.y < zoom*scale*(-vyska+(vyska/zoom/zoom))) posun.y = (zoom*scale*(-vyska+(vyska/zoom/zoom)));
        // Pozadi
        g2.setColor(Color.BLACK);
        if(export[1]) g2.setColor(Color.WHITE);
        g2.fillRect(0,0,this.getWidth(), this.getHeight());
        // Scale a start X, Y
        if(!export[0]) { // Tisk ma svuj scale a startXY, podle velikosti stranky (popr i jiny export)
            double scale_x = this.getWidth() / (double) sirka;
            double scale_y = this.getHeight() / (double) vyska;
            scale = Math.min(scale_x, scale_y);
            startXY = new Pozice((this.getWidth() - (int)(sirka*scale)) / 2.0, (this.getHeight() - (int)(vyska*scale)) / 2.0);
        }
        // Vykresleni
        g2.setClip((int)startXY.x, (int)startXY.y, (int)(sirka*scale), (int)(vyska*scale));
        g2.scale(zoom, zoom);
        g2.translate(posun.x, posun.y);
        img = new BufferedImage((int)(sirka*scale*zoom), (int)(vyska*scale*zoom), BufferedImage.TYPE_3BYTE_BGR);
        AffineTransform at = AffineTransform.getScaleInstance(scale*zoom, scale*zoom);
        AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        img = ato.filter(imgOrig, img);
        g2.drawImage(img, (int)(startXY.x/zoom), (int)(startXY.y/zoom), null);
        // Vrstevnice
        if(!skryt[1]) {
            // Plochy
            if(!skryt[2]) {
                BufferedImage vrstvy = new BufferedImage((int)(sirka*scale*zoom), (int)(vyska*scale*zoom), BufferedImage.TYPE_4BYTE_ABGR);
                vrstvy = ato.filter(vrstvyOrig, vrstvy);
                g2.drawImage(vrstvy, (int)(startXY.x/zoom), (int)(startXY.y/zoom), null);
            }
            // Cary
            g2.setColor(new Color(0x80000000, true));
            nakresliVrstevnice(g2);
            if(kliknutiXY != null && hodnotaKliknuti != null) {
                g2.setColor(new Color(0xFFFFFFFF, true));
                zvyrazniVrstevnici(g2, Integer.parseInt(hodnotaKliknuti));
            }
        }
        // Sipky
        if(!skryt[0]) {
            if(kliknutiXY != null && hodnotaKliknuti != null) {
                drawArrow(g2, hodnotaKliknuti, new Pozice(startXY.x/zoom + kliknutiXY.x * scale * zoom, startXY.y/zoom + kliknutiXY.y * scale * zoom));
            }
            drawArrow(g2, "Min. prevyseni", new Pozice(startXY.x/zoom + minimumXY.x * scale * zoom, startXY.y/zoom + minimumXY.y * scale * zoom));
            drawArrow(g2, "Max. prevyseni", new Pozice(startXY.x/zoom + maximumXY.x * scale * zoom, startXY.y/zoom + maximumXY.y * scale * zoom));
            drawArrow(g2, "Max. stoupani", new Pozice(startXY.x/zoom + stoupaniXY.x * scale * zoom, startXY.y/zoom + stoupaniXY.y * scale * zoom));
        }
        // Opakovany tisk nebo export
        export[0] = false;
        export[1] = false;
    }

    /**
     * Metoda nakresli vrstevnici jako hodne bodu tvaricich se jako cara
     * @param g2 graficky kontext
     */
    private void nakresliVrstevnice(Graphics2D g2) {
        int sirka = (int)(this.sirka/zoom*scale+2); // +2 protoze pri zoomu nekdy lehce glitchuje pravy dolni roh, tak mu pridam 2 pixely
        int vyska = (int)(this.vyska/zoom*scale+2);
        int[] hodnotyVrstevnic = new int[sirka*vyska];
        for(int y = (int)(-posun.y); y < (int)(-posun.y)+vyska; y++) {
            for(int x = (int)(-posun.x); x < (int)(-posun.x)+sirka; x++) {
                int value = 0;
                try {
                    value = img.getRGB(x, y);
                } catch(Exception ignored){}    // Pri zoomu a resizu okna tohle hazi exceptiony, i kdyz to osetruji v paintu()
                value = (((value >> 16) | (value >> 8) | (value)) + 256) * NacitaniDat.maximum / 255;
                hodnotyVrstevnic[(int)(posun.x)+x+((int)(posun.y)+y)*sirka] = value / 50;
            }
        }
        // Vykresleni vrstevnic
        for (int y = 0; y < vyska - 1; y++) {
            for (int x = 0; x < sirka - 1; x++) {
                int curr = hodnotyVrstevnic[x + y * sirka];
                int vpravo = hodnotyVrstevnic[x + 1 + y * sirka];
                int dole = hodnotyVrstevnic[x + (y + 1) * sirka];
                if (curr != vpravo) {
                    g2.draw(new Rectangle2D.Double(-posun.x+startXY.x/zoom + (x+0.5), -posun.y+startXY.y/zoom + y, 1, 1));
                }
                if (curr != dole) {
                    g2.draw(new Rectangle2D.Double(-posun.x+startXY.x/zoom + x, -posun.y+startXY.y/zoom + (y+0.5), 1, 1));
                }
            }
        }
    }

    /**
     * Metoda zvyrazni nejblizsi vrstevnici potom, co uzivatel nekam klikne
     * @param g2 graficky kontext
     * @param hodnota hodnota bodu, kam uzivatel klikl
     */
    private void zvyrazniVrstevnici(Graphics2D g2, int hodnota) {
        hodnota = hodnotaZvyrazneneVrstevnice(hodnota);
        int sirka = (int)(this.sirka/zoom*scale);
        int vyska = (int)(this.vyska/zoom*scale);
        boolean[] mapa = new boolean[sirka*vyska];
        for(int y = (int)(-posun.y); y < (int)(-posun.y)+vyska; y++) {
            for(int x = (int)(-posun.x); x < (int)(-posun.x)+sirka; x++) {
                int value = 0;
                try {
                    value = img.getRGB(x, y);
                } catch(Exception ignored){}    // Pri zoomu a resizu okna tohle hazi exceptiony, i kdyz to osetruji v paintu()
                value = (((value >> 16) | (value >> 8) | (value)) + 256) * NacitaniDat.maximum / 255;
                if(value < hodnota) {
                    mapa[(int) (posun.x) + x + ((int) (posun.y) + y) * sirka] = true;
                }
            }
        }
        // Vykresleni vrstevnic
        for (int y = 0; y < vyska - 1; y++) {
            for (int x = 0; x < sirka - 1; x++) {
                boolean curr = mapa[x + y * sirka];
                boolean vpravo = mapa[x + 1 + y * sirka];
                boolean dole = mapa[x + (y + 1) * sirka];
                if ((curr && !vpravo) || (!curr && vpravo)) {
                    g2.draw(new Rectangle2D.Double(-posun.x+startXY.x/zoom + (x+0.5), -posun.y+startXY.y/zoom + y, 1, 1));
                }
                if ((curr && !dole) || (!curr && dole)) {
                    g2.draw(new Rectangle2D.Double(-posun.x+startXY.x/zoom + x, -posun.y+startXY.y/zoom + (y+0.5), 1, 1));
                }
            }
        }
    }

    /**
     * Metoda vrati spravnou hodnotu zvyraznene vrstevnice
     * @param hodnota hodnota co se ma "zespravnit"
     * @return vraci spravnou hodnotu zvyraznene vrstevnice
     */
    private int hodnotaZvyrazneneVrstevnice(int hodnota) {
        int rozdil = hodnota;
        while(rozdil >= 25) {
            rozdil -= 50;
        }
        hodnota -= rozdil;
        // Osetreni aby minimum ukazovalo na vyssi vrstevnici v krajnim pripade (napr hodnoty 0-25)
        if(hodnota < hodnotaMinima - hodnotaMinima % 25 + 25) {
            hodnota += 50;
        }
        // Osetreni aby maximum ukazovalo na nizsi vrstevnici v krajnim pripade (napr u Plzne hodnoty 775+)
        if(hodnota > hodnotaMaxima - hodnotaMaxima % 25) {
            hodnota -= 50;
        }
        return hodnota;
    }

    /**
     * Metoda kresli hezkou sipku (implementace z meho bonusoveho ukolu 3)
     * Sipka vi jen kam ma ukazovat, zbytek se zaridi sam, pokud by sipka byla mimo okno, nevykresli se
     * @param g2 graficky kontext
     * @param popisek popisek sipky
     * @param x2y2 x a y mista kam ma sipka ukazovat
     */
    private void drawArrow(Graphics2D g2, String popisek, Pozice x2y2) {
        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        // Vyber spravneho smeru sipky
        Pozice x1y1 = vyberSpravnySmerSipky(x2y2, popisek, fm);
        if(x1y1 == null) return; // Nekresleni pokud zadna spravna sipka neexistuje a nevesla by se do okna
        // Vykresleni sipky
        vykresliSipku(g2, x1y1, x2y2);
        // Napis
        if(x1y1.y <= x2y2.y) {
            g2.drawString(popisek, (int)x1y1.x - fm.stringWidth(popisek) / 2, (int)(x1y1.y-0.5*fm.getHeight()));
        } else {
            g2.drawString(popisek, (int)x1y1.x - fm.stringWidth(popisek) / 2, (int)(x1y1.y+fm.getHeight()));
        }
    }

    /**
     * Metoda vybere spravny smer sipky, aby nevysla mimo okno
     * @param x2y2 x a y mista kam ma sipka ukazovat
     * @param popisek popisek dane sipky
     * @param fm FontMetrics, souvisi s popiskem
     * @return vraci pozici x1 a y1, pokud se sipka nevejde do okna vraci null
     */
    private Pozice vyberSpravnySmerSipky(Pozice x2y2, String popisek, FontMetrics fm) {
        Pozice x1y1;
        int delkaSipky = 60;
        double delta_fi = 2*Math.PI/360;
        for (int i = 0; i < 360; i++) {
            if(((i > 30 && i < 60) || (i > 210 && i < 240)) && (x2y2.y < startXY.y/zoom+5 || x2y2.y > startXY.y/zoom+vyska*scale-5)) continue; // Osetreni kdyz je sipka vodorovne a na kraji obrazku
            double x1 = delkaSipky*Math.cos(i*delta_fi + Math.PI*0.75)+x2y2.x;
            double y1 = delkaSipky*Math.sin(i*delta_fi + Math.PI*0.75)+x2y2.y;
            double hitboxX = Math.min(x1, Math.min(x2y2.x, x1-fm.stringWidth(popisek)/2.0));
            double hitboxY = Math.min(x2y2.y, y1-fm.getHeight());
            if(y1 > x2y2.y) hitboxY = x2y2.y;
            double hitboxW = Math.max(Math.abs(x2y2.x-x1),fm.stringWidth(popisek)/2.0)+fm.stringWidth(popisek)/2.0;
            double hitboxH = Math.abs(x2y2.y-y1)+fm.getHeight();
            if(hitboxX < startXY.x/zoom || hitboxY < startXY.y/zoom || hitboxX+hitboxW > sirka*scale*zoom + startXY.x/zoom || hitboxY+hitboxH > vyska*scale*zoom + startXY.y/zoom) {
                continue;
            }
            x1y1 = new Pozice(x1, y1);
            return x1y1;
        }
        return null;   // Nekresleni pokud zadna spravna sipka neexistuje a nevesla by se do okna
    }

    /**
     * Metoda vykresli sipku z pozice x1 y1 ukazujici na pozici x2 y2
     * @param g2 graficky kontext
     * @param x1y1 pozice odkud sipka ukazuje
     * @param x2y2 pozice kam sipka ukazuje
     */
    private void vykresliSipku(Graphics2D g2, Pozice x1y1, Pozice x2y2) {
        double tip_length = 10;
        // Vektory
        Pozice vektorU = new Pozice(x2y2.x - x1y1.x, x2y2.y - x1y1.y);
        double u_len1 = 1 / Math.sqrt(vektorU.x * vektorU.x + vektorU.y * vektorU.y);
        vektorU.x *= u_len1;
        vektorU.y *= u_len1;  // u ma delku 1
        Pozice vektorV = new Pozice(vektorU.y, -vektorU.x);
        vektorV.x *= 0.5*tip_length;
        vektorV.y *= 0.5*tip_length;  // spravna delka (pulka delky hrotu)
        // Vykresleni sipky
        BasicStroke stetecSipka = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        double strokeSize = stetecSipka.getLineWidth();
        g2.setStroke(stetecSipka);
        g2.draw(new Line2D.Double(x1y1.x, x1y1.y, x2y2.x - vektorU.x * strokeSize, x2y2.y - vektorU.y * strokeSize));
        Pozice vektorC = new Pozice(x2y2.x - vektorU.x*tip_length, x2y2.y - vektorU.y*tip_length);  // hrot sipky
        Path2D hrot = new Path2D.Double();
        Double[] hrot_x = {x2y2.x, vektorC.x + vektorV.x, vektorC.x - tip_length/5, vektorC.x - vektorV.x, x2y2.x, vektorC.x + vektorV.x};
        Double[] hrot_y = {x2y2.y, vektorC.y + vektorV.y, vektorC.y + tip_length/5, vektorC.y - vektorV.y, x2y2.y, vektorC.y + vektorV.y};
        hrot.moveTo(hrot_x[0], hrot_y[0]);
        for(int i = 1; i < hrot_x.length; i++) {
            hrot.lineTo(hrot_x[i], hrot_y[i]);
        }
        g2.fill(hrot);
    }

    /**
     * Metoda nacte obrazek do BufferedImage
     */
    private void nactiObrazek() {
        this.imgOrig = new BufferedImage(sirka, vyska, BufferedImage.TYPE_3BYTE_BGR);
        int[] array = new int[sirka*vyska];
        for (int i = 0; i < data.length; i++) {
            Color c = new Color(data[i], data[i], data[i]);
            array[i] = c.getRGB();
        }
        this.imgOrig.setRGB(0, 0, sirka, vyska, array, 0, sirka);
    }

    /**
     * Metoda nacte indexy minimalni, maximalni hodnoty a index nejvyssiho stoupani
     */
    private void nactiMinMaxStoupani() {
        int minimum = Integer.MAX_VALUE;
        int maximum = 0;
        int rozdil = -1;
        for(int y = 0; y < vyska; y++) {
            for(int x = 0; x < sirka; x++) {
                int curHodnota = new Color(imgOrig.getRGB(x,y)).getGreen();
                if(curHodnota < minimum) {
                    minimum = curHodnota;
                    hodnotaMinima = minimum * NacitaniDat.maximum / 255;
                    minimumXY = new Pozice(x, y);
                }
                if(curHodnota > maximum) {
                    maximum = curHodnota;
                    hodnotaMaxima = maximum * NacitaniDat.maximum / 255;
                    maximumXY = new Pozice(x, y);
                }
                if(x != sirka-1) {
                    int curRozdil = Math.abs(curHodnota-data[(x+1)+y*sirka]);
                    if(curRozdil > rozdil) {
                        stoupaniXY = new Pozice(x, y);
                        rozdil = curRozdil;
                    }
                }
                if(y != vyska-1) {
                    int curRozdil = Math.abs(curHodnota-data[x+(y+1)*sirka]);
                    if(curRozdil > rozdil) {
                        stoupaniXY = new Pozice(x, y);
                        rozdil = curRozdil;
                    }
                }
            }
        }
    }

    /**
     * Metoda vytvori paletu barev pro vrstevnice na miru podle obrazku (klidne i vice nez 8850 = Mt. Everest)
     * Jedna se o gradient, ktery se sikovne vytvari na miru obrazku (takze nekdy to bude duha, jindy spise jen 5 barev)
     */
    private void vytvorBarevnouPaletu() {
        int minimum = hodnotaMinima;
        paletaVrstevnic = new Color[hodnotaMaxima /50 - minimum/50 + 1];
        for(int i = 0; i < paletaVrstevnic.length; i++) {
            int petina = paletaVrstevnic.length / 5;
            if(petina == 0) petina = 1;
            if(i==0) {
                paletaVrstevnic[i] = new Color(255, 0, 0, 100);
                continue;
            }
            if(i < petina+1) {
                paletaVrstevnic[i] = new Color(255, (i)*(255/(petina)), 0, 100);
            } else if(i < 2*petina+1) {
                paletaVrstevnic[i] = new Color(255-(i-(petina))*(255/(petina)), 255, 0, 100);
            } else if(i < 3*petina+1) {
                paletaVrstevnic[i] = new Color(0, 255, (i-(2*petina))*(255/(petina)), 100);
            } else if(i < 4*petina+1) {
                paletaVrstevnic[i] = new Color(0, 255-(i-(3*petina))*(255/(petina)), 255, 100);
            } else {
                paletaVrstevnic[i] = new Color((i-(4*petina))*(255/(petina)), 0, 255, 100);
            }
        }
    }

    /**
     * Zaridi vyhodnoceni barev na vrstevnicovych plochach
     */
    private void nastavBarvyBarevnychPloch() {
        vrstvyOrig = new BufferedImage(sirka, vyska, BufferedImage.TYPE_4BYTE_ABGR);
        int minimum = hodnotaMinima;
        minimum = minimum / 50;
        int[] rgb = new int[dataOriginal.length];
        int[] moduloPadesat = new int[dataOriginal.length];
        for(int y = 0; y < vyska; y++) {
            for(int x = 0; x < sirka; x++) {
                moduloPadesat[x+y*sirka] = dataOriginal[x+y*sirka] - dataOriginal[x+y*sirka] % 50 + 50;
            }
        }
        for(int y = 0; y < vyska; y++) {
            for(int x = 0; x < sirka; x++) {
                rgb[x+y*sirka] = paletaVrstevnic[(moduloPadesat[x+y*sirka]/50) - minimum-1].getRGB();
            }
        }
        vrstvyOrig.setRGB(0,0, sirka, vyska, rgb, 0, sirka);
    }

    /**
     * Vykresleni obrazku pro tisk
     * @param graphics graficky kontext
     * @param pageFormat format stranky
     * @param pageIndex index stranky
     * @return vraci 0, kdyz je vse v poradku
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        export[0] = true;
        export[1] = true;
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle2D.Double(0,0, pageFormat.getWidth(), pageFormat.getHeight()));
        double scale_x = pageFormat.getWidth() / (double) sirka;
        double scale_y = pageFormat.getHeight() / (double) vyska;
        scale = Math.min(scale_x, scale_y);
        scale = scale - scale/50;   // Male okraje pri tisku
        startXY = new Pozice((pageFormat.getWidth() - (int)(sirka*scale)) / 2.0, (pageFormat.getHeight() - (int)(vyska*scale)) / 2.0);
        Pozice oldPosun = posun;
        posun = new Pozice(0,0);
        double oldZoom = zoom;
        zoom = 1;
        paint(graphics);
        posun = oldPosun;
        zoom = oldZoom;
        return 0;
    }

    /**
     * Vykresleni obrazku pro export do PNG (PNG obrazek bude v adresari vedle zdrojoveho PGM)
     * @param graphics graficky kontext
     * @param sirkaZadana pozadovana sirka
     * @param vyskaZadana pozadovana vyska
     */
    public void exportDoPNG(Graphics graphics, int sirkaZadana, int vyskaZadana) {
        export[0] = true;
        double scale_x = sirkaZadana / (double) sirka;
        double scale_y = vyskaZadana / (double) vyska;
        scale = Math.min(scale_x, scale_y);
        startXY = new Pozice((sirkaZadana - (int)(sirka*scale)) / 2.0, (vyskaZadana - (int)(vyska*scale)) / 2.0);
        Pozice oldPosun = posun;
        posun = new Pozice(0,0);
        double oldZoom = zoom;
        zoom = 1;
        paint(graphics);
        posun = oldPosun;
        zoom = oldZoom;
    }

    /**
     * Prevod obrazku do ascii artu (TXT ascii art bude v adresari vedle zdrojoveho PGM)
     * @param soubor cesta k souboru, co se ma vytvorit
     * @throws IOException soubor muze byt zadan spatne (resi se v ButtonPanelu)
     */
    public void exportDoASCII(String soubor) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(soubor))), true);
        String[] tabulkaAscii =     // Tabulka pro prevod z RGB do Ascii artu (muj navrh)
                {"@@","@@","&&","WW","88","BB","RR","##","HH","AA","EE","II","TT","{{","[[","((","||","**","++","^^",";;","::",",,","..","  ","  "};
        int krok = 1;
        if(sirka > 500 || vyska > 500) krok *= 2;
        if(sirka > 1000 || vyska > 1000) krok *= 2;
        if(sirka > 2000 || vyska > 2000) krok *= 2;     // Zvysovani kroku pro velke obrazky
        for(int y = 0; y < vyska; y+=krok) {
            for(int x = 0; x < sirka; x+=krok) {
                int hodnota = data[x + y*sirka];
                String ascii = tabulkaAscii[hodnota/10];
                pw.print(ascii);
            }
            pw.println();
        }
        pw.close();
    }

    /**
     * Metoda zarizuje export do SVG zapomoci knihovny JFreeSVG
     * @param soubor cesta k souboru, co se ma vytvorit
     * @throws IOException soubor muze byt zadan spatne (resi se v ButtonPanelu)
     */
    public void exportDoSVG(String soubor, int sirkaZadana, int vyskaZadana) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(soubor))), true);
        SVGGraphics2D svg = new SVGGraphics2D(sirkaZadana, vyskaZadana);
        export[0] = true;
        export[1] = true;
        double scale_x = sirkaZadana / (double) sirka;
        double scale_y = vyskaZadana / (double) vyska;
        scale = Math.min(scale_x, scale_y);
        startXY = new Pozice(0,0);
        Pozice oldPosun = posun;
        posun = new Pozice(0,0);
        double oldZoom = zoom;
        zoom = 1;
        paint(svg);
        pw.println(svg.getSVGDocument());
        pw.close();
        posun = oldPosun;
        zoom = oldZoom;
    }

}