import java.nio.file.NoSuchFileException;

/**
 * Trida predstavuje semestralni praci UPG
 *
 * @author Dominik Zappe
 * @version 2.2.0
 */
public class Mapa_SP2021 {

	/** nactena data */
	public static int[] data;
	/** nazev nacteneho souboru */
	public static String soubor;

	/**
	 * Hlavni metoda tridy, spousti aplikaci
	 * @param args argumenty prikazove radky
	 */
	public static void main(String[] args) {
		SpravceOkna okenik = SpravceOkna.getInstance();
		NacitaniDat nacitac = NacitaniDat.getInstance();
		try {
			if(args.length != 0) {
				soubor = args[0];
			} else soubor = "data/plzen.pgm";
			data = nacitac.nactiData(soubor);
			okenik.zalozOkno();
		}
	 	catch(NoSuchFileException e) {
	 		System.err.println("Soubor neexistuje.");
	 		okenik.oknoError();
		}
		catch(Exception e) {
			System.err.println("Nastal jiny problem.");
			System.err.println(e.getMessage());
			System.err.println(e.toString());
		}
	}

}