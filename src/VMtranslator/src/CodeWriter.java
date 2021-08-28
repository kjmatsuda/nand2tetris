import java.io.BufferedWriter;
import java.io.IOException;

public class CodeWriter {
	private BufferedWriter output;
	private String fileName;
	private int condLabelNum = 0;
	private int frameNum = 0;
	private int retNum = 0;
	private final int tempBaseAddress = 5;
	private final int pointerBaseAddress = 3;


	CodeWriter(BufferedWriter output) {
		this.output = output;
	}

	void setFileName(String fileName) {
		this.fileName = fileName;
	}

	void writeArithmetic(String command) throws IOException {
		if (isUnaryOperator(command))
		{
			// 単項演算子の場合
			writeLine("@SP");
			writeLine("M=M-1");
			writeLine("A=M");
			switch (command) {
			case "neg":
				writeLine("M=-M");
				break;
			case "not":
				writeLine("M=!M");
				break;
			}
			writeLine("@SP");
			writeLine("M=M+1");
		}
		else
		{
			// 二項演算子の場合
			writeLine("@SP");
			writeLine("M=M-1");
			writeLine("A=M");
			writeLine("D=M");
			writeLine("@SP");
			writeLine("M=M-1");
			writeLine("A=M");

			if (isBinaryArithmeticOperator(command))
			{
				writeBinaryArithmeticOperator(command);
			}
			else
			{
				writeBinaryCompareOperator(command);
			}
			writeLine("@SP");
			writeLine("M=M+1");
		}
	}

	void writePushPop(CommandType commandType, String segment, int index) throws IOException {
		if (commandType.equals(CommandType.C_PUSH))
		{
			switch (segment) {
			case "argument":
			case "local":
			case "this":
			case "that":
				writeLine("@" + index);
				writeLine("D=A");
				writeLine("@" + getRamSymbolStr(segment, index));
				writeLine("A=D+M");
				writeLine("D=M");
				writeLine("@SP");
				writeLine("A=M");
				writeLine("M=D");
				writeLine("@SP");
				writeLine("M=M+1");
				break;
			case "constant":
				writeLine("@" + index);
				writeLine("D=A");
				writeLine("@" + getRamSymbolStr(segment, index));
				writeLine("A=M");
				writeLine("M=D");
				writeLine("@" + getRamSymbolStr(segment, index));
				writeLine("M=M+1");
				break;
			case "temp":
			case "pointer":
			case "static":
				writeLine("@" + getRamSymbolStr(segment, index));
				writeLine("D=M");
				writeLine("@SP");
				writeLine("A=M");
				writeLine("M=D");
				writeLine("@SP");
				writeLine("M=M+1");
				break;
			default:
				break;
			}
		}
		else if (commandType.equals(CommandType.C_POP))
		{
			if ((segment.equals("temp")) ||
				(segment.equals("pointer")) ||
				(segment.equals("static")))
			{
				writeLine("@SP");
				writeLine("M=M-1");
				writeLine("A=M");
				writeLine("D=M");
				writeLine("@" + getRamSymbolStr(segment, index));

				writeLine("M=D");
			}
			else
			{
				writeLine("@" + index);
				writeLine("D=A");
				writeLine("@" + getRamSymbolStr(segment, index));
				writeLine("M=M+D");

				writeLine("@SP");
				writeLine("M=M-1");
				writeLine("A=M");
				writeLine("D=M");
				writeLine("@" + getRamSymbolStr(segment, index));

				writeLine("A=M");

				writeLine("M=D");

				writeLine("@" + index);
				writeLine("D=A");
				writeLine("@" + getRamSymbolStr(segment, index));
				writeLine("M=M-D");
			}
		}
	}

	void writeInit() throws IOException {
		writeLine("@256");
		writeLine("D=A");
		writeLine("@SP");
		writeLine("M=D");
		writeCall("Sys.init", 0);
	}

	void writeLabel(String label) throws IOException {
		writeLine("(" + label +")");
	}

	void writeGoto(String label) throws IOException {
		writeLine("@" + label);
		writeLine("0;JMP");
	}

	void writeIf(String label) throws IOException {
		writeLine("@SP");
		writeLine("M=M-1");
		writeLine("A=M");
		writeLine("D=M");
		writeLine("@" + label);
		writeLine("D;JNE");
	}

