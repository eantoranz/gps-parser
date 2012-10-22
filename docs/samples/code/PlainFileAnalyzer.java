import java.io.FileReader;

/*
 * This example will read the file you pass as the only argument
 */
public class PlainFileAnalyzer {

	public static void main(String[] args) {
		try {
			new GpsAnalyzer(new FileReader(args[0])).startAnalyzing();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
