public class JackAnalyzer {

	public static void main(String[] args) {

		if (args.length != 1)
		{
			System.out.println("usage: java JackAnalyzer [dirname or filename]");
			return;
		}

		JackCompiler compiler = new JackCompiler();
		compiler.compile(args[0]);
	}
}