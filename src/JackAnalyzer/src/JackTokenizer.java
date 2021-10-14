import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {
	private BufferedReader input;
	private String currentLine;
	private int tokenStartIdx;
	private int tokenEndIdx;
	private String currentToken;

	JackTokenizer(BufferedReader input) {
		this.input = input;
		this.currentLine = "";
		this.tokenStartIdx = -1;
		this.tokenEndIdx = -1;
	}

	public void setReader(BufferedReader input) {
		this.input = input;
	}

	public boolean hasMoreTokens() throws IOException {
		boolean hasMore = false;
		input.mark(256);
		hasMore = (input.readLine() != null);
		input.reset();
		return hasMore;
	}

	public void advance() throws IOException {
		if (currentLine.isEmpty() || (tokenEndIdx > currentLine.length()))
		{
			// 次の行に進む
			currentLine = input.readLine();
			while (skipLine(currentLine))
			{
				currentLine = input.readLine();
			}
			// コメントは削除
			currentLine = currentLine.replaceAll("//.*", "");
			tokenStartIdx = 0;
			tokenEndIdx = 1;
		}
		//// 開始インデックスを決める
		// スペースかタブの場合はスキップする
		boolean isInCommentBlock = false;
		if (isCommentBlockStart(currentLine, tokenStartIdx))
		{
			isInCommentBlock = true;
		}
		while (isSpaceOrTab(currentLine, tokenStartIdx) || isInCommentBlock)
		{
			tokenStartIdx++;

			if (isCommentBlockEnd(currentLine, tokenStartIdx))
			{
				isInCommentBlock = false;
				// */ をスキップする
				tokenStartIdx = tokenStartIdx + 2;
			}
		}
		tokenEndIdx = tokenStartIdx + 1;
		// TODO このブロックは advance の冒頭と重複するので修正
		if (currentLine.isEmpty() || (tokenEndIdx > currentLine.length()))
		{
			// 次の行に進む
			currentLine = input.readLine();
			while (skipLine(currentLine))
			{
				currentLine = input.readLine();
			}
			// コメントは削除
			currentLine = currentLine.replaceAll("//.*", "");
			tokenStartIdx = 0;
			tokenEndIdx = 1;
		}

		if (isSymbol(currentLine.substring(tokenStartIdx, tokenEndIdx)))
		{
			currentToken = currentLine.substring(tokenStartIdx, tokenEndIdx);
			tokenStartIdx++;
			tokenEndIdx++;
			return;
		}

		boolean isStringConstant = isDoubleQuotation(currentLine.substring(tokenStartIdx, tokenEndIdx));

		//// 終了インデックスを決める
		boolean meetsContinueCondition = true;
		while (meetsContinueCondition)
		{
			if (isStringConstant)
			{
				meetsContinueCondition = !isDoubleQuotation(currentLine.substring(tokenEndIdx, tokenEndIdx + 1));
			}
			else
			{
				meetsContinueCondition = !isSpaceOrTab(currentLine, tokenEndIdx) && !isSymbol(currentLine.substring(tokenEndIdx, tokenEndIdx + 1));
			}
			if (meetsContinueCondition || isStringConstant)
			{
				// 文字列のときは閉じるダブルクォーテーションをスキップするためにインデックスを進める
				tokenEndIdx++;
			}
		}
		if (isStringConstant)
		{
			// 最初と最後のダブルクォーテーションを飛ばす
			currentToken = currentLine.substring(tokenStartIdx + 1, tokenEndIdx - 1);
		}
		else
		{
			currentToken = currentLine.substring(tokenStartIdx, tokenEndIdx);
		}
		tokenStartIdx = tokenEndIdx;
		tokenEndIdx = tokenStartIdx + 1;
	}

	public TokenType tokenType() {
		TokenType type = TokenType.TOKEN_KEYWORD;

		if (isKeyword(currentToken))
		{
			type = TokenType.TOKEN_KEYWORD;
		}
		else if (isSymbol(currentToken))
		{
			type = TokenType.TOKEN_SYMBOL;
		}
		else if (isIdentifier(currentToken))
		{
			type = TokenType.TOKEN_IDENTIFIER;
		}
		else if (isIntConst(currentToken))
		{
			type = TokenType.TOKEN_INT_CONST;
		}
		else if (isStringConst(currentToken))
		{
			type = TokenType.TOKEN_STRING_CONST;
		}

		return type;
	}

	public String keyWord() {
//		KeyWord keyword = KeyWord.KEYWORD_CLASS;
//		switch (currentToken) {
//		case "class":
//			keyword = KeyWord.KEYWORD_CLASS;
//			break;
//		case "constructor":
//			keyword = KeyWord.KEYWORD_CONSTRUCTOR;
//			break;
//		case "function":
//			keyword = KeyWord.KEYWORD_FUNCTION;
//			break;
//		case "method":
//			keyword = KeyWord.KEYWORD_METHOD;
//			break;
//		case "field":
//			keyword = KeyWord.KEYWORD_FIELD;
//			break;
//		case "static":
//			keyword = KeyWord.KEYWORD_STATIC;
//			break;
//		case "var":
//			keyword = KeyWord.KEYWORD_VAR;
//			break;
//		case "int":
//			keyword = KeyWord.KEYWORD_INT;
//			break;
//		case "char":
//			keyword = KeyWord.KEYWORD_CHAR;
//			break;
//		case "boolean":
//			keyword = KeyWord.KEYWORD_BOOLEAN;
//			break;
//		case "void":
//			keyword = KeyWord.KEYWORD_VOID;
//			break;
//		case "true":
//			keyword = KeyWord.KEYWORD_TRUE;
//			break;
//		case "false":
//			keyword = KeyWord.KEYWORD_FALSE;
//			break;
//		case "null":
//			keyword = KeyWord.KEYWORD_NULL;
//			break;
//		case "this":
//			keyword = KeyWord.KEYWORD_THIS;
//			break;
//		case "let":
//			keyword = KeyWord.KEYWORD_LET;
//			break;
//		case "do":
//			keyword = KeyWord.KEYWORD_DO;
//			break;
//		case "if":
//			keyword = KeyWord.KEYWORD_IF;
//			break;
//		case "else":
//			keyword = KeyWord.KEYWORD_ELSE;
//			break;
//		case "while":
//			keyword = KeyWord.KEYWORD_WHILE;
//			break;
//		case "return":
//			keyword = KeyWord.KEYWORD_RETURN;
//			break;
//		default:
//			break;
//		}

		return currentToken;
	}

	public char symbol() {
		return currentToken.charAt(0);
	}

	public String identifier() {
		return currentToken;
	}

	public int intVal() {
		int retInt = 0;

		try {
			retInt = Integer.parseInt(currentToken);
		} catch (Exception e)
		{
			retInt = 0;
		}

		return retInt;
	}

	public String stringVal() {
		return currentToken;
	}

	private boolean skipLine(String line) {
		boolean skip = false;
		if (line.isEmpty() || line.startsWith("//")) {
			skip = true;
		}

		String regexExceptSpace = "\\S";		// 空白以外
		Pattern pattern = Pattern.compile(regexExceptSpace);
		Matcher matcher = pattern.matcher(line);
		if (!matcher.find())
		{
			// 空白しか含まない場合
			skip = true;
		}

		return skip;
	}

	private boolean isKeyword(String token) {
		boolean is = false;

		switch (token) {
		case "class":
		case "constructor":
		case "function":
		case "method":
		case "field":
		case "static":
		case "var":
		case "int":
		case "char":
		case "boolean":
		case "void":
		case "true":
		case "false":
		case "null":
		case "this":
		case "let":
		case "do":
		case "if":
		case "else":
		case "while":
		case "return":
			is = true;
			break;
		default:
			break;
		}

		return is;
	}

	private boolean isSymbol(String token) {
		boolean is = false;

		switch (token) {
		case "{":
		case "}":
		case "(":
		case ")":
		case "[":
		case "]":
		case ".":
		case ",":
		case ";":
		case "+":
		case "-":
		case "*":
		case "/":
		case "&":
		case "|":
		case "<":
		case ">":
		case "=":
		case "~":
			is = true;
			break;
		default:
			break;
		}

		return is;
	}

	private boolean isIdentifier(String token) {
		boolean is = false;

		// アルファベット、数字、アンダースコアの文字列。ただし数字から始まる文字列は除く
		String regex = "^[A-Za-z_]+[0-9]*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(token);

		if (matcher.find())
		{
			is = true;
		}

		return is;
	}

	private boolean isIntConst(String token) {
		boolean is = false;

		// 0から32767までの10進数の数字
		int number = 0;
		try {
			number = Integer.parseInt(currentToken);
		} catch (Exception e)
		{
			number = -1;
		}

		if (number >= 0 && number <= 32767)
		{
			is = true;
		}

		return is;
	}

	private boolean isStringConst(String token) {
		boolean is = true;

		// ダブルクォートと改行文字を含まないユニコードの文字列
		if (token.indexOf('\n') >= 0)
		{
			is = false;
		}
		else if (token.indexOf('"') >= 0)
		{
			is = false;
		}

		return is;
	}

	private boolean isSpaceOrTab(String line, int startIdx) {
		boolean is = false;

		int endIdx = startIdx + 1;

		if (endIdx <= line.length())
		{
			switch (line.substring(startIdx, endIdx)) {
			case " ":
			case "\t":
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isDoubleQuotation(String token) {
		boolean is = false;

		switch (token) {
		case "\"":
			is = true;
			break;
		default:
			break;
		}

		return is;
	}

	private boolean isCommentBlockStart(String line, int startIdx) {
		boolean is = false;

		int endIdx = startIdx + 3;

		if (endIdx <= line.length())
		{
			switch (line.substring(startIdx, endIdx)) {
			case "/**":
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}

	private boolean isCommentBlockEnd(String line, int startIdx) {
		boolean is = false;

		int endIdx = startIdx + 2;

		if (endIdx <= line.length())
		{
			switch (line.substring(startIdx, endIdx)) {
			case "*/":
				is = true;
				break;
			default:
				break;
			}
		}

		return is;
	}
}
