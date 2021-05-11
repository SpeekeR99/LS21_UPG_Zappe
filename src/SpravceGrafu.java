import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Arrays;

/**
 * Trida predstavuje spravce grafu, navrhovy typ jedinacek
 * Trida ma metody, ktere tvori grafy pomoci knihovny JFreeChart
 *
 * @author Dominik Zappe
 * @version 2.2.0
 */
public class SpravceGrafu {

    /** instance jedinacka */
    public static SpravceGrafu Instance = new SpravceGrafu();
    /** privatni konstruktor, jedinacek */
    private SpravceGrafu() {}
    /** getInstance, jedinacek */
    public static SpravceGrafu getInstance() {
        return Instance;
    }
    /** minimalni hodnota v datech */
    private int min;
    /** maximalni hodnota v datech */
    private int max;

    /**
     * Metoda vytvori BoxPlot graf prevyseni
     * @param data data z kterych se graf tvori
     * @return vraci ChartPanel s grafem
     */
    public ChartPanel vytvorGrafPrevyseni(int[] data) {
        // Statisticke udaje pro graf
        int suma = 0;
        for(int cislo : data) {
            suma += cislo;
        }
        double prumer = suma / (double)data.length;
        Arrays.sort(data);
        min = data[0];
        max = data[data.length-1];
        double median;
        if(data.length % 2 == 1) {
            median = data[data.length/2];
        } else {
            median = (data[data.length/2] + data[data.length/2]-1) / 2.0;
        }
        double q1;
        double q3;
        if(data.length % 4 <= 1) {
            q1 = (data[data.length/4] + data[data.length/4]-1) / 2.0;
            q3 = (data[3*data.length/4] + data[3*data.length/4]-1) / 2.0;
        } else {
            q1 = data[data.length/4];
            q3 = data[3*data.length/4];
        }
        // Graf jako takovy (boxplot)
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add(new BoxAndWhiskerItem(prumer, median, q1, q3, min, max, 0, 0, null), Mapa_SP2021.soubor, Mapa_SP2021.soubor);
        JFreeChart graf = ChartFactory.createBoxAndWhiskerChart("Graf převýšení", "Cesta k souboru", "Převýšení", dataset, true);
        // Uprava vzhledu grafu
        CategoryPlot plot = graf.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0xFFFFFF));
        plot.setRangeGridlinePaint(new Color(0x646464));
        plot.setRangeGridlinesVisible(true);
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLowerMargin(0.40);
        domainAxis.setUpperMargin(0.40);
        BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer) plot.getRenderer();
        renderer.setFillBox(true);
        renderer.setSeriesPaint(0, new Color(0x146E14));
        renderer.setSeriesOutlinePaint(0, Color.BLACK);
        renderer.setUseOutlinePaintForWhiskers(true);
        renderer.setMedianVisible(true);
        renderer.setMeanVisible(true);
        return new ChartPanel(graf);
    }

    /**
     * Metoda vytvori Histogram prevyseni
     * @param data data z kterych se graf tvori
     * @return vraci ChartPanel s grafem
     */
    public ChartPanel vytvorHistogramPrevyseni(int[] data) {
        // Statisticke udaje pro graf
        int[] cetnost = new int[DrawingPanel.paletaVrstevnic.length];
        int min = this.min;
        int max = this.max;
        min -= min % 50;
        max -= max % 50;
        for (int prevyseni : data) {
            int modulo = prevyseni % 50;
            prevyseni -= modulo;
            int index = 0;
            for(int i = min; i < max+50; i += 50) {
                if(prevyseni == i) cetnost[index]++;
                index++;
            }
        }
        // Graf jako takovy (histogram)
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(int prevyseni : cetnost) {
            dataset.addValue(prevyseni, Mapa_SP2021.soubor, min+"-"+(min+50));
            min += 50;
        }
        JFreeChart histogram = ChartFactory.createBarChart("Histogram převýšení", "Převýšení", "Četnost", dataset);
        // Uprava vzhledu grafu
        CategoryPlot plot = histogram.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0xFFFFFF));
        plot.setRangeGridlinePaint(new Color(0x646464));
        plot.setRangeGridlinesVisible(true);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getIntegerInstance()));
        renderer.setDefaultItemLabelFont(new Font("Arial", Font.PLAIN, 10));
        renderer.setDefaultItemLabelsVisible(true);
        BarRenderer br = (BarRenderer) renderer;
        br.setItemMargin(0.05);
        br.setBarPainter(new StandardBarPainter());
        br.setSeriesPaint(0, new Color(0x146E14));
        return new ChartPanel(histogram);
    }

}
