import java.io.BufferedWriter;
import java.io.IOException;

public class CodeWriter {
	private BufferedWriter output;

	CodeWriter(BufferedWriter output) {
		this.output = output;
	}

	void setFileName(String fileName) {
		// TODO setFileName
	}
	
	void writeArithmetic(String command) throws IOException {
		// TODO writeArithmetic neg
		// TODO writeArithmetic not
		// TODO writeArithmetic eq
		// TODO writeArithmetic lt
		// TODO writeArithmetic gt
		output.write("@SP");
		output.newLine();
		output.write("M=M-1");
		output.newLine();
		output.write("A=M");
		output.newLine();
		output.write("D=M");
		output.newLine();
		output.write("@SP");
		output.newLine();
		output.write("M=M-1");
		output.newLine();
		output.write("A=M");
		output.newLine();
		switch (command) {
		case "add":
			output.write("M=D+M");
			break;
		case "sub":
			output.write("M=M-D");
			break;
		case "and":
			output.write("M=M&D");
			break;
		case "or":
			output.write("M=M|D");
			break;
		default:
			break;
		}
		output.newLine();
		output.write("@SP");
		output.newLine();
		output.write("M=M+1");
		output.newLine();
	}

	void writePushPop(CommandType commandType, String segment, int index) throws IOException {
		// TODO writePushPop。 pop にも対応できるようにする
		output.write("@" + index);
		output.newLine();
		output.write("D=A");
		output.newLine();
		output.write("@SP");
		output.newLine();
		output.write("A=M");
		output.newLine();
		output.write("M=D");
		output.newLine();
		output.write("@SP");
		output.newLine();
		output.write("M=M+1");
		output.newLine();
	}

	void close() {
		// TODO close
	}
}
