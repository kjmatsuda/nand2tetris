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
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: class, actual: " + tokenizer.stringVal());
			return;
		}

		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// IDENTIFIER の className
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: className, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// SYMBOL の '{'
		tokenizer.advance();
		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
			tokenizer.advance();
		}

		// SYMBOL の '}'
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: type, actual: " + tokenizer.stringVal());
			return;
		}
		writeLineType();

		// IDENTIFIER の varName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// (',' varName)*
		tokenizer.advance();
		while (isComma())
		{
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			// varName
			tokenizer.advance();
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");
			tokenizer.advance();
		}

		// SYMBOL の ';'
		if (!isSemicolon())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: type, actual: " + tokenizer.stringVal());
			return;
		}
		writeLineType();

		// IDENTIFIER の subroutineName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: subroutineName, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// SYMBOL の '('
		tokenizer.advance();
		if (!isOpenBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		tokenizer.advance();
		compileParameterList();

		// SYMBOL の ')'
		if (!isCloseBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: type, actual: " + tokenizer.stringVal());
				return;
			}
			writeLineType();

			// IDENTIFIER の varName
			tokenizer.advance();
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

			tokenizer.advance();
			if (isComma())
			{
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
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
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: type, actual: " + tokenizer.stringVal());
			return;
		}
		writeLineType();

		// IDENTIFIER の varName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// (',' varName)*
		tokenizer.advance();
		while (isComma())
		{
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			// varName
			tokenizer.advance();
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");
			tokenizer.advance();
		}

		// SYMBOL の ';'
		if (!isSemicolon())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
				tokenizer.advance();
			}
			else if (isIfStatement())
			{
				compileIf();
			}
			else if (isWhileStatement())
			{
				compileWhile();
				tokenizer.advance();
			}
			else if (isDoStatement())
			{
				compileDo();
				tokenizer.advance();
			}
			else if (isReturnStatement())
			{
				compileReturn();
				tokenizer.advance();
			}
		}

		indentLevelUp();
		writeLine(output, "</statements>");
	}

	public void compileDo() throws IOException{
		writeLine(output, "<doStatement>");
		indentLevelDown();

		// KEYWORD の'do'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// subroutineCall
		tokenizer.advance();
		compileSubroutineCall();

		// ';'
		tokenizer.setPreloaded(false);
		tokenizer.advance();
		if (!isSemicolon())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// ('[' expression ']')?
		tokenizer.advance();
		if (isOpenSquareBracket())
		{
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

			tokenizer.advance();
			compileExpression();

			tokenizer.advance();
			if (!isCloseSquareBracket())
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ], actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			tokenizer.advance();
		}

		// '='
		if (!isEqual())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: =, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// expression
		tokenizer.advance();
		compileExpression();

		// ';'
		tokenizer.advance();
		if (!isSemicolon())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// expression
		tokenizer.advance();
		compileExpression();

		// ')'
		tokenizer.advance();
		if (!isCloseBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// '{'
		tokenizer.advance();
		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		tokenizer.advance();
		compileStatements();

		// '}'
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		indentLevelUp();
		writeLine(output, "</whileStatement>");
	}

	public void compileReturn() throws IOException{
		writeLine(output, "<returnStatement>");
		indentLevelDown();

		// KEYWORD の'return'(呼び出し元でチェックしているからここではしない)
		writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		tokenizer.advance();
		if (isSemicolon())
		{
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
		}
		else
		{
			// expression?
			compileExpression();

			// ';'
			tokenizer.advance();
			if (!isSemicolon())
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
		}

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
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// expression
		tokenizer.advance();
		compileExpression();

		// ')'
		// TODO Square/Square.jack のif ((y + size) < 254) { を通すにはここのコメントアウトを外す。だがそうすると if (false) { でこける
		tokenizer.advance();
		if (!isCloseBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// '{'
		tokenizer.advance();
		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		tokenizer.advance();
		compileStatements();

		// '}'
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// ('else' '{' statements '}')?
		tokenizer.advance();
		if (isElse())
		{
			writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

			tokenizer.advance();
			if (!isOpenCurlyBracket())
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

			tokenizer.advance();
			compileStatements();

			//tokenizer.advance();
			if (!isCloseCurlyBracket())
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

			tokenizer.advance();
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
		// if ((y + size) < 254) をうまく扱えるように以下の advance を追加した
		tokenizer.advance();
		if (!isOperator())
		{
			tokenizer.setPreloaded(true);
		}
		else
		{
			while (isOperator())
			{
				// while (i < length) のカッコ内をうまく処理できてないので、ここでは先読みを無効化する
				tokenizer.setPreloaded(false);
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();
				compileTerm();
				// ここで advance しないといけないケースあると思うが、コメントアウトすることで ArrayTest/Main.jack のコンパイルが通った
				//tokenizer.advance();
			}
		}

		indentLevelUp();
		writeLine(output, "</expression>");
	}

	public void compileTerm() throws IOException{
		writeLine(output, "<term>");
		indentLevelDown();

		switch (tokenizer.tokenType()) {
		case TOKEN_INT_CONST:
			writeLine(output, "<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
			break;
		case TOKEN_STRING_CONST:
			writeLine(output, "<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
			break;
		case TOKEN_KEYWORD:
			if (!isKeywordConstant())
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: keywordConstant, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");
			break;
		case TOKEN_IDENTIFIER:
			// '(' か '.' なら subroutineCall、そうでなければ varName
			// varName or subroutineCall
			writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

			// ('[' expression ']')?
			tokenizer.advance();
			if (isOpenSquareBracket())
			{
				// varName の後に続く'['だった場合
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

				tokenizer.advance();
				compileExpression();

				tokenizer.advance();
				if (!isCloseSquareBracket())
				{
					// 構文エラー
					writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ], actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				//tokenizer.advance();
			}
			// TODO この部分 compileSubroutineCall とかぶってるから何とかしたい
			else if (isOpenBracket())
			{
				// subroutineName の後に続く'('だった場合
				// '(' expressionList ')'
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();

				compileExpressionList();

				if (!isCloseBracket())
				{
					// 構文エラー
					writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				// ArrayTest の「let length = Keyboard.readInt("HOW MANY NUMBERS? ");」をうまく処理するためにコメントアウト
				// tokenizer.advance();
			}
			else if (isDot())
			{
				// '.'だった場合
				// '.'
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();

				// subroutineName
				if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
				{
					// 構文エラー
					writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: subroutineName, actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

				// '(' expressionList ')'
				tokenizer.advance();
				if (!isOpenBracket())
				{
					// 構文エラー
					writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();

				compileExpressionList();

				if (!isCloseBracket())
				{
					// 構文エラー
					writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				// ArrayTest の「let length = Keyboard.readInt("HOW MANY NUMBERS? ");」をうまく処理するためにコメントアウト
				// tokenizer.advance();
			}
			else
			{
				// 無処理、varName だけだった場合はここにくる
				tokenizer.setPreloaded(true);
			}
			break;
		case TOKEN_SYMBOL:
			if (isUnaryOperator())
			{
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();
				compileTerm();
			}
			else if (isOpenBracket())
			{
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();

				compileExpression();

				tokenizer.advance();
				if (!isCloseBracket())
				{
					// 構文エラー
					writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				// TODO ここに tokenizer.advance(); を入れると Square/Square.jack のコンパイルは少し進むが、Square/Main.jack のコンパイルに失敗するようになる
				// Square/Main.jack, Square/SquareGame.jack のコンパイルを通すにはここはコメントアウト
				// tokenizer.advance();
			}
			else
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: unaryOp or (, actual: " + tokenizer.stringVal());
				return;
			}

			break;
		default:
			break;
		}

		indentLevelUp();
		writeLine(output, "</term>");
	}

	public void compileExpressionList() throws IOException{
		writeLine(output, "<expressionList>");
		indentLevelDown();

		while (!isCloseBracket())
		{
			compileExpression();

			// tokenizer.advance();
			if (isComma())
			{
				writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();
			}
		}

		indentLevelUp();
		writeLine(output, "</expressionList>");
	}

	public void compileSubroutineCall() throws IOException{
		// 1つ目は identifier
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: subroutineName or className or varName, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

		tokenizer.advance();
		if (isOpenBracket())
		{
			// 2つ目が '(' の場合
			// '(' expressionList ')'
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			tokenizer.advance();

			compileExpressionList();

			if (!isCloseBracket())
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			// tokenizer.advance();
		}
		else if (isDot())
		{
			// 2つ目が '.' の場合
			// '.'
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			tokenizer.advance();

			// subroutineName
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: subroutineName, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<identifier> " + tokenizer.identifier() + " </identifier>");

			// '(' expressionList ')'
			tokenizer.advance();
			if (!isOpenBracket())
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			tokenizer.advance();

			compileExpressionList();

			if (!isCloseBracket())
			{
				// 構文エラー
				writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(output, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			// TODO ここで advance したら、「do Screen.setColor(false);」は通るが「do game.run();」は通らない
			// tokenizer.advance();
		}
		else
		{
			// 構文エラー
			writeLine(output, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ( or ., actual: " + tokenizer.stringVal());
			return;
		}
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

	private boolean isDot() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case '.':
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

	private boolean isKeywordConstant() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			switch (tokenizer.keyWord()) {
			case KEYWORD_TRUE:
			case KEYWORD_FALSE:
			case KEYWORD_NULL:
			case KEYWORD_THIS:
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

	private boolean isUnaryOperator() {
		boolean is = false;

		if (tokenizer.tokenType() == TokenType.TOKEN_SYMBOL)
		{
			switch (tokenizer.symbol()) {
			case '-':
			case '~':
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
