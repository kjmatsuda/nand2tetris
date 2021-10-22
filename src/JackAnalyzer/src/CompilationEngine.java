import java.io.BufferedReader;
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

		// TODO SYMBOL の '{'

		// TODO classVarDec*

		// TODO subroutineDec*

		// TODO SYMBOL の '}'
		indentLevelUp();
		writeLine(output, "</class>");
	}

	public void compileClassVarDec(){
		// TODO compileClassVarDec
	}

	public void compileSubroutine(){
		// TODO compileSubroutine
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

}