	void writeCall(String functionName, int numArgs) throws IOException {
		String returnAddress = functionName + "$" + "return-address";

		// push return-address
		writeLine("@" + returnAddress);
		writeLine("D=A");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		writeLine("@SP");
		writeLine("M=M+1");
		// push LCL
		writeLine("@LCL");
		writeLine("D=M");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		writeLine("@SP");
		writeLine("M=M+1");
		// push ARG
		writeLine("@ARG");
		writeLine("D=M");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		writeLine("@SP");
		writeLine("M=M+1");
		// push THIS
		writeLine("@THIS");
		writeLine("D=M");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		writeLine("@SP");
		writeLine("M=M+1");
		// push THAT
		writeLine("@THAT");
		writeLine("D=M");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		writeLine("@SP");
		writeLine("M=M+1");
		// ARG = SP - n - 5
		writeLine("@0");
		writeLine("D=A");
		writeLine("@SP");
		writeLine("M=M-D");
		writeLine("@5");
		writeLine("D=A");
		writeLine("@SP");
		writeLine("M=M-D");
		writeLine("D=M");
		writeLine("@ARG");
		writeLine("M=D");
		writeLine("@0");
		writeLine("D=A");
		writeLine("@SP");
		writeLine("M=M+D");
		writeLine("@5");
		writeLine("D=A");
		writeLine("@SP");
		writeLine("M=M+D");
		// LCL = SP
		writeLine("@SP");
		writeLine("D=M");
		writeLine("@LCL");
		writeLine("M=D");
		// goto Function
		writeGoto(functionName);
		// (return-address)
		writeLabel(returnAddress);
	}

	void writeReturn() throws IOException {
		this.frameNum++;
		this.retNum++;

		String frame = "frame" + this.frameNum;
		String ret = "RET" + this.retNum;

		// FRAME = LCL
		writeLine("@LCL");
		writeLine("D=M");
		writeLine("@" + frame);
		writeLine("M=D");

		// RET = *(FRAME - 5)
		writeLine("@5");
		writeLine("D=A");
		writeLine("@" + frame);
		writeLine("M=M-D");
		writeLine("A=M");
		writeLine("D=M");
		writeLine("@" + ret);
		writeLine("M=D");
		writeLine("@5");
		writeLine("D=A");
		writeLine("@" + frame);
		writeLine("M=M+D");

		// *ARG = pop()
		writePushPop(CommandType.C_POP, "argument", 0);

		// SP = ARG + 1
		writeLine("@ARG");
		writeLine("D=M+1");
		writeLine("@SP");
		writeLine("M=D");

		// THAT = *(FRAME - 1)
		writeRestoreCallerEnv("that", 1);

		// THIS = *(FRAME - 2)
		writeRestoreCallerEnv("this", 2);

		// ARG = *(FRAME - 3)
		writeRestoreCallerEnv("argument", 3);

		// LCL = *(FRAME - 4)
		writeRestoreCallerEnv("local", 4);

		// goto RET
		writeLine("@" + ret);
		writeLine("A=M");
		writeLine("0;JMP");
	}

	void writeFunction(String functionName, int numLocals) throws IOException {
		writeLine("(" + functionName +")");

		for (int ii = 0; ii < numLocals; ii++)
		{
			writePushPop(CommandType.C_PUSH, "constant", 0);
		}
	}

	void close() throws IOException {
		output.close();
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
			writeLine("M=D+M");
			break;
		case "sub":
			writeLine("M=M-D");
			break;
		case "and":
			writeLine("M=M&D");
			break;
		case "or":
			writeLine("M=M|D");
			break;
		default:
			break;
		}
	}

	private void writeBinaryCompareOperator(String command) throws IOException {
		this.condLabelNum++;

		writeLine("D=M-D");
		writeLine("@COND_SATISFIED" + condLabelNum);

		switch (command) {
		case "eq":
			writeLine("D;JEQ");
			break;
		case "lt":
			writeLine("D;JLT");
			break;
		case "gt":
			writeLine("D;JGT");
			break;
		default:
			break;
		}

		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=0");
		writeLine("@COND_END" + condLabelNum);
		writeLine("0;JEQ");
		writeLine("(COND_SATISFIED" + condLabelNum + ")");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=-1");
		writeLine("(COND_END" + condLabelNum + ")");
	}

	private void writeRestoreCallerEnv(String segment, int offset) throws IOException {
		String frame = "frame" + this.frameNum;

		writeLine("@" + offset);
		writeLine("D=A");
		writeLine("@" + frame);
		writeLine("M=M-D");
		writeLine("A=M");
		writeLine("D=M");
		writeLine("@" + getRamSymbolStr(segment, 0));
		writeLine("M=D");
		writeLine("@" + offset);
		writeLine("D=A");
		writeLine("@" + frame);
		writeLine("M=M+D");
	}

	private String getRamSymbolStr(String segment, int index) {
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
		case "temp":
			ramSymbolStr = "R" + (tempBaseAddress + index);
			break;
		case "pointer":
			ramSymbolStr = "R" + (pointerBaseAddress + index);
			break;
		case "static":
			ramSymbolStr = fileName + "." + index;
			break;
		default:
			break;
		}

		return ramSymbolStr;
	}

	private void writeLine(String str) throws IOException {
		output.write(str);
		output.newLine();
	}
}
