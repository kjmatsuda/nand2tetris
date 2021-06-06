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

	// TODO commandType

	public String symbol() {
		// TODO symbol
		return currentCommand;
	}

	public String dest() {
		// TODO dest
		return "TODO dest";
	}

	public String comp() {
		// TODO comp
		return "TODO comp";
	}

	public String jump() {
		// TODO jump
		return "TODO jump";
	}

	private boolean skipLine(String line) {
		boolean skip = false;
		if (line.isEmpty() || line.startsWith("//")) {
			skip = true;
		}
		return skip;
	}
}
