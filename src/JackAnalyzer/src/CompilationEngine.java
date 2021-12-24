import java.io.BufferedWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CompilationEngine {
	private JackTokenizer tokenizer;
	private SymbolTable symbolTable;
	private VMWriter vmWriter;
	private BufferedWriter outputXml;
	private int indentLevel = 0;
	private String className = "";
	Document expressionTree;

	CompilationEngine(JackTokenizer tokenizer, BufferedWriter outputXml, BufferedWriter outputVm) {
		this.tokenizer = tokenizer;
		this.outputXml = outputXml;
		this.symbolTable = new SymbolTable();
		this.vmWriter = new VMWriter(outputVm);
		this.expressionTree = getNewDocument();
	}

	private static Document getNewDocument() {
		Document newDocument = null;
		DocumentBuilder builder = null;

		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			newDocument = builder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return newDocument;
	}

	public void compileClass() throws IOException {
		writeLine(outputXml, "<class>");
		indentLevelDown();

		tokenizer.advance();

		// KEYWORD の class
		if (!(tokenizer.tokenType() == TokenType.TOKEN_KEYWORD && tokenizer.keyWord() == KeyWord.KEYWORD_CLASS))
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: class, actual: " + tokenizer.stringVal());
			return;
		}

		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// IDENTIFIER の className
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: className, actual: " + tokenizer.stringVal());
			return;
		}
		this.className = tokenizer.identifier();
		// identifier
		writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");

		// SYMBOL の '{'
		tokenizer.advance();
		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

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
			this.symbolTable.startSubroutine();
			compileSubroutine();
			tokenizer.advance();
		}

		// SYMBOL の '}'
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		indentLevelUp();
		writeLine(outputXml, "</class>");
	}

	public void compileClassVarDec() throws IOException{
		writeLine(outputXml, "<classVarDec>");
		indentLevelDown();

		// KEYWORD の'static', 'field'(呼び出し元でチェックしているからここではしない)
		SymbolKind kind = convertKeyWordToSymbolKind(tokenizer.keyWord());
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// KEYWORD の type
		String type = "";
		tokenizer.advance();
		if (!isType())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: type, actual: " + tokenizer.stringVal());
			return;
		}
		type = getType();
		writeLineType();

		// IDENTIFIER の varName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
			return;
		}
		// identifier
		String name = tokenizer.identifier();
		this.symbolTable.define(name, type, kind);
		writeLine(outputXml, getIdentifierOpenTag(name, true) + name + " </identifier>");

		// (',' varName)*
		tokenizer.advance();
		while (isComma())
		{
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			// varName
			tokenizer.advance();
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
				return;
			}
			// identifier
			name = tokenizer.identifier();
			this.symbolTable.define(name, type, kind);
			writeLine(outputXml, getIdentifierOpenTag(name, true) + name + " </identifier>");
			tokenizer.advance();
		}

		// SYMBOL の ';'
		if (!isSemicolon())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		indentLevelUp();
		writeLine(outputXml, "</classVarDec>");
	}

	public void compileSubroutine() throws IOException{
		writeLine(outputXml, "<subroutineDec>");
		indentLevelDown();

		KeyWord subroutineType = tokenizer.keyWord();

		// KEYWORD の'constructor', 'function', 'method'(呼び出し元でチェックしているからここではしない)
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// KEYWORD の type
		tokenizer.advance();
		if (!isType())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: type, actual: " + tokenizer.stringVal());
			return;
		}
		writeLineType();

		// IDENTIFIER の subroutineName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: subroutineName, actual: " + tokenizer.stringVal());
			return;
		}
		// identifier
		writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");
		vmWriter.writeFunction(className + "." + tokenizer.identifier(), symbolTable.varCount(SymbolKind.KIND_VAR));

		// SYMBOL の '('
		tokenizer.advance();
		if (!isOpenBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		tokenizer.advance();
		compileParameterList();

		// SYMBOL の ')'
		if (!isCloseBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		tokenizer.advance();
		compileSubroutineBody(subroutineType);

		indentLevelUp();
		writeLine(outputXml, "</subroutineDec>");
	}

	public void compileParameterList() throws IOException{
		writeLine(outputXml, "<parameterList>");
		indentLevelDown();

		while (!isCloseBracket())
		{
			// type
			String type = "";
			if (!isType())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: type, actual: " + tokenizer.stringVal());
				return;
			}
			type = getType();
			writeLineType();

			// IDENTIFIER の varName
			tokenizer.advance();
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
				return;
			}
			// identifier
			String name = tokenizer.identifier();
			this.symbolTable.define(name, type,  SymbolKind.KIND_ARG);
			writeLine(outputXml, getIdentifierOpenTag(name, true) + name + " </identifier>");

			tokenizer.advance();
			if (isComma())
			{
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();
			}
		}
		indentLevelUp();
		writeLine(outputXml, "</parameterList>");
	}

	public void compileSubroutineBody(KeyWord subroutineType) throws IOException{
		writeLine(outputXml, "<subroutineBody>");
		indentLevelDown();

		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");


		if (subroutineType == KeyWord.KEYWORD_METHOD)
		{
			this.symbolTable.define("this", this.className, SymbolKind.KIND_ARG);
		}

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
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		indentLevelUp();
		writeLine(outputXml, "</subroutineBody>");
	}

	public void compileVarDec() throws IOException{
		writeLine(outputXml, "<varDec>");
		indentLevelDown();

		// KEYWORD の'var'(呼び出し元でチェックしているからここではしない)
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// KEYWORD の type
		String type = "";
		tokenizer.advance();
		if (!isType())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: type, actual: " + tokenizer.stringVal());
			return;
		}
		type = getType();
		writeLineType();

		// IDENTIFIER の varName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
			return;
		}
		// identifier
		String name = tokenizer.identifier();
		this.symbolTable.define(name, type, SymbolKind.KIND_VAR);
		writeLine(outputXml, getIdentifierOpenTag(name, true) + name + " </identifier>");

		// (',' varName)*
		tokenizer.advance();
		while (isComma())
		{
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			// varName
			tokenizer.advance();
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
				return;
			}
			// identifier
			name = tokenizer.identifier();
			this.symbolTable.define(name, type, SymbolKind.KIND_VAR);
			writeLine(outputXml, getIdentifierOpenTag(name, true) + name + " </identifier>");
			tokenizer.advance();
		}

		// SYMBOL の ';'
		if (!isSemicolon())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		indentLevelUp();
		writeLine(outputXml, "</varDec>");
	}

	public void compileStatements() throws IOException{
		writeLine(outputXml, "<statements>");
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
		writeLine(outputXml, "</statements>");
	}

	public void compileDo() throws IOException{
		writeLine(outputXml, "<doStatement>");
		indentLevelDown();

		// KEYWORD の'do'(呼び出し元でチェックしているからここではしない)
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// subroutineCall
		tokenizer.advance();
		compileSubroutineCall();

		// ';'
		tokenizer.setPreloaded(false);
		tokenizer.advance();
		if (!isSemicolon())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		indentLevelUp();
		writeLine(outputXml, "</doStatement>");
	}

	public void compileLet() throws IOException{
		writeLine(outputXml, "<letStatement>");
		indentLevelDown();

		// KEYWORD の'let'(呼び出し元でチェックしているからここではしない)
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// varName
		tokenizer.advance();
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: varName, actual: " + tokenizer.stringVal());
			return;
		}
		// identifier
		String name = tokenizer.identifier();
		writeLine(outputXml, getIdentifierOpenTag(name, false) + name + " </identifier>");

		// ('[' expression ']')?
		tokenizer.advance();
		if (isOpenSquareBracket())
		{
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

			tokenizer.advance();
			this.expressionTree = getNewDocument();
			compileExpression(this.expressionTree);
			writeExpressionVMCode(this.expressionTree);

			tokenizer.advance();
			if (!isCloseSquareBracket())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ], actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			tokenizer.advance();
		}

		// '='
		if (!isEqual())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: =, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// expression
		tokenizer.advance();
		this.expressionTree = getNewDocument();
		compileExpression(this.expressionTree);
		writeExpressionVMCode(this.expressionTree);

		// ';'
		tokenizer.advance();
		if (!isSemicolon())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		indentLevelUp();
		writeLine(outputXml, "</letStatement>");
	}

	public void compileWhile() throws IOException{
		writeLine(outputXml, "<whileStatement>");
		indentLevelDown();

		// KEYWORD の'while'(呼び出し元でチェックしているからここではしない)
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// '('
		tokenizer.advance();
		if (!isOpenBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// expression
		tokenizer.advance();
		this.expressionTree = getNewDocument();
		compileExpression(this.expressionTree);
		writeExpressionVMCode(this.expressionTree);

		// ')'
		tokenizer.advance();
		if (!isCloseBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// '{'
		tokenizer.advance();
		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		tokenizer.advance();
		compileStatements();

		// '}'
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		indentLevelUp();
		writeLine(outputXml, "</whileStatement>");
	}

	public void compileReturn() throws IOException{
		writeLine(outputXml, "<returnStatement>");
		indentLevelDown();

		// KEYWORD の'return'(呼び出し元でチェックしているからここではしない)
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		vmWriter.writePush(Segment.SEGMENT_CONST, 0);
		vmWriter.writeReturn();

		tokenizer.advance();
		if (isSemicolon())
		{
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
		}
		else
		{
			// expression?
			this.expressionTree = getNewDocument();
			compileExpression(this.expressionTree);
			writeExpressionVMCode(this.expressionTree);

			// ';'
			tokenizer.advance();
			if (!isSemicolon())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
		}

		indentLevelUp();
		writeLine(outputXml, "</returnStatement>");
	}

	public void compileIf() throws IOException{
		writeLine(outputXml, "<ifStatement>");
		indentLevelDown();

		// KEYWORD の'if'(呼び出し元でチェックしているからここではしない)
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		// '('
		tokenizer.advance();
		if (!isOpenBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// expression
		tokenizer.advance();
		this.expressionTree = getNewDocument();
		compileExpression(this.expressionTree);
		writeExpressionVMCode(this.expressionTree);

		// ')'
		tokenizer.advance();
		if (!isCloseBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// '{'
		tokenizer.advance();
		if (!isOpenCurlyBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		tokenizer.advance();
		compileStatements();

		// '}'
		if (!isCloseCurlyBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		// ('else' '{' statements '}')?
		tokenizer.advance();
		if (isElse())
		{
			writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

			tokenizer.advance();
			if (!isOpenCurlyBracket())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: {, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

			tokenizer.advance();
			compileStatements();

			//tokenizer.advance();
			if (!isCloseCurlyBracket())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: }, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

			tokenizer.advance();
		}

		indentLevelUp();
		writeLine(outputXml, "</ifStatement>");
	}

	public void compileExpression(Node expressionRoot) throws IOException{
		writeLine(outputXml, "<expression>");
		Element expression = expressionTree.createElement("expression");
		expressionRoot.appendChild(expression);

		indentLevelDown();

		// term
		compileTerm(expression);

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
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

				Element operator = expressionTree.createElement("operator");
				operator.setTextContent(convertSymbolToString(tokenizer.symbol()));
				expression.appendChild(operator);

				tokenizer.advance();
				compileTerm(expression);
				// ここで advance しないといけないケースあると思うが、コメントアウトすることで ArrayTest/Main.jack のコンパイルが通った
				//tokenizer.advance();
			}
		}

		indentLevelUp();
		writeLine(outputXml, "</expression>");
	}

	public void compileTerm(Node expressionRoot) throws IOException{
		writeLine(outputXml, "<term>");
		indentLevelDown();

		Element term = expressionTree.createElement("term");
		expressionRoot.appendChild(term);

		switch (tokenizer.tokenType()) {
		case TOKEN_INT_CONST:
			writeLine(outputXml, "<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
			Element integerConstant = expressionTree.createElement("integerConstant");
			integerConstant.setTextContent(String.valueOf(tokenizer.intVal()));
			term.appendChild(integerConstant);
			break;
		case TOKEN_STRING_CONST:
			writeLine(outputXml, "<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
			Element stringConstant = expressionTree.createElement("stringConstant");
			stringConstant.setTextContent(tokenizer.stringVal());
			term.appendChild(stringConstant);
			break;
		case TOKEN_KEYWORD:
			if (!isKeywordConstant())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: keywordConstant, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");
			break;
		case TOKEN_IDENTIFIER:
			// '(' か '.' なら subroutineCall、そうでなければ varName
			// varName or subroutineCall
			String name = tokenizer.identifier();

			// ('[' expression ']')?
			tokenizer.advance();
			if (isOpenSquareBracket())
			{
				// varName の後に続く'['だった場合
				writeLine(outputXml, getIdentifierOpenTag(name, false) + name + " </identifier>");

				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

				tokenizer.advance();
				compileExpression(expressionRoot);

				tokenizer.advance();
				if (!isCloseSquareBracket())
				{
					// 構文エラー
					writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ], actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				//tokenizer.advance();
			}
			// TODO この部分 compileSubroutineCall とかぶってるから何とかしたい
			else if (isOpenBracket())
			{
				// subroutineName の後に続く'('だった場合
				writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");
				// '(' expressionList ')'
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();

				compileExpressionList();

				if (!isCloseBracket())
				{
					// 構文エラー
					writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				// ArrayTest の「let length = Keyboard.readInt("HOW MANY NUMBERS? ");」をうまく処理するためにコメントアウト
				// tokenizer.advance();
			}
			else if (isDot())
			{
				// '.'だった場合
				writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");

				// '.'
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();

				// subroutineName
				if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
				{
					// 構文エラー
					writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: subroutineName, actual: " + tokenizer.stringVal());
					return;
				}
				// identifier
				writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");

				// '(' expressionList ')'
				tokenizer.advance();
				if (!isOpenBracket())
				{
					// 構文エラー
					writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();

				compileExpressionList();

				if (!isCloseBracket())
				{
					// 構文エラー
					writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				// ArrayTest の「let length = Keyboard.readInt("HOW MANY NUMBERS? ");」をうまく処理するためにコメントアウト
				// tokenizer.advance();
			}
			else
			{
				// 無処理、varName だけだった場合はここにくる
				writeLine(outputXml, getIdentifierOpenTag(name, false) + name + " </identifier>");
				Element varName = expressionTree.createElement("varName");
				varName.setTextContent(name);
				term.appendChild(varName);

				tokenizer.setPreloaded(true);
			}
			break;
		case TOKEN_SYMBOL:
			if (isUnaryOperator())
			{
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				Element unaryOperator = expressionTree.createElement("unaryOperator");
				unaryOperator.setTextContent(convertSymbolToString(tokenizer.symbol()));
				expressionRoot.appendChild(unaryOperator);

				tokenizer.advance();
				compileTerm(expressionRoot);
			}
			else if (isOpenBracket())
			{
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();

				compileExpression(expressionRoot);

				tokenizer.advance();
				if (!isCloseBracket())
				{
					// 構文エラー
					writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
					return;
				}
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				// Square/Main.jack, Square/SquareGame.jack のコンパイルを通すにはここはコメントアウト
				// tokenizer.advance();
			}
			else
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: unaryOp or (, actual: " + tokenizer.stringVal());
				return;
			}

			break;
		default:
			break;
		}

		indentLevelUp();
		writeLine(outputXml, "</term>");
	}

	public void compileExpressionList() throws IOException{
		writeLine(outputXml, "<expressionList>");
		indentLevelDown();

		while (!isCloseBracket())
		{
			this.expressionTree = getNewDocument();
			compileExpression(this.expressionTree);
			writeExpressionVMCode(this.expressionTree);

			tokenizer.advance();
			if (isComma())
			{
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();
			}
		}

		indentLevelUp();
		writeLine(outputXml, "</expressionList>");
	}

	public void compileSubroutineCall() throws IOException {
		// 1つ目は identifier
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: subroutineName or className or varName, actual: " + tokenizer.stringVal());
			return;
		}
		// identifier
		writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");

		tokenizer.advance();
		if (isOpenBracket())
		{
			// 2つ目が '(' の場合
			// '(' expressionList ')'
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			tokenizer.advance();

			compileExpressionList();

			if (!isCloseBracket())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			// tokenizer.advance();
		}
		else if (isDot())
		{
			// 2つ目が '.' の場合
			// '.'
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			tokenizer.advance();

			// subroutineName
			if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: subroutineName, actual: " + tokenizer.stringVal());
				return;
			}
			// identifier
			writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");

			// '(' expressionList ')'
			tokenizer.advance();
			if (!isOpenBracket())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: (, actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			tokenizer.advance();

			compileExpressionList();

			if (!isCloseBracket())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
				return;
			}
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			// tokenizer.advance();
		}
		else
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ( or ., actual: " + tokenizer.stringVal());
			return;
		}
	}

	private void writeExpressionVMCode(Node expNode) throws IOException {
		if (expNode.getNodeName().equals("integerConstant"))
		{
			// expが数字の場合
			int constantValue = Integer.parseInt(expNode.getFirstChild().getTextContent());
			vmWriter.writePush(Segment.SEGMENT_CONST, constantValue);
		}
		else if (expNode.getNodeName().equals("varName"))
		{
			// expが変数の場合
			String varName = expNode.getFirstChild().getTextContent();
			SymbolKind kind = this.symbolTable.kindOf(varName);
			int index = this.symbolTable.indexOf(varName);
			vmWriter.writePush(convertKindToSegment(kind), index);
		}
		else
		{
			// TODO writeExpressionVMCode
			Node childNode = expNode.getFirstChild();
			while (childNode != null)
			{
				writeExpressionVMCode(childNode);
				// TODO op を出力
				// TODO "call f"を出力
				childNode = childNode.getNextSibling();
			}
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
			writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");
		}
		else
		{
			// identifier
			writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");
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

	private String convertSymbolToString(char symbol) {
		String symbolStr = String.valueOf(symbol);

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

	private String getType() {
		String retType = "xxx";
		if (tokenizer.tokenType() == TokenType.TOKEN_KEYWORD)
		{
			retType = keyWordToString(tokenizer.keyWord());
		}
		else
		{
			retType = tokenizer.identifier();
		}
		return retType;
	}

	private SymbolKind convertKeyWordToSymbolKind(KeyWord keyWord) {
		SymbolKind retKind = SymbolKind.KIND_NONE;

		switch (keyWord) {
		case KEYWORD_STATIC:
			retKind = SymbolKind.KIND_STATIC;
			break;
		case KEYWORD_FIELD:
			retKind = SymbolKind.KIND_FIELD;
			break;
		case KEYWORD_VAR:
			retKind = SymbolKind.KIND_VAR;
			break;
		default:
			break;
		}
		return retKind;
	}

	private String getSymbolCategory(SymbolKind kind) {
		String retCategory = "class";

		switch (kind) {
		case KIND_STATIC:
		case KIND_FIELD:
			retCategory = "class";
			break;
		case KIND_VAR:
		case KIND_ARG:
			retCategory = "subroutine";
			break;
		default:
			break;
		}
		return retCategory;
	}

	private String getIdentifierOpenTag(String name, boolean defined) {
		String retTagStr = "<identifier>";

		String category = getSymbolCategory(this.symbolTable.kindOf(name));
		String context = "defined";

		if (!defined)
		{
			context = "used";
		}

		retTagStr = "<identifier category=\"" + category + "\" context=\"" + context
						+ "\" kind=\"" + convertKindToString(this.symbolTable.kindOf(name)) + "\" index=\"" + this.symbolTable.indexOf(name) + "\"> ";

		return retTagStr;
	}

	private String convertKindToString(SymbolKind kind) {
		String kindStr = "none";
		switch (kind) {
		case KIND_NONE:
			kindStr = "none";
			break;
		case KIND_STATIC:
			kindStr = "static";
			break;
		case KIND_FIELD:
			kindStr = "field";
			break;
		case KIND_ARG:
			kindStr = "argument";
			break;
		case KIND_VAR:
			kindStr = "var";
			break;
		default:
			break;
		}

		return kindStr;
	}

	private Segment convertKindToSegment(SymbolKind kind) {
		Segment segment = Segment.SEGMENT_CONST;
		switch (kind) {
		case KIND_NONE:
			break;
		case KIND_STATIC:
			segment = Segment.SEGMENT_STATIC;
			break;
		case KIND_FIELD:
			// TODO KIND_FIELD はどの Segment に変換すればいい？
			break;
		case KIND_ARG:
			segment = Segment.SEGMENT_ARG;
			break;
		case KIND_VAR:
			segment = Segment.SEGMENT_LOCAL;
			break;
		default:
			break;
		}

		return segment;
	}

}
