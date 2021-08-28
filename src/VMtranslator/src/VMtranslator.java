import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VMtranslator {

	static FilenameFilter filterVM = new FilenameFilter() {
		@Override
		public boolean accept(File file, String str){
			if (str.endsWith("vm")) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static void main(String[] args) {
		BufferedReader reader = null;
		BufferedWriter output = null;
		if (args.length != 1)
		{
			System.out.println("usage: java VMtranslator [dirname or filename]");
			return;
		}

		Path path = Paths.get(args[0]);
		File[] files;
		String outputFileNameWithoutExtension = "";
		if (Files.isDirectory(path))
		{
			// 引数がディレクトリの場合
			File dir = new File(args[0]);
			files = dir.listFiles(filterVM);

			outputFileNameWithoutExtension = dir.getName();
		}
		else
		{
			// 引数がファイルの場合
			files = new File[1];
			files[0] = new File(args[0]);

			outputFileNameWithoutExtension = files[0].getName().substring(0, files[0].getName().lastIndexOf("."));
		}

		try {
			File outputFile = new File(outputFileNameWithoutExtension + ".asm");
			OutputStreamWriter  osw = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
			output = new BufferedWriter(osw);

			CodeWriter codeWriter = new CodeWriter(output);
			codeWriter.setFileName(outputFileNameWithoutExtension);
			codeWriter.writeInit();

			for (File inputFile: files)
			{
				reader = new BufferedReader(new FileReader(inputFile));

				Parser parser = new Parser(reader);

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
					case C_LABEL:
						codeWriter.writeLabel(parser.arg1());
						break;
					case C_IF:
						codeWriter.writeIf(parser.arg1());
						break;
					case C_GOTO:
						codeWriter.writeGoto(parser.arg1());
						break;
					case C_FUNCTION:
						codeWriter.writeFunction(parser.arg1(), parser.arg2());
						break;
					case C_RETURN:
						codeWriter.writeReturn();
						break;
					case C_CALL:
						codeWriter.writeCall(parser.arg1(), parser.arg2());
						break;
					default:
						break;
					}
				}
				reader.close();
			}
			codeWriter.close();
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
