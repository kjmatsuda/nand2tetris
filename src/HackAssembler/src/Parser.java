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
		// コメントは削除
		currentCommand = currentCommand.replaceAll("//.*", "");
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
			symbolStr = currentCommand.substring(1, currentCommand.length() - 1);
		}
		return symbolStr;
	}

	public String dest() {
		int indexOfEq = currentCommand.indexOf('=');
		String destStr = "null";
		if (indexOfEq > 0) {
			destStr = currentCommand.substring(0, indexOfEq);
		}
		return destStr;
	}

	public String comp() {
		int indexOfEq = currentCommand.indexOf('=');
		int indexOfSemiColon = currentCommand.indexOf(';');

		int startIndex = 0;
		if (indexOfEq > 0)
		{
			startIndex = indexOfEq + 1;
		}

		int endIndex = currentCommand.length();
		if (indexOfSemiColon > 0)
		{
			endIndex = indexOfSemiColon;
		}

		return currentCommand.substring(startIndex, endIndex);
	}

	public String jump() {
		int indexOfSemiColon = currentCommand.indexOf(';');
		String jumpStr = "null";
		if (indexOfSemiColon >= 0) {
			jumpStr = currentCommand.substring(indexOfSemiColon + 1);
		}
		return jumpStr;
	}

	private boolean skipLine(String line) {
		boolean skip = false;
		if (line.isEmpty() || line.startsWith("//")) {
			skip = true;
		}
		return skip;
	}
}
