import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Assembler {
	public static void main(String[] args) {
		BufferedReader reader = null;
		FileWriter writer = null;
		if (args.length != 1 || !args[0].endsWith(".asm"))
		{
			System.out.println("usage: java Assembler XXX.asm");
			return;
		}
		int extensionIndex = args[0].lastIndexOf(".");
		String fileNameWithoutExtension = args[0].substring(0, extensionIndex);
		try {
			File inputFile = new File(args[0]);
			reader = new BufferedReader(new FileReader(inputFile));

			File outputFile = new File(fileNameWithoutExtension + ".hack");
			writer = new FileWriter(outputFile);

			Parser parser = new Parser(reader);

			while (parser.hasMoreCommands())
			{
				parser.advance();
				writer.write(parser.symbol());
				System.out.println(parser.symbol());
			}
		} catch(FileNotFoundException e) {
			System.out.println(e);
		} catch(IOException e) {
			System.out.println(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
