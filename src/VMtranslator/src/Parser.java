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
		// コメントは削除
		currentCommand = currentCommand.replaceAll("//.*", "");
	}

	public CommandType commandType() {
		CommandType type = CommandType.C_ARITHMETIC;

		if (currentCommand.startsWith("push"))
		{
			type = CommandType.C_PUSH;
		}
		else if (currentCommand.startsWith("pop"))
		{
			type = CommandType.C_POP;
		}
		else if (currentCommand.startsWith("label"))
		{
			type = CommandType.C_LABEL;
		}
		else if (currentCommand.startsWith("if-goto"))
		{
			type = CommandType.C_IF;
		}

		return type;
	}

	public String arg1() {
		String retString = "";

		String[] args = currentCommand.split(" ");
		if (commandType().equals(CommandType.C_ARITHMETIC))
		{
			retString = args[0];
		}
		else
		{
			if (args.length > 1)
			{
				retString = args[1];
			}
		}

		return retString;
	}

	public int arg2() {
		int retInt = 0;

		String[] args = currentCommand.split(" ");

		switch (commandType()) {
		case C_PUSH:
		case C_POP:
		case C_FUNCTION:
		case C_CALL:
			if (args.length > 2)
			{
				try {
					retInt = Integer.parseInt(args[2]);
				} catch (Exception e)
				{
					retInt = 0;
				}
			}
			break;
		default:
			break;
		}

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
