import java.io.BufferedReader;
import java.io.IOException;

public class Parser {
	private BufferedReader input;
	private String currentCommand;

	Parser(BufferedReader input) {
		this.input = input;
	}

	public void setReader(BufferedReader input) {
		this.input = input;
	}

	public boolean hasMoreCommands() throws IOException {
		// TODO hasMoreCommands
		boolean hasMore = false;
		input.mark(256);
		hasMore = (input.readLine() != null);
		input.reset();
		return hasMore;
	}

	public void advance() throws IOException {
		// TODO advance
		currentCommand = input.readLine();
		while (skipLine(currentCommand))
		{
			currentCommand = input.readLine();
		}
		// 空白文字は削除
		currentCommand = currentCommand.replaceAll(" ", "");
		// コメントは削除
		currentCommand = currentCommand.replaceAll("//.*", "");
	}

	public CommandType commandType() {
		// TODO advance
		CommandType type = CommandType.C_ARITHMETIC;
		return type;
	}

	public String arg1() {
		// TODO arg1
		String retString = "";
		
		return retString;
	}

	public int arg2() {
		// TODO arg2
		int retInt = 0;
		
		return retInt;
	}
	
	private boolean skipLine(String line) {
		boolean skip = false;
		if (line.isEmpty() || line.startsWith("//")) {
			skip = true;
		}
		return skip;
	}
}
