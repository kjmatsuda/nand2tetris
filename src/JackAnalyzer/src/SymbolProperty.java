public class SymbolProperty {

	private String type;
	private SymbolKind kind;
	private int index;

	SymbolProperty(String type, SymbolKind kind, int index) {
		this.setType(type);
		this.setKind(kind);
		this.setIndex(index);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public SymbolKind getKind() {
		return kind;
	}

	public void setKind(SymbolKind kind) {
		this.kind = kind;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
