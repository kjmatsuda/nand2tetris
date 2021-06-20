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
			Code code = new Code();
			while (parser.hasMoreCommands())
			{
				parser.advance();
				CommandType commandType = parser.commandType();
				String output = "";
				switch (commandType) {
				case A_COMMAND:
					// TODO A_COMMAND　シンボルか10進数の数値の場合で分岐する
					int digitNumber = Integer.parseInt(parser.symbol());
					output = "1" + String.format("%15s", Integer.toBinaryString(digitNumber)).replace(' ', '0');
					break;
				case C_COMMAND:
					output = "111"
							+ code.comp(parser.comp())
							+ code.dest(parser.dest())
							+ code.jump(parser.jump());
					break;
				case L_COMMAND:
					// TODO L_COMMAND
					break;
				default:
					break;
				}
				writer.write(output);
				System.out.println(output);
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
