import java.util.HashMap;

public class SymbolTable {
	private HashMap<String, SymbolProperty> classScopeHash;
	private HashMap<String, SymbolProperty> subroutineScopeHash;

	SymbolTable() {
		classScopeHash = new HashMap<>();
		subroutineScopeHash = new HashMap<>();
	}

	public void define(String name, String type, SymbolKind kind) {
		SymbolProperty prop = new SymbolProperty(type, kind, this.varCount(kind));

		switch (kind) {
		case KIND_STATIC:
		case KIND_FIELD:
			classScopeHash.put(name, prop);
			break;
		case KIND_ARG:
		case KIND_VAR:
			subroutineScopeHash.put(name, prop);
			break;
		default:
			break;
		}
	}

	public int varCount(SymbolKind kind) {
		int retCount = 0;

		HashMap<String, SymbolProperty> hash = null;

		switch (kind) {
		case KIND_STATIC:
		case KIND_FIELD:
			hash = this.classScopeHash;
			break;
		case KIND_ARG:
		case KIND_VAR:
			hash = this.subroutineScopeHash;
			break;
		default:
			break;
		}

		if (hash != null)
		{
			for (SymbolProperty prop : hash.values())
			{
				if (prop.getKind().equals(kind))
				{
					retCount++;
				}
			}
		}

		return retCount;
	}

	public SymbolKind kindOf(String name) {
		SymbolKind retKind = SymbolKind.KIND_NONE;

		SymbolProperty prop = getPropFromCurrentScope(name);

		if (prop != null)
		{
			retKind = prop.getKind();
		}

		return retKind;
	}

	public String typeOf(String name) {
		String retType = "";

		SymbolProperty prop = getPropFromCurrentScope(name);

		if (prop != null)
		{
			retType = prop.getType();
		}

		return retType;
	}

	public int indexOf(String name) {
		int retIdx = 0;

		SymbolProperty prop = getPropFromCurrentScope(name);

		if (prop != null)
		{
			retIdx = prop.getIndex();
		}

		return retIdx;
	}

	private SymbolProperty getPropFromCurrentScope(String name) {
		SymbolProperty prop = this.subroutineScopeHash.get(name);

		if (null == prop)
		{
			prop = this.classScopeHash.get(name);
		}

		return prop;
	}
}
