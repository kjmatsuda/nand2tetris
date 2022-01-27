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

public class JackCompiler {
	JackCompiler() {
	}

	FilenameFilter filterJack = new FilenameFilter() {
		@Override
		public boolean accept(File file, String str){
			if (str.endsWith("jack")) {
				return true;
			} else {
				return false;
			}
		}
	};

	public void compile(String source) {
		BufferedReader input = null;
		BufferedWriter outputXml = null;
		BufferedWriter outputVm = null;

		Path path = Paths.get(source);
		File[] files;
		String dirName = ".";
		if (Files.isDirectory(path))
		{
			// 引数がディレクトリの場合
			dirName = source;
			File dir = new File(source);
			files = dir.listFiles(filterJack);
		}
		else
		{
			// 引数がファイルの場合
			files = new File[1];
			files[0] = new File(source);
		}

		try {
			for (File inputFile: files)
			{
				input = new BufferedReader(new FileReader(inputFile));

				File outputFileNameXml = new File(dirName + "/" + getFileNameWithoutExtension(inputFile.getName()) + ".xml");
				OutputStreamWriter  oswXml = new OutputStreamWriter(new FileOutputStream(outputFileNameXml), "UTF-8");
				outputXml = new BufferedWriter(oswXml);

				File outputFileNameVm = new File(dirName + "/" + getFileNameWithoutExtension(inputFile.getName()) + ".vm");
				OutputStreamWriter  oswVm = new OutputStreamWriter(new FileOutputStream(outputFileNameVm), "UTF-8");
				outputVm = new BufferedWriter(oswVm);

				JackTokenizer tokenizer = new JackTokenizer(input, inputFile.getName());
				CompilationEngine engine = new CompilationEngine(tokenizer, outputXml, outputVm);
				engine.compileClass();
				System.out.println("comipilation done. (" + inputFile.getName() + ")");
				outputXml.close();
				outputVm.close();
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
				if (outputXml != null) {
					outputXml.close();
				}
				if (outputVm != null) {
					outputVm.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getFileNameWithoutExtension(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
}
