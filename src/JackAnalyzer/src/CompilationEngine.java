import java.io.BufferedWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CompilationEngine {
	private JackTokenizer tokenizer;
	private SymbolTable symbolTable;
	private VMWriter vmWriter;
	private BufferedWriter outputXml;
	private int indentLevel = 0;
	private String className = "";
	Document expressionTree;
	private int labelNum = 1;

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

		boolean isConstructor = tokenizer.keyWord() == KeyWord.KEYWORD_CONSTRUCTOR;

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
		String subroutineName = tokenizer.identifier();

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
		compileSubroutineBody(subroutineType, subroutineName, isConstructor);

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

	public void compileSubroutineBody(KeyWord subroutineType, String subroutineName, boolean isConstructor) throws IOException{
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

		vmWriter.writeFunction(className + "." + subroutineName, symbolTable.varCount(SymbolKind.KIND_VAR));

		if (isConstructor)
		{
			vmWriter.writePush(Segment.SEGMENT_CONST, this.symbolTable.varCount(SymbolKind.KIND_FIELD));
			vmWriter.writeCall("Memory.alloc", 1);
			vmWriter.writePop(Segment.SEGMENT_POINTER, 0);
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

		this.expressionTree = getNewDocument();
		compileSubroutineCall(this.expressionTree);
		writeExpressionVMCode(this.expressionTree);
		// System.out.println(Util.createXMLString(this.expressionTree));

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
		String varName = tokenizer.identifier();
		writeLine(outputXml, getIdentifierOpenTag(varName, false) + varName + " </identifier>");

		// ('[' expression ']')?
		tokenizer.advance();
		if (isOpenSquareBracket())
		{
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

			tokenizer.advance();
			this.expressionTree = getNewDocument();
			compileExpression(this.expressionTree);
			writeExpressionVMCode(this.expressionTree);
			// System.out.println(Util.createXMLString(this.expressionTree));

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
		// System.out.println(Util.createXMLString(this.expressionTree));

		SymbolKind kind = this.symbolTable.kindOf(varName);
		int index = this.symbolTable.indexOf(varName);
		vmWriter.writePop(convertKindToSegment(kind), index);

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

		String startLabel = "START-WHILE-L" + String.valueOf(this.labelNum++);
		String endLabel = "END-WHILE-L" + String.valueOf(this.labelNum++);

		// KEYWORD の'while'(呼び出し元でチェックしているからここではしない)
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");

		vmWriter.writeLabel(startLabel);

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
		// System.out.println(Util.createXMLString(this.expressionTree));

		// ')'
		tokenizer.advance();
		if (!isCloseBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		vmWriter.writeArithmetic(Command.COMMAND_NOT);
		vmWriter.writeIf(endLabel);

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

		vmWriter.writeGoto(startLabel);
		vmWriter.writeLabel(endLabel);

		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		indentLevelUp();
		writeLine(outputXml, "</whileStatement>");
	}

	public void compileReturn() throws IOException{
		writeLine(outputXml, "<returnStatement>");
		indentLevelDown();

		// KEYWORD の'return'(呼び出し元でチェックしているからここではしない)
		writeLine(outputXml, "<keyword> " + keyWordToString(tokenizer.keyWord()) + " </keyword>");


		tokenizer.advance();
		if (isSemicolon())
		{
			vmWriter.writePush(Segment.SEGMENT_CONST, 0);
			vmWriter.writeReturn();

			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
		}
		else
		{
			// expression?
			this.expressionTree = getNewDocument();
			compileExpression(this.expressionTree);
			writeExpressionVMCode(this.expressionTree);
			// System.out.println(Util.createXMLString(this.expressionTree));

			// ';'
			tokenizer.advance();
			if (!isSemicolon())
			{
				// 構文エラー
				writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ;, actual: " + tokenizer.stringVal());
				return;
			}

			vmWriter.writeReturn();

			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
		}

		indentLevelUp();
		writeLine(outputXml, "</returnStatement>");
	}

	public void compileIf() throws IOException{
		writeLine(outputXml, "<ifStatement>");
		indentLevelDown();

		String endIfLabel = "END-IF-L" + String.valueOf(this.labelNum++);
		String elseLabel = "ELSE-L" + String.valueOf(this.labelNum++);

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
		// System.out.println(Util.createXMLString(this.expressionTree));

		// ')'
		tokenizer.advance();
		if (!isCloseBracket())
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: ), actual: " + tokenizer.stringVal());
			return;
		}
		writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");

		vmWriter.writeArithmetic(Command.COMMAND_NOT);
		vmWriter.writeIf(elseLabel);

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

		vmWriter.writeGoto(endIfLabel);

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

			vmWriter.writeLabel(elseLabel);

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

		vmWriter.writeLabel(endIfLabel);

		indentLevelUp();
		writeLine(outputXml, "</ifStatement>");
	}

	public void compileExpression(Node expressionRoot) throws IOException{
		writeLine(outputXml, "<expression>");
		Element expression = this.expressionTree.createElement("expression");
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

				Element operator = this.expressionTree.createElement("operator");
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

		Element term = this.expressionTree.createElement("term");

		switch (tokenizer.tokenType()) {
		case TOKEN_INT_CONST:
			writeLine(outputXml, "<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
			Element integerConstant = this.expressionTree.createElement("integerConstant");
			integerConstant.setTextContent(String.valueOf(tokenizer.intVal()));

			expressionRoot.appendChild(term);
			term.appendChild(integerConstant);
			break;
		case TOKEN_STRING_CONST:
			writeLine(outputXml, "<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
			Element stringConstant = this.expressionTree.createElement("stringConstant");
			stringConstant.setTextContent(tokenizer.stringVal());

			expressionRoot.appendChild(term);
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

			Element keywordConstant = this.expressionTree.createElement("keywordConstant");
			keywordConstant.setTextContent(keyWordToString(tokenizer.keyWord()));

			expressionRoot.appendChild(term);
			term.appendChild(keywordConstant);

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
				String subroutineName = name;
				int numberOfArguments = 0;

				writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");
				// '(' expressionList ')'
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();

				// TODO オブジェクト指向対応
				Element subroutineCall = this.expressionTree.createElement("subroutineCall");
				subroutineCall.setTextContent(subroutineName);
				expressionRoot.appendChild(subroutineCall);

				numberOfArguments = compileExpressionList(subroutineCall);
				subroutineCall.setAttribute("numberOfArguments", Integer.toString(numberOfArguments));

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
				boolean isInstanceMethod = false;
				String type = name;

				if (this.symbolTable.kindOf(name) != SymbolKind.KIND_NONE)
				{
					type = this.symbolTable.typeOf(name);
					isInstanceMethod = true;
				}

				String subroutineName = type;
				int numberOfArguments = 0;

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
				subroutineName = subroutineName + "." + tokenizer.identifier();

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

				Element subroutineCall = this.expressionTree.createElement("subroutineCall");
				subroutineCall.setTextContent(subroutineName);
				expressionRoot.appendChild(subroutineCall);

				if (isInstanceMethod)
				{
					// 隠れ引数を渡す
					Element varName = this.expressionTree.createElement("varName");
					varName.setTextContent(name);

					subroutineCall.appendChild(term);
					term.appendChild(varName);
				}

				numberOfArguments = compileExpressionList(subroutineCall);

				if (isInstanceMethod)
				{
					// 隠れ引数として渡した分をインクリメント
					numberOfArguments++;
				}

				subroutineCall.setAttribute("numberOfArguments", Integer.toString(numberOfArguments));

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
				Element varName = this.expressionTree.createElement("varName");
				varName.setTextContent(name);

				expressionRoot.appendChild(term);
				term.appendChild(varName);

				tokenizer.setPreloaded(true);
			}
			break;
		case TOKEN_SYMBOL:
			if (isUnaryOperator())
			{
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				Element unaryOperator = this.expressionTree.createElement("unaryOperator");
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

	public int compileExpressionList(Node expressionRoot) throws IOException{
		writeLine(outputXml, "<expressionList>");
		indentLevelDown();

		int numberOfArguments = 0;

		while (!isCloseBracket())
		{
			numberOfArguments++;
			// this.expressionTree = getNewDocument();
			compileExpression(expressionRoot);
//			writeExpressionVMCode(this.expressionTree);
//			System.out.println(Util.createXMLString(this.expressionTree));

			tokenizer.advance();
			if (isComma())
			{
				writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
				tokenizer.advance();
			}
		}

		indentLevelUp();
		writeLine(outputXml, "</expressionList>");

		return numberOfArguments;
	}

	public void compileSubroutineCall(Node expressionRoot) throws IOException {
		// 1つ目は identifier
		if (!(tokenizer.tokenType() == TokenType.TOKEN_IDENTIFIER))
		{
			// 構文エラー
			writeLine(outputXml, new Object(){}.getClass().getEnclosingMethod().getName() + ", Syntax error. expected: subroutineName or className or varName, actual: " + tokenizer.stringVal());
			return;
		}
		// identifier
		writeLine(outputXml, "<identifier> " + tokenizer.identifier() + " </identifier>");

		String name = tokenizer.identifier();

		tokenizer.advance();
		if (isOpenBracket())
		{
			// 2つ目が '(' の場合
			String subroutineName = name;
			int numberOfArguments = 0;

			// '(' expressionList ')'
			writeLine(outputXml, "<symbol> " + convertSymbolToXmlElement(tokenizer.symbol()) + " </symbol>");
			tokenizer.advance();

			// TODO オブジェクト指向対応
			Element subroutineCall = this.expressionTree.createElement("subroutineCall");
			subroutineCall.setTextContent(subroutineName);
			expressionRoot.appendChild(subroutineCall);

			numberOfArguments = compileExpressionList(subroutineCall);
			subroutineCall.setAttribute("numberOfArguments", Integer.toString(numberOfArguments));

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
			boolean isInstanceMethod = false;
			String type = name;

			if (this.symbolTable.kindOf(name) != SymbolKind.KIND_NONE)
			{
				type = this.symbolTable.typeOf(name);
				isInstanceMethod = true;
			}

			String subroutineName = type;
			int numberOfArguments = 0;

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
			subroutineName = subroutineName + "." + tokenizer.identifier();

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

			Element subroutineCall = this.expressionTree.createElement("subroutineCall");
			subroutineCall.setTextContent(subroutineName);
			expressionRoot.appendChild(subroutineCall);

			if (isInstanceMethod)
			{
				// 隠れ引数を渡す
				Element varName = this.expressionTree.createElement("varName");
				varName.setTextContent(name);

				Element term = this.expressionTree.createElement("term");

				subroutineCall.appendChild(term);
				term.appendChild(varName);
			}

			numberOfArguments = compileExpressionList(subroutineCall);

			if (isInstanceMethod)
			{
				// 隠れ引数として渡した分をインクリメント
				numberOfArguments++;
			}

			subroutineCall.setAttribute("numberOfArguments", Integer.toString(numberOfArguments));

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
		NodeList expNodeList = expNode.getChildNodes();

		if (expNode.getNodeName().equals("subroutineCall"))
		{
			// サブルーチン呼び出しの場合
			String subroutineName = expNode.getFirstChild().getTextContent();
			Element expElem = (Element)expNode;
			int numberOfArguments = Integer.parseInt(expElem.getAttribute("numberOfArguments"));

			int childCount = numberOfArguments;
			// 一つ目の子は subroutineName なので読み飛ばす
			Node childNode = expNode.getFirstChild();
			childNode = childNode.getNextSibling();
			while ((childCount > 0) && (childNode != null))
			{
				writeExpressionVMCode(childNode);
				childNode = childNode.getNextSibling();
				childCount--;
			}

			vmWriter.writeCall(subroutineName, numberOfArguments);
		}
		else if (expNodeList.getLength() == 3)
		{
			// exp が (exp1 op exp2) である場合
			if (isOperatorSymbolStr(expNodeList.item(1).getFirstChild().getTextContent()))
			{
				// Operands
				writeExpressionVMCode(expNodeList.item(0));
				writeExpressionVMCode(expNodeList.item(2));

				// Operator
				writeExpressionVMCode(expNodeList.item(1));
			}
		}
		else if (expNodeList.getLength() == 2)
		{
			// op (exp1) である場合
			if (isOperatorSymbolStr(expNodeList.item(0).getTextContent()))
			{
				// Operand
				writeExpressionVMCode(expNodeList.item(1));

				// Operator
				writeExpressionVMCode(expNodeList.item(0));
			}
		}
		else if (expNode.getNodeName().equals("integerConstant"))
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
		else if (expNode.getNodeName().equals("keywordConstant"))
		{
			// expがキーワードの場合
			String keywordStr = expNode.getFirstChild().getTextContent();
			if (keywordStr.equals("true"))
			{
				vmWriter.writePush(Segment.SEGMENT_CONST, 1);
				vmWriter.writeArithmetic(Command.COMMAND_NEG);
			}
			else if (keywordStr.equals("this"))
			{
				vmWriter.writePush(Segment.SEGMENT_POINTER, 0);
			}
			else
			{
				vmWriter.writePush(Segment.SEGMENT_CONST, 0);
			}
		}
		else if (expNode.getNodeName().equals("operator"))
		{
			// expがoperatorの場合
			String operatorStr = expNode.getFirstChild().getTextContent();

			if (operatorStr.equals("*"))
			{
				vmWriter.writeCall("Math.multiply", 2);
			}
			else if (operatorStr.equals("/"))
			{
				vmWriter.writeCall("Math.divide", 2);
			}
			else
			{
				vmWriter.writeArithmetic(convertOperatorStrToCommand(operatorStr));
			}
		}
		else if (expNode.getNodeName().equals("unaryOperator"))
		{
			// expがunaryOperatorの場合
			String operatorStr = expNode.getFirstChild().getTextContent();

			vmWriter.writeArithmetic(convertUnaryOperatorStrToCommand(operatorStr));
		}
		else
		{
			// System.out.println(expNode.getChildNodes().getLength());
			Node childNode = expNode.getFirstChild();
			while (childNode != null)
			{
				writeExpressionVMCode(childNode);
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

	private boolean isOperatorSymbolStr(String str) {
		boolean is = false;

		switch (str) {
		case "+":
		case "-":
		case "*":
		case "/":
		case "&":
		case "|":
		case "<":
		case ">":
		case "=":
			// 二項演算子
			is = true;
			break;
		case "~":
			// 単項演算子
			is = true;
			break;
		default:
			break;
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
			segment = Segment.SEGMENT_THIS;
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

	private Command convertOperatorStrToCommand(String operatorStr) {
		Command command = Command.COMMAND_ADD;

		switch (operatorStr) {
		case "+":
			command = Command.COMMAND_ADD;
			break;
		case "-":
			command = Command.COMMAND_SUB;
			break;
		case "=":
			command = Command.COMMAND_EQ;
			break;
		case ">":
			command = Command.COMMAND_GT;
			break;
		case "<":
			command = Command.COMMAND_LT;
			break;
		case "&":
			command = Command.COMMAND_AND;
			break;
		case "|":
			command = Command.COMMAND_OR;
			break;
		default:
			break;
		}

		return command;
	}

	private Command convertUnaryOperatorStrToCommand(String operatorStr) {
		Command command = Command.COMMAND_NEG;

		switch (operatorStr) {
		case "-":
			command = Command.COMMAND_NEG;
			break;
		case "~":
			command = Command.COMMAND_NOT;
			break;
		default:
			break;
		}

		return command;
	}

}
