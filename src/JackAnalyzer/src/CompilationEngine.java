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
		while (isSubroutineDec())
		{
			compileSubroutine();
		}

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

		// KEYWORD の'static', 'field'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// KEYWORD の type
		tokenizer.advance();
		if (!isType())
		{
			// 構文エラー
			return;
		}
		writeLineType();

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
		writeLine(output, "<subroutineDec>");
		indentLevelDown();

		// KEYWORD の'constructor', 'function', 'method'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// KEYWORD の type
		tokenizer.advance();
		if (!isType())
		{
			// 構文エラー
			return;
		}
		writeLineType();

		// IDENTIFIER の subroutineName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// SYMBOL の '('
		tokenizer.advance();
		if (!isOpenBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		tokenizer.advance();
		compileParameterList();

		// SYMBOL の ')'
		if (!isCloseBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		tokenizer.advance();
		compileSubroutineBody();

		indentLevelUp();
		writeLine(output, "</subroutineDec>");
	}

	public void compileParameterList() throws IOException{
		writeLine(output, "<parameterList>");
		indentLevelDown();

		while (!isCloseBracket())
		{
			// type
			if (!isType())
			{
				// 構文エラー
				return;
			}
			writeLineType();

			// IDENTIFIER の varName
			tokenizer.advance();
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				return;
			}
			writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

			tokenizer.advance();
			if (isComma())
			{
				writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");
				tokenizer.advance();
			}
		}
		indentLevelUp();
		writeLine(output, "</parameterList>");
	}

	public void compileSubroutineBody() throws IOException{
		writeLine(output, "<subroutineBody>");
		indentLevelDown();

		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		// varDec*
		tokenizer.advance();
		while (isVarDec())
		{
			compileVarDec();
			tokenizer.advance();
		}

		// statements
		compileStatements();

		// SYMBOL の '}'
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		indentLevelUp();
		writeLine(output, "</subroutineBody>");
	}

	public void compileVarDec() throws IOException{
		writeLine(output, "<varDec>");
		indentLevelDown();

		// KEYWORD の'var'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// KEYWORD の type
		tokenizer.advance();
		if (!isType())
		{
			// 構文エラー
			return;
		}
		writeLineType();

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
		writeLine(output, "</varDec>");
	}

	public void compileStatements() throws IOException{
		writeLine(output, "<statements>");
		indentLevelDown();

		while (isStatement())
		{
			// statement*
			if (isLetStatement())
			{
				compileLet();
			}
			else if (isIfStatement())
			{
				compileIf();
			}
			else if (isWhileStatement())
			{
				compileWhile();
			}
			else if (isDoStatement())
			{
				compileDo();
			}
			else if (isReturnStatement())
			{
				compileReturn();
			}
			tokenizer.advance();
		}

		indentLevelUp();
		writeLine(output, "</statements>");
	}

	public void compileDo() throws IOException{
		writeLine(output, "<doStatement>");
		indentLevelDown();

		// KEYWORD の'do'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// TODO subroutineCall

		// ';'
		if (!isSemicolon())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		indentLevelUp();
		writeLine(output, "</doStatement>");
	}

	public void compileLet() throws IOException{
		writeLine(output, "<letStatement>");
		indentLevelDown();

		// KEYWORD の'let'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// varName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// ('[' expression ']')?
		if (isOpenSquareBracket())
		{
			writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");
			tokenizer.advance();

			compileExpression();

			if (!isCloseSquareBracket())
			{
				// 構文エラー
				return;
			}
			writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");
			tokenizer.advance();
		}

		// '='
		tokenizer.advance();
		if (!isEqual())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		// expression
		compileExpression();

		// ';'
		if (!isSemicolon())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		indentLevelUp();
		writeLine(output, "</letStatement>");
	}

	public void compileWhile() throws IOException{
		writeLine(output, "<whileStatement>");
		indentLevelDown();

		// KEYWORD の'while'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// '('
		tokenizer.advance();
		if (!isOpenBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		// expression
		compileExpression();

		// ')'
		tokenizer.advance();
		if (!isCloseBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		// '{'
		tokenizer.advance();
		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		compileStatements();

		// '}'
		tokenizer.advance();
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		indentLevelUp();
		writeLine(output, "</whileStatement>");
	}

	public void compileReturn() throws IOException{
		writeLine(output, "<returnStatement>");
		indentLevelDown();

		// KEYWORD の'return'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// expression?
		compileExpression();

		// ';'
		if (!isSemicolon())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		indentLevelUp();
		writeLine(output, "</returnStatement>");
	}

	public void compileIf() throws IOException{
		writeLine(output, "<ifStatement>");
		indentLevelDown();

		// KEYWORD の'if'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// '('
		tokenizer.advance();
		if (!isOpenBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		// expression
		compileExpression();

		// ')'
		tokenizer.advance();
		if (!isCloseBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		// '{'
		tokenizer.advance();
		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		compileStatements();

		// '}'
		tokenizer.advance();
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			return;
		}
		writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

		// ('else' '{' statements '}')?
		tokenizer.advance();
		if (isElse())
		{
			tokenizer.advance();
			if (!isOpenCurlyBracket())
			{
				// 構文エラー
				return;
			}
			writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");

			compileStatements();

			tokenizer.advance();
			if (!isCloseCurlyBracket())
			{
				// 構文エラー
				return;
			}
			writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");
		}

		indentLevelUp();
		writeLine(output, "</ifStatement>");
	}

	public void compileExpression() throws IOException{
		writeLine(output, "<expression>");
		indentLevelDown();

		// term
		compileTerm();

		// (op term)*
		while (isOperator())
		{
			writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");
			compileTerm();
			tokenizer.advance();
		}

		indentLevelUp();
		writeLine(output, "</expression>");
	}

	public void compileTerm(){
		// TODO compileTerm

	}

	public void compileExpressionList() throws IOException{
		writeLine(output, "<expressionList>");
		indentLevelDown();

		while (!isCloseBracket())
		{
			compileExpression();

			tokenizer.advance();
			if (isComma())
			{
				writeLine(output, "<symbol> " + tokenizer.symbol() + " </symbol>");
				tokenizer.advance();
			}
		}

		indentLevelUp();
		writeLine(output, "</expressionList>");
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

	private void writeLineType() throws IOException {
		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");
		}
		else
		{
			writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");
		}
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

	private boolean isOpenSquareBracket() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case '[':
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isCloseSquareBracket() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case ']':
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isOpenBracket() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case '(':
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isCloseBracket() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case ')':
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

	private boolean isEqual() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case '=':
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

	private boolean isVarDec() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_VAR:
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

	private boolean isStatement() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_LET:
			case KEYWORD_IF:
			case KEYWORD_WHILE:
			case KEYWORD_DO:
			case KEYWORD_RETURN:
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isLetStatement() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_LET:
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}
	private boolean isIfStatement() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_IF:
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}
	private boolean isWhileStatement() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_WHILE:
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}
	private boolean isDoStatement() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_DO:
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}
	private boolean isReturnStatement() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_RETURN:
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isOperator() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case '+':
			case '-':
			case '*':
			case '/':
			case '&':
			case '|':
			case '<':
			case '>':
			case '=':
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isElse() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_ELSE:
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}
}
