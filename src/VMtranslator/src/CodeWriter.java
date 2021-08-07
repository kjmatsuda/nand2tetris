import java.io.BufferedWriter;
import java.io.IOException;

public class CodeWriter {
	private BufferedWriter output;
	private int condLabelNum = 0;
	private final int tempBaseAddress = 5;

	CodeWriter(BufferedWriter output) {
		this.output = output;
	}

	void setFileName(String fileName) {
		// TODO setFileName
	}

	void writeArithmetic(String command) throws IOException {
		if (isUnaryOperator(command))
		{
			// 単項演算子の場合
			output.write("@SP");
			output.newLine();
			output.write("M=M-1");
			output.newLine();
			output.write("A=M");
			output.newLine();
			switch (command) {
			case "neg":
				output.write("M=-M");
				break;
			case "not":
				output.write("M=!M");
				break;
			}
			output.newLine();
			output.write("@SP");
			output.newLine();
			output.write("M=M+1");
			output.newLine();
		}
		else
		{
			// 二項演算子の場合
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

			if (isBinaryArithmeticOperator(command))
			{
				writeBinaryArithmeticOperator(command);
			}
			else
			{
				writeBinaryCompareOperator(command);
			}
			output.write("@SP");
			output.newLine();
			output.write("M=M+1");
			output.newLine();
		}
	}

	void writePushPop(CommandType commandType, String segment, int index) throws IOException {
		if (commandType.equals(CommandType.C_PUSH))
		{
			// TODO writePushPop。 pointer セグメントに対応する
			// TODO writePushPop。 static セグメントに対応する
			switch (segment) {
			case "argument":
			case "local":
			case "this":
			case "that":
				output.write("@" + index);
				output.newLine();
				output.write("D=A");
				output.newLine();
				output.write("@" + getRamSymbolStr(segment));
				output.newLine();
				output.write("A=D+M");
				output.newLine();
				output.write("D=M");
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
				break;
			case "constant":
				output.write("@" + index);
				output.newLine();
				output.write("D=A");
				output.newLine();
				output.write("@" + getRamSymbolStr(segment));
				output.newLine();
				output.write("A=M");
				output.newLine();
				output.write("M=D");
				output.newLine();
				output.write("@" + getRamSymbolStr(segment));
				output.newLine();
				output.write("M=M+1");
				output.newLine();
				break;
			case "temp":
				output.write("@R" + (tempBaseAddress + index));
				output.newLine();
				output.write("D=M");
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
				break;
			default:
				break;
			}
		}
		else if (commandType.equals(CommandType.C_POP))
		{
			if (segment.equals("temp"))
			{
				output.write("@SP");
				output.newLine();
				output.write("M=M-1");
				output.newLine();
				output.write("A=M");
				output.newLine();
				output.write("D=M");
				output.newLine();
				output.write("@R" + (tempBaseAddress + index));
				output.newLine();

				output.write("M=D");
				output.newLine();
			}
			else
			{
				output.write("@" + index);
				output.newLine();
				output.write("D=A");
				output.newLine();
				output.write("@" + getRamSymbolStr(segment));
				output.newLine();
				output.write("M=M+D");
				output.newLine();

				output.write("@SP");
				output.newLine();
				output.write("M=M-1");
				output.newLine();
				output.write("A=M");
				output.newLine();
				output.write("D=M");
				output.newLine();
				output.write("@" + getRamSymbolStr(segment));
				output.newLine();

				output.write("A=M");
				output.newLine();

				output.write("M=D");
				output.newLine();

				output.write("@" + index);
				output.newLine();
				output.write("D=A");
				output.newLine();
				output.write("@" + getRamSymbolStr(segment));
				output.newLine();
				output.write("M=M-D");
				output.newLine();
			}
		}
	}

	void close() {
		// TODO close
	}

	private boolean isUnaryOperator(String command) {
		boolean isUnary = false;

		switch (command) {
		case "neg":
		case "not":
			isUnary = true;
			break;
		default:
			break;
		}

		return isUnary;
	}

	private boolean isBinaryArithmeticOperator(String command) {
		boolean isBinary = false;

		switch (command) {
		case "add":
		case "sub":
		case "and":
		case "or":
			isBinary = true;
			break;
		default:
			break;
		}

		return isBinary;
	}

	private void writeBinaryArithmeticOperator(String command) throws IOException {
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
	}

	private void writeBinaryCompareOperator(String command) throws IOException {
		this.condLabelNum++;

		output.write("D=M-D");
		output.newLine();
		output.write("@COND_SATISFIED" + condLabelNum);
		output.newLine();

		switch (command) {
		case "eq":
			output.write("D;JEQ");
			break;
		case "lt":
			output.write("D;JLT");
			break;
		case "gt":
			output.write("D;JGT");
			break;
		default:
			break;
		}

		output.newLine();
		output.write("@SP");
		output.newLine();
		output.write("A=M");
		output.newLine();
		output.write("M=0");
		output.newLine();
		output.write("@COND_END" + condLabelNum);
		output.newLine();
		output.write("0;JEQ");
		output.newLine();
		output.write("(COND_SATISFIED" + condLabelNum + ")");
		output.newLine();
		output.write("@SP");
		output.newLine();
		output.write("A=M");
		output.newLine();
		output.write("M=-1");
		output.newLine();
		output.write("(COND_END" + condLabelNum + ")");
		output.newLine();
	}

	private String getRamSymbolStr(String segment) {
		String ramSymbolStr = "SP";

		switch (segment) {
		case "argument":
			ramSymbolStr = "ARG";
			break;
		case "local":
			ramSymbolStr = "LCL";
			break;
		case "this":
			ramSymbolStr = "THIS";
			break;
		case "that":
			ramSymbolStr = "THAT";
			break;
		case "constant":
			ramSymbolStr = "SP";
			break;
		default:
			break;
		}

		return ramSymbolStr;
	}
}
