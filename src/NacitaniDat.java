import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Trida pro nacitani dat, navrhovy typ jedinacek
 *
 * @author Dominik Zappe
 * @version 2.2.0
 */
public class NacitaniDat {

    /** instance jedinacka */
    public static NacitaniDat Instance = new NacitaniDat();
    /** privatni konstruktor, jedinacek */
    private NacitaniDat() {}
    /** getInstance, jedinacek */
    public static NacitaniDat getInstance() {
        return Instance;
    }
    /** originalni data bez preskalovani barev */
    public static int[] dataOriginal;
    /** vyska obrazku */
    public static int vyska;
    /** sirka obrazku */
    public static int sirka;
    /** nejvetsi hodnota v poli */
    public static int maximum;

    /**
	 * Metoda nacte data ze souboru a ulozi je do pole cisel
	 * @param soubor soubor z ktereho se ma cist
	 * @return vraci pole cisel ze souboru
	 * @throws IOException soubor nemusi existovat
	 */
    public int[] nactiData(String soubor) throws IOException {
        List<String> data = Files.readAllLines(Paths.get(soubor));
        int index = 0;
        int resolutionIndex = 0;
        String[] radek;
        int[] resolution_maximum = new int[3];
        while(true) {
            radek = data.get(index).trim().split("\\s+");
            for(String slovo: radek) {
                boolean cislo = true;
                for(int i = 0; i < slovo.length(); i++) {
                    if(Character.digit(slovo.charAt(i),10) < 0) cislo = false;
                }
                if(cislo) {
                    resolution_maximum[resolutionIndex] = Integer.parseInt(slovo);
                    resolutionIndex++;
                }
                if(resolutionIndex == 3) break;
            }
            if(resolutionIndex == 3) break;
            index++;
        }
        sirka = resolution_maximum[0];
        vyska = resolution_maximum[1];
        maximum = resolution_maximum[2];
        while(index >= 0) {
            data.remove(index);
            index--;
        }
        return prevedNaCisla(data);
    }

    /**
	 * Metoda rozkraji radky na jednotlive Stringy a prevede je na cisla
	 * @param data data co se maji krajet
	 * @return pole cisel
	 */
    private int[] prevedNaCisla(List<String> data) {
        List<String[]> dataJednotlive = new ArrayList<>();
        for (String radek : data) {
            dataJednotlive.add(radek.trim().split("\\s+"));
        }
        data.clear();
        for (String[] strings : dataJednotlive) {
            data.addAll(Arrays.asList(strings));
        }
        int[] poleCisel = new int[vyska * sirka];
        dataOriginal = new int[vyska * sirka];
        for(int i = 0; i < poleCisel.length; i++) {
            poleCisel[i] = preskalujBarvu(Integer.parseInt(data.get(i)));
            dataOriginal[i] = Integer.parseInt(data.get(i));
        }
        return poleCisel;
    }

    /**
     * Preskalovavac barvy nejvyssi hodnota bude jakoby 255
     * @param barva hodnota barvy v intervalu < minimum ; maximum >
     * @return preskalovana hodnota barvy v intervalu < minimum ; 255 >
     */
    private int preskalujBarvu(int barva) {
        return (barva * 255 / maximum);
    }

}
