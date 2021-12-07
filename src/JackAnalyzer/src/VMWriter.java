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
		// TODO writePop
	}

	public void writeArithmetic(Command command) throws IOException {
		// TODO writeArithmetic
	}

	public void writeLabel(String label) throws IOException {
		// TODO writeLabel
	}

	public void writeGoto(String label) throws IOException {
		// TODO writeGoto
	}

	public void writeIf(String label) throws IOException {
		// TODO writeIf
	}

	public void writeCall(String name, int nArgs) throws IOException {
		// TODO writeCall
	}

	public void writeFunction(String name, int nLocals) throws IOException {
		// TODO writeFunction
	}

	public void writeReturn() throws IOException {
		// TODO writeReturn
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
		// TODO temp の push ってこれでよかったっけ？
		case SEGMENT_TEMP:
			segmentStr = "temp";
			break;
		default:
			break;
		}

		return segmentStr;
	}

}
