import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class VMtranslator {
	public static void main(String[] args) {
		BufferedReader reader = null;
		BufferedWriter output = null;
		if (args.length != 1)
		{
			System.out.println("usage: java VMtranslator [dirname or filename]");
			return;
		}
		// TODO ディレクトリ指定で複数のファイルを扱えるようにする
		
		int extensionIndex = args[0].lastIndexOf(".");
		String fileNameWithoutExtension = args[0].substring(0, extensionIndex);
		
		try {
			File inputFile = new File(args[0]);
			reader = new BufferedReader(new FileReader(inputFile));

			File outputFile = new File(fileNameWithoutExtension + ".asm");
			output = new BufferedWriter(new FileWriter(outputFile));

			Parser parser = new Parser(reader);
			CodeWriter	codeWriter = new CodeWriter(output);
			
			while (parser.hasMoreCommands())
			{
				parser.advance();
				CommandType commandType = parser.commandType();
				switch (commandType) {
				case C_PUSH:
				case C_POP:
					codeWriter.writePushPop(commandType, parser.arg1(), parser.arg2());
					break;
				case C_ARITHMETIC:
					codeWriter.writeArithmetic(parser.arg1());
					break;
				default:
					break;
				}
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
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
