import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JackAnalyzer {

	static FilenameFilter filterJack = new FilenameFilter() {
		@Override
		public boolean accept(File file, String str){
			if (str.endsWith("jack")) {
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
			System.out.println("usage: java JackAnalyzer [dirname or filename]");
			return;
		}

		Path path = Paths.get(args[0]);
		File[] files;
		String outputFileNameWithoutExtension = "";
		if (Files.isDirectory(path))
		{
			// 引数がディレクトリの場合
			File dir = new File(args[0]);
			files = dir.listFiles(filterJack);

			outputFileNameWithoutExtension = dir.getName();
		}
		else
		{
			// 引数がファイルの場合
			files = new File[1];
			files[0] = new File(args[0]);

			outputFileNameWithoutExtension = getFileNameWithoutExtension(files[0].getName());
		}

		try {
			File outputFile = new File(outputFileNameWithoutExtension + ".xml");
			OutputStreamWriter  osw = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
			output = new BufferedWriter(osw);

			CodeWriter codeWriter = new CodeWriter(output);
			codeWriter.writeInit();

			for (File inputFile: files)
			{
				codeWriter.setFileName(getFileNameWithoutExtension(inputFile.getName()));

				reader = new BufferedReader(new FileReader(inputFile));

				JackTokenizer tokenizer = new JackTokenizer(reader);

				while (tokenizer.hasMoreCommands())
				{
					tokenizer.advance();
					CommandType commandType = tokenizer.commandType();
					switch (commandType) {
					case C_PUSH:
					case C_POP:
						codeWriter.writePushPop(commandType, tokenizer.arg1(), tokenizer.arg2());
						break;
					case C_ARITHMETIC:
						codeWriter.writeArithmetic(tokenizer.arg1());
						break;
					case C_LABEL:
						codeWriter.writeLabel(tokenizer.arg1());
						break;
					case C_IF:
						codeWriter.writeIf(tokenizer.arg1());
						break;
					case C_GOTO:
						codeWriter.writeGoto(tokenizer.arg1());
						break;
					case C_FUNCTION:
						codeWriter.writeFunction(tokenizer.arg1(), tokenizer.arg2());
						break;
					case C_RETURN:
						codeWriter.writeReturn();
						break;
					case C_CALL:
						codeWriter.writeCall(tokenizer.arg1(), tokenizer.arg2());
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
	private static String getFileNameWithoutExtension(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

}