import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Assembler {
	public static void main(String[] args) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
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
			writer = new BufferedWriter(new FileWriter(outputFile));

			Parser parser = new Parser(reader);
			Code code = new Code();

			// シンボルテーブルの生成
			SymbolTable symbolTable = new SymbolTable();
			// 1回目のパスでは(Xxx)のような疑似コマンドをシンボルテーブルに追加する
			int romAddress = 0;
			while (parser.hasMoreCommands())
			{
				parser.advance();
				CommandType commandType = parser.commandType();
				switch (commandType) {
				case A_COMMAND:
					romAddress++;
					break;
				case C_COMMAND:
					romAddress++;
					break;
				case L_COMMAND:
					if (!Util.isInteger(parser.symbol()))
					{
						if (!symbolTable.contains(parser.symbol()))
						{
							symbolTable.addEntry(parser.symbol(), romAddress);
						}
					}
					break;
				default:
					break;
				}
			}

			// ファイルを開き直して先頭に戻す
			reader.close();
			reader = new BufferedReader(new FileReader(inputFile));
			parser.setReader(reader);

			// パース
			int ramAddress = 16;
			while (parser.hasMoreCommands())
			{
				parser.advance();
				CommandType commandType = parser.commandType();
				String output = "";
				switch (commandType) {
				case A_COMMAND:
					int digitNumber = 0;
					if (!Util.isInteger(parser.symbol()))
					{
						if (symbolTable.contains(parser.symbol()))
						{
							digitNumber = symbolTable.getAddress(parser.symbol());
						}
						else
						{
							symbolTable.addEntry(parser.symbol(), ramAddress);
							ramAddress++;
						}
					}
					else
					{
						digitNumber = Integer.parseInt(parser.symbol());
					}
					output = "0" + String.format("%15s", Integer.toBinaryString(digitNumber)).replace(' ', '0');
					break;
				case C_COMMAND:
					output = "111"
							+ code.comp(parser.comp())
							+ code.dest(parser.dest())
							+ code.jump(parser.jump());
					break;
				case L_COMMAND:
					break;
				default:
					break;
				}
				writer.write(output);
				writer.newLine();
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
