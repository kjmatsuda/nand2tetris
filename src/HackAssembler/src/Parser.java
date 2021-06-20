import java.io.BufferedReader;
import java.io.IOException;

public class Parser {
	private BufferedReader input;
	private String currentCommand;

	Parser(BufferedReader input) {
		this.input = input;
	}
	public boolean hasMoreCommands() throws IOException {
		boolean hasMore = false;
		input.mark(256);
		hasMore = (input.readLine() != null);
		input.reset();
		return hasMore;
	}

	public void advance() throws IOException {
		currentCommand = input.readLine();
		while (skipLine(currentCommand))
		{
			currentCommand = input.readLine();
		}
		// 空白文字は削除
		currentCommand = currentCommand.replaceAll(" ", "");
	}

	public CommandType commandType() {
		CommandType type = CommandType.A_COMMAND;
		if (currentCommand.startsWith("@"))
		{
			type = CommandType.A_COMMAND;
		}
		else if (currentCommand.startsWith("("))
		{
			type = CommandType.L_COMMAND;
		}
		else
		{
			type = CommandType.C_COMMAND;
		}
		return type;
	}

	public String symbol() {
		String symbolStr = "";
		if (currentCommand.startsWith("@"))
		{
			symbolStr = currentCommand.substring(1);
		}
		else if (currentCommand.startsWith("("))
		{
			symbolStr = currentCommand.substring(1, currentCommand.length() - 2);
		}
		return symbolStr;
	}

	public String dest() {
		int indexOfEq = currentCommand.indexOf('=');
		return currentCommand.substring(0, indexOfEq - 1);
	}

	public String comp() {
		int indexOfEq = currentCommand.indexOf('=');
		return currentCommand.substring(indexOfEq + 1);
	}

	public String jump() {
		int indexOfSemiColon = currentCommand.indexOf(';');
		return currentCommand.substring(indexOfSemiColon + 1);
	}

	private boolean skipLine(String line) {
		boolean skip = false;
		if (line.isEmpty() || line.startsWith("//")) {
			skip = true;
		}
		return skip;
	}
}
