import java.io.BufferedWriter;
import java.io.IOException;

public class CompilationEngine {
	private JackTokenizer tokenizer;
	private BufferedWriter output;
	private int indentLevel = 0;

	CompilationEngine(JackTokenizer tokenizer, BufferedWriter output) {
		this.tokenizer = tokenizer;
		this.output = output;
	}

	public void compileClass() throws IOException {
		writeLine(output, "<class>");
		indentLevelDown();

		tokenizer.advance();

		// KEYWORD の class
		if (!(tokenizer.tokenType() == TokenType.TOKEN_KEYWORD && tokenizer.keyWord() == KeyWord.KEYWORD_CLASS))
		{
			// 構文エラー
			return;
		}

		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// IDENTIFIER の className
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// SYMBOL の '{'
		tokenizer.advance();
		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		// classVarDec*
		tokenizer.advance();
		while (isClassVarDec())
		{
			compileClassVarDec();
			tokenizer.advance();
		}

		// subroutineDec*
//		while (isSubroutineDec())
//		{
//			compileSubroutine();
//		}

		// SYMBOL の '}'
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		indentLevelUp();
		writeLine(output, "</class>");
	}

	public void compileClassVarDec() throws IOException{
		writeLine(output, "<classVarDec>");
		indentLevelDown();
		// KEYWORD の'static' もしくは 'field'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// KEYWORD の type
		tokenizer.advance();
		if (!isType())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// IDENTIFIER の varName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// (',' varName)*
		tokenizer.advance();
		while (isComma())
		{
			writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");
			// varName
			tokenizer.advance();
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				return;
			}
			writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");
			tokenizer.advance();
		}

		// SYMBOL の ';'
		if (!isSemicolon())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		indentLevelUp();
		writeLine(output, "</classVarDec>");
	}

	public void compileSubroutine() throws IOException{
		// TODO compileSubroutine
		writeLine(output, "<subroutineDec>");
		indentLevelDown();
		writeLine(output, "</subroutineDec>");
		indentLevelUp();
	}

	public void compileParameterList(){
		// TODO compileParameterList
	}

	public void compileVarDec(){
		// TODO compileVarDec
	}

	public void compileStatements(){
		// TODO compileStatements
	}

	public void compileDo(){
		// TODO compileDo
	}

	public void compileLet(){
		// TODO compileLet
	}

	public void compileWhile(){
		// TODO compileWhile
	}

	public void compileReturn(){
		// TODO compileReturn
	}

	public void compileIf(){
		// TODO compileIf
	}

	public void compileExpression(){
		// TODO compileExpression
	}

	public void compileTerm(){
		// TODO compileTerm
	}

	public void compileExpressionList(){
		// TODO compileExpressionList
	}

	private String keyWordToString(KeyWord keyword) {
		String keywordStr = "unknown";
		switch (keyword) {
		case KEYWORD_CLASS:
			keywordStr = "class";
			break;
		case KEYWORD_CONSTRUCTOR:
			keywordStr = "constructor";
			break;
		case KEYWORD_FUNCTION:
			keywordStr = "function";
			break;
		case KEYWORD_METHOD:
			keywordStr = "method";
			break;
		case KEYWORD_FIELD:
			keywordStr = "field";
			break;
		case KEYWORD_STATIC:
			keywordStr = "static";
			break;
		case KEYWORD_VAR:
			keywordStr = "var";
			break;
		case KEYWORD_INT:
			keywordStr = "int";
			break;
		case KEYWORD_CHAR:
			keywordStr = "char";
			break;
		case KEYWORD_BOOLEAN:
			keywordStr = "boolean";
			break;
		case KEYWORD_VOID:
			keywordStr = "void";
			break;
		case KEYWORD_TRUE:
			keywordStr = "true";
			break;
		case KEYWORD_FALSE:
			keywordStr = "false";
			break;
		case KEYWORD_NULL:
			keywordStr = "null";
			break;
		case KEYWORD_THIS:
			keywordStr = "this";
			break;
		case KEYWORD_LET:
			keywordStr = "let";
			break;
		case KEYWORD_DO:
			keywordStr = "do";
			break;
		case KEYWORD_IF:
			keywordStr = "if";
			break;
		case KEYWORD_ELSE:
			keywordStr = "else";
			break;
		case KEYWORD_WHILE:
			keywordStr = "while";
			break;
		case KEYWORD_RETURN:
			keywordStr = "return";
			break;
		default:
			break;
		}

		return keywordStr;
	}

	private void indentLevelDown() {
		indentLevel += 2;
	}

	private void indentLevelUp() {
		indentLevel -= 2;
	}

	private void writeLine(BufferedWriter output, String str) throws IOException {
		String indentStr = "";
		if (indentLevel > 0)
		{
			indentStr = String.format("%" + indentLevel + "s", "");
		}
		output.write(indentStr + str);
		output.newLine();
	}

	private String convertSymbolToXmlElement(char symbol) {
		String symbolStr = String.valueOf(symbol);

		switch (symbolStr) {
		case "<":
			symbolStr = "&lt;";
			break;
		case ">":
			symbolStr = "&gt;";
			break;
		case "&":
			symbolStr = "&amp;";
			break;
		default:
			break;
		}

		return symbolStr;
	}

	private boolean isOpenCurlyBracket() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case '{':
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isCloseCurlyBracket() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case '}':
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isSemicolon() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case ';':
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isComma() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case ',':
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isClassVarDec() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_STATIC:
			case KEYWORD_FIELD:
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isSubroutineDec() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_CONSTRUCTOR:
			case KEYWORD_FUNCTION:
			case KEYWORD_METHOD:
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isType() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_INT:
			case KEYWORD_CHAR:
			case KEYWORD_BOOLEAN:
			case KEYWORD_VOID:
				is = true;
				break;
			default:
				break;
			}
		}
		else if (tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER)
		{
			is = true;
		}

		return is;
	}
}
