public class Code {
	public String dest(String destMnemonic) {
		String destBinary = "";

		switch (destMnemonic) {
		case "null":
			destBinary = "000";
			break;
		case "M":
			destBinary = "001";
			break;
		case "D":
			destBinary = "010";
			break;
		case "MD":
			destBinary = "011";
			break;
		case "A":
			destBinary = "100";
			break;
		case "AM":
			destBinary = "101";
			break;
		case "AD":
			destBinary = "110";
			break;
		case "AMD":
			destBinary = "111";
			break;
		default:
			break;
		}

		return destBinary;
	}

	public String comp(String compMnemonic) {
		String compBinary = "";

		switch (compMnemonic) {
		//// a=0 のニーモニック
		case "0":
			compBinary = "0101010";
			break;
		case "1":
			compBinary = "0111111";
			break;
		case "-1":
			compBinary = "0111010";
			break;
		case "D":
			compBinary = "0001100";
			break;
		case "A":
			compBinary = "0110000";
			break;
		case "!D":
			compBinary = "0001101";
			break;
		case "!A":
			compBinary = "0110001";
			break;
		case "-D":
			compBinary = "0001111";
			break;
		case "-A":
			compBinary = "0110011";
			break;
		case "D+1":
			compBinary = "0011111";
			break;
		case "A+1":
			compBinary = "0110111";
			break;
		case "D-1":
			compBinary = "0001110";
			break;
		case "A-1":
			compBinary = "0110010";
			break;
		case "D+A":
			compBinary = "0000010";
			break;
		case "D-A":
			compBinary = "0010011";
			break;
		case "A-D":
			compBinary = "0000111";
			break;
		case "D&A":
			compBinary = "0000000";
			break;
		case "D|A":
			compBinary = "0010101";
			break;
		//// a=1 のニーモニック
		case "M":
			compBinary = "1110000";
			break;
		case "!M":
			compBinary = "1110001";
			break;
		case "-M":
			compBinary = "1110011";
			break;
		case "M+1":
			compBinary = "1110111";
			break;
		case "M-1":
			compBinary = "1110010";
			break;
		case "D+M":
			compBinary = "1000010";
			break;
		case "D-M":
			compBinary = "1010011";
			break;
		case "M-D":
			compBinary = "1000111";
			break;
		case "D&M":
			compBinary = "1000000";
			break;
		case "D|M":
			compBinary = "1010101";
			break;
		default:
			break;
		}

		return compBinary;
	}

	public String jump(String jumpMnemonic) {
		String jumpBinary = "";

		switch (jumpMnemonic) {
		case "null":
			jumpBinary = "000";
			break;
		case "JGT":
			jumpBinary = "001";
			break;
		case "JEQ":
			jumpBinary = "010";
			break;
		case "JGE":
			jumpBinary = "011";
			break;
		case "JLT":
			jumpBinary = "100";
			break;
		case "JNE":
			jumpBinary = "101";
			break;
		case "JLE":
			jumpBinary = "110";
			break;
		case "JMP":
			jumpBinary = "111";
			break;
		default:
			break;
		}

		return jumpBinary;
	}
}
