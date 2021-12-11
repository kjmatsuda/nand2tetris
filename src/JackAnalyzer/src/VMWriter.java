import java.io.BufferedWriter;
import java.io.IOException;

public class VMWriter {
	private BufferedWriter output;

	VMWriter(BufferedWriter output) {
		this.output = output;
	}

	public void writePush(Segment segment, int index) throws IOException {
		writeLine(output, "push " + segmentToString(segment) + " " + index);
	}

	public void writePop(Segment segment, int index) throws IOException {
		writeLine(output, "pop " + segmentToString(segment) + " " + index);
	}

	public void writeArithmetic(Command command) throws IOException {
		writeLine(output, commandToString(command));
	}

	public void writeLabel(String label) throws IOException {
		writeLine(output, "label " + label);
	}

	public void writeGoto(String label) throws IOException {
		writeLine(output, "goto " + label);
	}

	public void writeIf(String label) throws IOException {
		writeLine(output, "if-goto " + label);
	}

	public void writeCall(String name, int nArgs) throws IOException {
		writeLine(output, "call " + name + " " + nArgs);
	}

	public void writeFunction(String name, int nLocals) throws IOException {
		writeLine(output, "function " + name + " " + nLocals);
	}

	public void writeReturn() throws IOException {
		writeLine(output, "return");
	}

	public void close() {
		// TODO close
	}

	private void writeLine(BufferedWriter output, String str) throws IOException {
		String indentStr = "    ";
//		if (indentLevel > 0)
//		{
//			indentStr = String.format("%" + indentLevel + "s", "");
//		}
		output.write(indentStr + str);
		output.newLine();
	}

	private String segmentToString(Segment segment) {
		String segmentStr = "unknown";
		switch (segment) {
		case SEGMENT_CONST:
			segmentStr = "constant";
			break;
		case SEGMENT_ARG:
			segmentStr = "argument";
			break;
		case SEGMENT_LOCAL:
			segmentStr = "local";
			break;
		case SEGMENT_STATIC:
			segmentStr = "static";
			break;
		case SEGMENT_THIS:
			segmentStr = "this";
			break;
		case SEGMENT_THAT:
			segmentStr = "that";
			break;
		case SEGMENT_POINTER:
			segmentStr = "pointer";
			break;
		case SEGMENT_TEMP:
			segmentStr = "temp";
			break;
		default:
			break;
		}

		return segmentStr;
	}

	private String commandToString(Command command) {
		String commandStr = "add";
		switch (command) {
		case COMMAND_ADD:
			commandStr = "add";
			break;
		case COMMAND_SUB:
			commandStr = "sub";
			break;
		case COMMAND_NEG:
			commandStr = "neg";
			break;
		case COMMAND_EQ:
			commandStr = "eq";
			break;
		case COMMAND_GT:
			commandStr = "gt";
			break;
		case COMMAND_LT:
			commandStr = "lt";
			break;
		case COMMAND_AND:
			commandStr = "and";
			break;
		case COMMAND_OR:
			commandStr = "or";
			break;
		case COMMAND_NOT:
			commandStr = "not";
			break;
		default:
			break;
		}

		return commandStr;
	}

}
