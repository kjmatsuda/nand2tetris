import java.util.HashMap;

public class SymbolTable {
	private HashMap<String,Integer> symbolMap = new HashMap<>();

	SymbolTable() {
		// 定義済シンボルを登録
		symbolMap.put("SP", 0x0000);
		symbolMap.put("LCL", 0x0001);
		symbolMap.put("ARG", 0x0002);
		symbolMap.put("THIS", 0x0003);
		symbolMap.put("THAT", 0x0004);
		symbolMap.put("R0", 0x0000);
		symbolMap.put("R1", 0x0001);
		symbolMap.put("R2", 0x0002);
		symbolMap.put("R3", 0x0003);
		symbolMap.put("R4", 0x0004);
		symbolMap.put("R5", 0x0005);
		symbolMap.put("R6", 0x0006);
		symbolMap.put("R7", 0x0007);
		symbolMap.put("R8", 0x0008);
		symbolMap.put("R9", 0x0009);
		symbolMap.put("R10", 0x000a);
		symbolMap.put("R11", 0x000b);
		symbolMap.put("R12", 0x000c);
		symbolMap.put("R13", 0x000d);
		symbolMap.put("R14", 0x000e);
		symbolMap.put("R15", 0x000f);
		symbolMap.put("SCREEN", 0x4000);
		symbolMap.put("KBD", 0x6000);
	}

	public void addEntry(String symbol, int address) {
		symbolMap.put(symbol, address);
	}

	public boolean contains(String symbol) {
		return symbolMap.containsKey(symbol);
	}

	public int getAddress(String symbol) {
		return symbolMap.get(symbol);
	}
}
