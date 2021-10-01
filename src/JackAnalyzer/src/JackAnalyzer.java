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
		BufferedReader input = null;
		BufferedWriter output = null;

		if (args.length != 1)
		{
			System.out.println("usage: java JackAnalyzer [dirname or filename]");
			return;
		}

		Path path = Paths.get(args[0]);
		File[] files;
		String dirName = ".";
		if (Files.isDirectory(path))
		{
			// 引数がディレクトリの場合
			dirName = args[0];
			File dir = new File(args[0]);
			files = dir.listFiles(filterJack);
		}
		else
		{
			// 引数がファイルの場合
			files = new File[1];
			files[0] = new File(args[0]);
		}

		try {
			for (File inputFile: files)
			{
				input = new BufferedReader(new FileReader(inputFile));
				File outputFile = new File(dirName + "/" + getFileNameWithoutExtension(inputFile.getName()) + "T.xml");
				OutputStreamWriter  osw = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
				output = new BufferedWriter(osw);

				CompilationEngine engine = new CompilationEngine(input, output);
				engine.compileClass();

				JackTokenizer tokenizer = new JackTokenizer(input);

				while (tokenizer.hasMoreTokens())
				{
					tokenizer.advance();
					TokenType tokenType = tokenizer.tokenType();
					switch (tokenType) {
					case TOKEN_KEYWORD:
						// TODO
						break;
					case TOKEN_SYMBOL:
						// TODO
						break;
					case TOKEN_IDENTIFIER:
						// TODO
						break;
					case TOKEN_INT_CONST:
						// TODO
						break;
					case TOKEN_STRING_CONST:
						// TODO
						break;
					default:
						break;
					}
				}
				input.close();
			}
		} catch(FileNotFoundException e) {
			System.out.println(e);
		} catch(IOException e) {
			System.out.println(e);
		} finally {
			try {
				if (input != null) {
					input.close();
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