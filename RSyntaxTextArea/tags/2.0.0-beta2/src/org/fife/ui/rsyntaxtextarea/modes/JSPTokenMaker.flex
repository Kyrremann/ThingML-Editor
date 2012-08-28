/*
 * 02/11/2008
 *
 * JSPTokenMaker.java - Generates tokens for JSP syntax highlighting.
 * Copyright (C) 2008 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 */
package org.fife.ui.rsyntaxtextarea.modes;

import java.io.*;
import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.*;


/**
 * Scanner for JSP files (supporting HTML 5).
 *
 * This implementation was created using
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.1; however, the generated file
 * was modified for performance.  Memory allocation needs to be almost
 * completely removed to be competitive with the handwritten lexers (subclasses
 * of <code>AbstractTokenMaker</code>, so this class has been modified so that
 * Strings are never allocated (via yytext()), and the scanner never has to
 * worry about refilling its buffer (needlessly copying chars around).
 * We can achieve this because RText always scans exactly 1 line of tokens at a
 * time, and hands the scanner this line as an array of characters (a Segment
 * really).  Since tokens contain pointers to char arrays instead of Strings
 * holding their contents, there is no need for allocating new memory for
 * Strings.<p>
 *
 * The actual algorithm generated for scanning has, of course, not been
 * modified.<p>
 *
 * If you wish to regenerate this file yourself, keep in mind the following:
 * <ul>
 *   <li>The generated JSPTokenMaker.java</code> file will contain two
 *       definitions of both <code>zzRefill</code> and <code>yyreset</code>.
 *       You should hand-delete the second of each definition (the ones
 *       generated by the lexer), as these generated methods modify the input
 *       buffer, which we'll never have to do.</li>
 *   <li>You should also change the declaration/definition of zzBuffer to NOT
 *       be initialized.  This is a needless memory allocation for us since we
 *       will be pointing the array somewhere else anyway.</li>
 *   <li>You should NOT call <code>yylex()</code> on the generated scanner
 *       directly; rather, you should use <code>getTokenList</code> as you would
 *       with any other <code>TokenMaker</code> instance.</li>
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.7
 *
 */
%%

%public
%class JSPTokenMaker
%extends AbstractMarkupTokenMaker
%unicode
%type org.fife.ui.rsyntaxtextarea.Token


%{

	/**
	 * Type specific to JSPTokenMaker denoting a line ending with an unclosed
	 * double-quote attribute.
	 */
	public static final int INTERNAL_ATTR_DOUBLE			= -1;


	/**
	 * Type specific to JSPTokenMaker denoting a line ending with an unclosed
	 * single-quote attribute.
	 */
	public static final int INTERNAL_ATTR_SINGLE			= -2;


	/**
	 * Token type specific to JSPTokenMaker; this signals that the user has
	 * ended a line with an unclosed HTML tag; thus a new line is beginning
	 * still inside of the tag.
	 */
	public static final int INTERNAL_INTAG					= -3;

	/**
	 * Token type specific to JSPTokenMaker; this signals that the user has
	 * ended a line with an unclosed <code>&lt;script&gt;</code> tag.
	 */
	public static final int INTERNAL_INTAG_SCRIPT			= -4;

	/**
	 * Token type specifying we're in a double-qouted attribute in a
	 * script tag.
	 */
	public static final int INTERNAL_ATTR_DOUBLE_QUOTE_SCRIPT = -5;

	/**
	 * Token type specifying we're in a single-qouted attribute in a
	 * script tag.
	 */
	public static final int INTERNAL_ATTR_SINGLE_QUOTE_SCRIPT = -6;

	/**
	 * Token type specifying we're in a JSP hidden comment ("<%-- ... --%>").
	 */
	public static final int INTERNAL_IN_HIDDEN_COMMENT		= -7;

	/**
	 * Token type specifying we're in a JSP directive (either include, page
	 * or taglib).
	 */
	public static final int INTERNAL_IN_JSP_DIRECTIVE			= -8;

	/**
	 * Token type specifying we're in JavaScript.
	 */
	public static final int INTERNAL_IN_JS					= -9;

	/**
	 * Token type specifying we're in a JavaScript multiline comment.
	 */
	public static final int INTERNAL_IN_JS_MLC				= -10;

	/**
	 * Token type specifying we're in a Java documentation comment.
	 */
	public static final int INTERNAL_IN_JAVA_DOCCOMMENT		= -(1<<11);

	/**
	 * Token type specifying we're in Java code.
	 */
	public static final int INTERNAL_IN_JAVA_EXPRESSION		= -(2<<11);

	/**
	 * Token type specifying we're in Java multiline comment.
	 */
	public static final int INTERNAL_IN_JAVA_MLC				= -(3<<11);

	/**
	 * The state JSP was started in (YYINITIAL, INTERNAL_IN_JS, etc.).
	 */
	private int jspInState;

	/**
	 * Whether closing markup tags are automatically completed for JSP.
	 */
	private static boolean completeCloseTags;


	/**
	 * Constructor.  This must be here because JFlex does not generate a
	 * no-parameter constructor.
	 */
	public JSPTokenMaker() {
		super();
	}


	/**
	 * Adds the token specified to the current linked list of tokens as an
	 * "end token;" that is, at <code>zzMarkedPos</code>.
	 *
	 * @param tokenType The token's type.
	 */
	private void addEndToken(int tokenType) {
		addToken(zzMarkedPos,zzMarkedPos, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addToken(int, int, int)
	 */
	private void addHyperlinkToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, true);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int tokenType) {
		addToken(zzStartRead, zzMarkedPos-1, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param array The character array.
	 * @param start The starting offset in the array.
	 * @param end The ending offset in the array.
	 * @param tokenType The token's type.
	 * @param startOffset The offset in the document at which this token
	 *                    occurs.
	 */
	public void addToken(char[] array, int start, int end, int tokenType, int startOffset) {
		super.addToken(array, start,end, tokenType, startOffset);
		zzStartRead = zzMarkedPos;
	}


	/**
	 * Sets whether markup close tags should be completed.  You might not want
	 * this to be the case, since some tags in standard HTML aren't usually
	 * closed.
	 *
	 * @return Whether closing markup tags are completed.
	 * @see #setCompleteCloseTags(boolean)
	 */
	public boolean getCompleteCloseTags() {
		return completeCloseTags;
	}


	/**
	 * Returns the first token in the linked list of tokens generated
	 * from <code>text</code>.  This method must be implemented by
	 * subclasses so they can correctly implement syntax highlighting.
	 *
	 * @param text The text from which to get tokens.
	 * @param initialTokenType The token type we should start with.
	 * @param startOffset The offset into the document at which
	 *        <code>text</code> starts.
	 * @return The first <code>Token</code> in a linked list representing
	 *         the syntax highlighted text.
	 */
	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

		resetTokenList();
		this.offsetShift = -text.offset + startOffset;
		jspInState = YYINITIAL; // Shouldn't be necessary

		// Start off in the proper state.
		int state = Token.NULL;
		switch (initialTokenType) {
			case Token.COMMENT_MULTILINE:
				state = COMMENT;
				start = text.offset;
				break;
			case Token.PREPROCESSOR:
				state = PI;
				start = text.offset;
				break;
			case Token.VARIABLE:
				state = DTD;
				start = text.offset;
				break;
			case INTERNAL_INTAG:
				state = INTAG;
				start = text.offset;
				break;
			case INTERNAL_INTAG_SCRIPT:
				state = INTAG_SCRIPT;
				start = text.offset;
				break;
			case INTERNAL_ATTR_DOUBLE:
				state = INATTR_DOUBLE;
				start = text.offset;
				break;
			case INTERNAL_ATTR_SINGLE:
				state = INATTR_SINGLE;
				start = text.offset;
				break;
			case INTERNAL_ATTR_DOUBLE_QUOTE_SCRIPT:
				state = INATTR_DOUBLE_SCRIPT;
				start = text.offset;
				break;
			case INTERNAL_ATTR_SINGLE_QUOTE_SCRIPT:
				state = INATTR_SINGLE_SCRIPT;
				start = text.offset;
				break;
			case INTERNAL_IN_HIDDEN_COMMENT:
				state = HIDDEN_COMMENT;
				start = text.offset;
				break;
			case INTERNAL_IN_JSP_DIRECTIVE:
				state = JSP_DIRECTIVE;
				start = text.offset;
				break;
			case INTERNAL_IN_JS:
				state = JAVASCRIPT;
				start = text.offset;
				break;
			case INTERNAL_IN_JS_MLC:
				state = JS_MLC;
				start = text.offset;
				break;
			default:
				if (initialTokenType<-1024) { // INTERNAL_IN_JAVAxxx - jspInState
					int main = -(-initialTokenType & 0xffffff00);
					switch (main) {
						default: // Should never happen
						case INTERNAL_IN_JAVA_DOCCOMMENT:
							state = JAVA_DOCCOMMENT;
							start = text.offset;
							break;
						case INTERNAL_IN_JAVA_EXPRESSION:
							state = JAVA_EXPRESSION;
							start = text.offset;
							break;
						case INTERNAL_IN_JAVA_MLC:
							state = JAVA_MLC;
							start = text.offset;
							break;
					}
					jspInState = -initialTokenType&0xff;
				}
				else {
					state = Token.NULL;
				}
				break;
		}

		s = text;
		try {
			yyreset(zzReader);
			yybegin(state);
			return yylex();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return new DefaultToken();
		}

	}


	/**
	 * Sets whether markup close tags should be completed.  You might not want
	 * this to be the case, since some tags in standard HTML aren't usually
	 * closed.
	 *
	 * @param complete Whether closing markup tags are completed.
	 * @see #getCompleteCloseTags()
	 */
	public static void setCompleteCloseTags(boolean complete) {
		completeCloseTags = complete;
	}


	/**
	 * Refills the input buffer.
	 *
	 * @return      <code>true</code> if EOF was reached, otherwise
	 *              <code>false</code>.
	 */
	private boolean zzRefill() {
		return zzCurrentPos>=s.offset+s.count;
	}


	/**
	 * Resets the scanner to read from a new input stream.
	 * Does not close the old reader.
	 *
	 * All internal variables are reset, the old input stream 
	 * <b>cannot</b> be reused (internal buffer is discarded and lost).
	 * Lexical state is set to <tt>YY_INITIAL</tt>.
	 *
	 * @param reader   the new input stream 
	 */
	public final void yyreset(java.io.Reader reader) {
		// 's' has been updated.
		zzBuffer = s.array;
		/*
		 * We replaced the line below with the two below it because zzRefill
		 * no longer "refills" the buffer (since the way we do it, it's always
		 * "full" the first time through, since it points to the segment's
		 * array).  So, we assign zzEndRead here.
		 */
		//zzStartRead = zzEndRead = s.offset;
		zzStartRead = s.offset;
		zzEndRead = zzStartRead + s.count - 1;
		zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;
		zzLexicalState = YYINITIAL;
		zzReader = reader;
		zzAtBOL  = true;
		zzAtEOF  = false;
	}


%}

// HTML-specific stuff.
Whitespace			= ([ \t\f])
LineTerminator			= ([\n])
Identifier			= ([^ \t\n<&]+)
AmperItem				= ([&][^; \t]*[;]?)
InTagIdentifier		= ([^ \t\n\"\'/=>]+)
UnclosedStringLiteral	= ([\"][^\"]*)
StringLiteral			= ({UnclosedStringLiteral}[\"])
UnclosedCharLiteral		= ([\'][^\']*)
CharLiteral			= ({UnclosedCharLiteral}[\'])
EndScriptTag			= ("</" [sS][cC][rR][iI][pP][tT] ">")

JspExpressionStart		= ("<%=")
JspScriptletStart		= ("<%")
JspDeclarationStart		= ("<%!")
JspStart				= ({JspExpressionStart}|{JspScriptletStart}|{JspDeclarationStart})

// Java stuff.
Letter							= [A-Za-z]
LetterOrUnderscore				= ({Letter}|"_")
NonzeroDigit						= [1-9]
Digit							= ("0"|{NonzeroDigit})
HexDigit							= ({Digit}|[A-Fa-f])
OctalDigit						= ([0-7])
AnyCharacterButApostropheOrBackSlash	= ([^\\'])
AnyCharacterButDoubleQuoteOrBackSlash	= ([^\\\"\n])
EscapedSourceCharacter				= ("u"{HexDigit}{HexDigit}{HexDigit}{HexDigit})
Escape							= ("\\"(([btnfr\"'\\])|([0123]{OctalDigit}?{OctalDigit}?)|({OctalDigit}{OctalDigit}?)|{EscapedSourceCharacter}))
NonSeparator						= ([^\t\f\r\n\ \(\)\{\}\[\]\;\,\.\=\>\<\!\~\?\:\+\-\*\/\&\|\^\%\"\']|"#"|"\\")
IdentifierStart					= ({LetterOrUnderscore}|"$")
IdentifierPart						= ({IdentifierStart}|{Digit}|("\\"{EscapedSourceCharacter}))
WhiteSpace				= ([ \t\f])
JCharLiteral				= ([\']({AnyCharacterButApostropheOrBackSlash}|{Escape})[\'])
JUnclosedCharLiteral			= ([\'][^\'\n]*)
JErrorCharLiteral			= ({UnclosedCharLiteral}[\'])
JStringLiteral				= ([\"]({AnyCharacterButDoubleQuoteOrBackSlash}|{Escape})*[\"])
JUnclosedStringLiteral		= ([\"]([\\].|[^\\\"])*[^\"]?)
JErrorStringLiteral			= ({UnclosedStringLiteral}[\"])
MLCBegin					= "/*"
MLCEnd					= "*/"
DocCommentBegin			= "/**"
LineCommentBegin			= "//"
IntegerHelper1				= (({NonzeroDigit}{Digit}*)|"0")
IntegerHelper2				= ("0"(([xX]{HexDigit}+)|({OctalDigit}*)))
IntegerLiteral				= ({IntegerHelper1}[lL]?)
HexLiteral				= ({IntegerHelper2}[lL]?)
FloatHelper1				= ([fFdD]?)
FloatHelper2				= ([eE][+-]?{Digit}+{FloatHelper1})
FloatLiteral1				= ({Digit}+"."({FloatHelper1}|{FloatHelper2}|{Digit}+({FloatHelper1}|{FloatHelper2})))
FloatLiteral2				= ("."{Digit}+({FloatHelper1}|{FloatHelper2}))
FloatLiteral3				= ({Digit}+{FloatHelper2})
FloatLiteral				= ({FloatLiteral1}|{FloatLiteral2}|{FloatLiteral3}|({Digit}+[fFdD]))
ErrorNumberFormat			= (({IntegerLiteral}|{HexLiteral}|{FloatLiteral}){NonSeparator}+)
BooleanLiteral				= ("true"|"false")
Separator					= ([\(\)\{\}\[\]])
Separator2				= ([\;,.])
NonAssignmentOperator		= ("+"|"-"|"<="|"^"|"++"|"<"|"*"|">="|"%"|"--"|">"|"/"|"!="|"?"|">>"|"!"|"&"|"=="|":"|">>"|"~"|"|"|"&&"|">>>")
AssignmentOperator			= ("="|"-="|"*="|"/="|"|="|"&="|"^="|"+="|"%="|"<<="|">>="|">>>=")
Operator					= ({NonAssignmentOperator}|{AssignmentOperator})
DocumentationKeyword		= ("author"|"deprecated"|"exception"|"link"|"param"|"return"|"see"|"serial"|"serialData"|"serialField"|"since"|"throws"|"version")
JIdentifier				= ({IdentifierStart}{IdentifierPart}*)
ErrorIdentifier			= ({NonSeparator}+)
Annotation				= ("@"{JIdentifier}?)
PrimitiveTypes				= ("boolean"|"byte"|"char"|"double" |"float"|"int"|"long"|"short")

CurrentBlockTag				= ("author"|"deprecated"|"exception"|"param"|"return"|"see"|"serial"|"serialData"|"serialField"|"since"|"throws"|"version")
ProposedBlockTag			= ("category"|"example"|"tutorial"|"index"|"exclude"|"todo"|"internal"|"obsolete"|"threadsafety")
BlockTag					= ({CurrentBlockTag}|{ProposedBlockTag})
InlineTag					= ("code"|"docRoot"|"inheritDoc"|"link"|"linkplain"|"literal"|"value")

URLGenDelim				= ([:\/\?#\[\]@])
URLSubDelim				= ([\!\$&'\(\)\*\+,;=])
URLUnreserved			= ({LetterOrUnderscore}|{Digit}|[\-\.\~])
URLCharacter			= ({URLGenDelim}|{URLSubDelim}|{URLUnreserved}|[%])
URLCharacters			= ({URLCharacter}*)
URLEndCharacter			= ([\/\$]|{Letter}|{Digit})
URL						= (((https?|f(tp|ile))"://"|"www.")({URLCharacters}{URLEndCharacter})?)

// JavaScript stuff.
JS_UnclosedCharLiteral		= ("'"({AnyCharacterButApostropheOrBackSlash}|{Escape})*)
JS_CharLiteral				= ({JS_UnclosedCharLiteral}"'")
JS_UnclosedErrorCharLiteral	= ([\']([^\'\n]|"\\'")*)
JS_ErrorCharLiteral			= ({JS_UnclosedErrorCharLiteral}[\'])
JS_UnclosedStringLiteral	= ([\"]({AnyCharacterButDoubleQuoteOrBackSlash}|{Escape})*)
JS_StringLiteral			= ({JS_UnclosedStringLiteral}[\"])
JS_UnclosedErrorStringLiteral	= ([\"]([^\"\n]|"\\\"")*)
JS_ErrorStringLiteral		= ({JS_UnclosedErrorStringLiteral}[\"])
JS_MLCBegin				= ({MLCBegin})
JS_MLCEnd					= ({MLCEnd})
JS_LineCommentBegin			= ({LineCommentBegin})
JS_IntegerLiteral			= ({IntegerLiteral})
JS_HexLiteral				= ({HexLiteral})
JS_FloatLiteral			= ({FloatLiteral})
JS_ErrorNumberFormat		= ({ErrorNumberFormat})
JS_Separator				= ({Separator})
JS_Separator2				= ({Separator2})
JS_Operator				= ({Operator})
JS_Identifier				= ({JIdentifier})
JS_ErrorIdentifier			= ({ErrorIdentifier})


%state COMMENT
%state PI
%state DTD
%state INTAG
%state INTAG_CHECK_TAG_NAME
%state INATTR_DOUBLE
%state INATTR_SINGLE
%state INTAG_SCRIPT
%state INATTR_DOUBLE_SCRIPT
%state INATTR_SINGLE_SCRIPT
%state JAVASCRIPT
%state JS_MLC
%state HIDDEN_COMMENT
%state JAVA_DOCCOMMENT
%state JAVA_EXPRESSION
%state JAVA_MLC
%state JSP_DIRECTIVE


%%

<YYINITIAL> {
	"<!--"					{ start = zzMarkedPos-4; yybegin(COMMENT); }
	"<script"					{
							  addToken(zzStartRead,zzStartRead, Token.MARKUP_TAG_DELIMITER);
							  addToken(zzMarkedPos-6,zzMarkedPos-1, Token.MARKUP_TAG_NAME);
							  start = zzMarkedPos; yybegin(INTAG_SCRIPT);
							}
	"<!"						{ start = zzMarkedPos-2; yybegin(DTD); }
	"<?"						{ start = zzMarkedPos-2; yybegin(PI); }
	"<%--"					{ start = zzMarkedPos-4; yybegin(HIDDEN_COMMENT); }
	{JspStart}				{ addToken(Token.SEPARATOR); jspInState = zzLexicalState; yybegin(JAVA_EXPRESSION); }
	"<%@"                         { addToken(Token.SEPARATOR); yybegin(JSP_DIRECTIVE); }
	"<"({Letter}|{Digit})+		{
									int count = yylength();
									addToken(zzStartRead,zzStartRead, Token.MARKUP_TAG_DELIMITER);
									zzMarkedPos -= (count-1); //yypushback(count-1);
									yybegin(INTAG_CHECK_TAG_NAME);
								}
	"</"({Letter}|{Digit})+		{
									int count = yylength();
									addToken(zzStartRead,zzStartRead+1, Token.MARKUP_TAG_DELIMITER);
									zzMarkedPos -= (count-2); //yypushback(count-2);
									yybegin(INTAG_CHECK_TAG_NAME);
								}
	"<"							{ addToken(Token.MARKUP_TAG_DELIMITER); yybegin(INTAG); }
	"</"						{ addToken(Token.MARKUP_TAG_DELIMITER); yybegin(INTAG); }
	{LineTerminator}			{ addNullToken(); return firstToken; }
	{Identifier}				{ addToken(Token.IDENTIFIER); } // Catches everything.
	{AmperItem}				{ addToken(Token.DATA_TYPE); }
	{Whitespace}+				{ addToken(Token.WHITESPACE); }
	<<EOF>>					{ addNullToken(); return firstToken; }
}

<COMMENT> {
	[^\n\-]+					{}
	{LineTerminator}			{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }
	"-->"					{ yybegin(YYINITIAL); addToken(start,zzStartRead+2, Token.COMMENT_MULTILINE); }
	"-"						{}
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }
}

<HIDDEN_COMMENT> {
	[^\n\-]+					{}
	{LineTerminator}			{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addEndToken(INTERNAL_IN_HIDDEN_COMMENT); return firstToken; }
	"--%>"					{ yybegin(YYINITIAL); addToken(start,zzStartRead+3, Token.COMMENT_MULTILINE); }
	"-"						{}
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addEndToken(INTERNAL_IN_HIDDEN_COMMENT); return firstToken; }
}

<PI> {
	[^\n\?]+					{}
	{LineTerminator}			{ addToken(start,zzStartRead-1, Token.PREPROCESSOR); return firstToken; }
	"?>"						{ yybegin(YYINITIAL); addToken(start,zzStartRead+1, Token.PREPROCESSOR); }
	"?"						{}
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.PREPROCESSOR); return firstToken; }
}

<DTD> {
	[^\n>]+					{}
	{LineTerminator}			{ addToken(start,zzStartRead-1, Token.VARIABLE); return firstToken; }
	">"						{ yybegin(YYINITIAL); addToken(start,zzStartRead, Token.VARIABLE); }
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.VARIABLE); return firstToken; }
}

<INTAG_CHECK_TAG_NAME> {
	[Aa] |
	[aA][bB][bB][rR] |
	[aA][cC][rR][oO][nN][yY][mM] |
	[aA][dD][dD][rR][eE][sS][sS] |
	[aA][pP][pP][lL][eE][tT] |
	[aA][rR][eE][aA] |
	[aA][rR][tT][iI][cC][lL][eE] |
	[aA][sS][iI][dD][eE] |
	[aA][uU][dD][iI][oO] |
	[bB] |
	[bB][aA][sS][eE] |
	[bB][aA][sS][eE][fF][oO][nN][tT] |
	[bB][dD][oO] |
	[bB][gG][sS][oO][uU][nN][dD] |
	[bB][iI][gG] |
	[bB][lL][iI][nN][kK] |
	[bB][lL][oO][cC][kK][qQ][uU][oO][tT][eE] |
	[bB][oO][dD][yY] |
	[bB][rR] |
	[bB][uU][tT][tT][oO][nN] |
	[cC][aA][nN][vV][aA][sS] |
	[cC][aA][pP][tT][iI][oO][nN] |
	[cC][eE][nN][tT][eE][rR] |
	[cC][iI][tT][eE] |
	[cC][oO][dD][eE] |
	[cC][oO][lL] |
	[cC][oO][lL][gG][rR][oO][uU][pP] |
	[cC][oO][mM][mM][aA][nN][dD] |
	[cC][oO][mM][mM][eE][nN][tT] |
	[dD][dD] |
	[dD][aA][tT][aA][gG][rR][iI][dD] |
	[dD][aA][tT][aA][lL][iI][sS][tT] |
	[dD][aA][tT][aA][tT][eE][mM][pP][lL][aA][tT][eE] |
	[dD][eE][lL] |
	[dD][eE][tT][aA][iI][lL][sS] |
	[dD][fF][nN] |
	[dD][iI][aA][lL][oO][gG] |
	[dD][iI][rR] |
	[dD][iI][vV] |
	[dD][lL] |
	[dD][tT] |
	[eE][mM] |
	[eE][mM][bB][eE][dD] |
	[eE][vV][eE][nN][tT][sS][oO][uU][rR][cC][eE] |
	[fF][iI][eE][lL][dD][sS][eE][tT] |
	[fF][iI][gG][uU][rR][eE] |
	[fF][oO][nN][tT] |
	[fF][oO][oO][tT][eE][rR] |
	[fF][oO][rR][mM] |
	[fF][rR][aA][mM][eE] |
	[fF][rR][aA][mM][eE][sS][eE][tT] |
	[hH][123456] |
	[hH][eE][aA][dD] |
	[hH][eE][aA][dD][eE][rR] |
	[hH][rR] |
	[hH][tT][mM][lL] |
	[iI] |
	[iI][fF][rR][aA][mM][eE] |
	[iI][lL][aA][yY][eE][rR] |
	[iI][mM][gG] |
	[iI][nN][pP][uU][tT] |
	[iI][nN][sS] |
	[iI][sS][iI][nN][dD][eE][xX] |
	[kK][bB][dD] |
	[kK][eE][yY][gG][eE][nN] |
	[lL][aA][bB][eE][lL] |
	[lL][aA][yY][eE][rR] |
	[lL][eE][gG][eE][nN][dD] |
	[lL][iI] |
	[lL][iI][nN][kK] |
	[mM][aA][pP] |
	[mM][aA][rR][kK] |
	[mM][aA][rR][qQ][uU][eE][eE] |
	[mM][eE][nN][uU] |
	[mM][eE][tT][aA] |
	[mM][eE][tT][eE][rR] |
	[mM][uU][lL][tT][iI][cC][oO][lL] |
	[nN][aA][vV] |
	[nN][eE][sS][tT] |
	[nN][oO][bB][rR] |
	[nN][oO][eE][mM][bB][eE][dD] |
	[nN][oO][fF][rR][aA][mM][eE][sS] |
	[nN][oO][lL][aA][yY][eE][rR] |
	[nN][oO][sS][cC][rR][iI][pP][tT] |
	[oO][bB][jJ][eE][cC][tT] |
	[oO][lL] |
	[oO][pP][tT][gG][rR][oO][uU][pP] |
	[oO][pP][tT][iI][oO][nN] |
	[oO][uU][tT][pP][uU][tT] |
	[pP] |
	[pP][aA][rR][aA][mM] |
	[pP][lL][aA][iI][nN][tT][eE][xX][tT] |
	[pP][rR][eE] |
	[pP][rR][oO][gG][rR][eE][sS][sS] |
	[qQ] |
	[rR][uU][lL][eE] |
	[sS] |
	[sS][aA][mM][pP] |
	[sS][cC][rR][iI][pP][tT] |
	[sS][eE][cC][tT][iI][oO][nN] |
	[sS][eE][lL][eE][cC][tT] |
	[sS][eE][rR][vV][eE][rR] |
	[sS][mM][aA][lL][lL] |
	[sS][oO][uU][rR][cC][eE] |
	[sS][pP][aA][cC][eE][rR] |
	[sS][pP][aA][nN] |
	[sS][tT][rR][iI][kK][eE] |
	[sS][tT][rR][oO][nN][gG] |
	[sS][tT][yY][lL][eE] |
	[sS][uU][bB] |
	[sS][uU][pP] |
	[tT][aA][bB][lL][eE] |
	[tT][bB][oO][dD][yY] |
	[tT][dD] |
	[tT][eE][xX][tT][aA][rR][eE][aA] |
	[tT][fF][oO][oO][tT] |
	[tT][hH] |
	[tT][hH][eE][aA][dD] |
	[tT][iI][mM][eE] |
	[tT][iI][tT][lL][eE] |
	[tT][rR] |
	[tT][tT] |
	[uU] |
	[uU][lL] |
	[vV][aA][rR] |
	[vV][iI][dD][eE][oO]    { addToken(Token.MARKUP_TAG_NAME); }
	{InTagIdentifier}		{ /* A non-recognized HTML tag name */ yypushback(yylength()); yybegin(INTAG); }
	.						{ /* Shouldn't happen */ yypushback(1); yybegin(INTAG); }
	<<EOF>>					{ addToken(zzMarkedPos,zzMarkedPos, INTERNAL_INTAG); return firstToken; }
}

<INTAG> {
	{JspStart}				{ addToken(Token.SEPARATOR); jspInState = zzLexicalState; yybegin(JAVA_EXPRESSION); }
	"/"						{ addToken(Token.MARKUP_TAG_DELIMITER); }
	{InTagIdentifier}			{ addToken(Token.MARKUP_TAG_ATTRIBUTE); }
	{Whitespace}				{ addToken(Token.WHITESPACE); }
	"="						{ addToken(Token.OPERATOR); }
	"/>"						{ yybegin(YYINITIAL); addToken(Token.MARKUP_TAG_DELIMITER); }
	">"						{ yybegin(YYINITIAL); addToken(Token.MARKUP_TAG_DELIMITER); }
	[\"]						{ start = zzMarkedPos-1; yybegin(INATTR_DOUBLE); }
	[\']						{ start = zzMarkedPos-1; yybegin(INATTR_SINGLE); }
	<<EOF>>					{ addToken(zzMarkedPos,zzMarkedPos, INTERNAL_INTAG); return firstToken; }
}

<INATTR_DOUBLE> {
	{JspStart}				{ int temp=zzStartRead; if (zzStartRead>start) addToken(start,zzStartRead-1, Token.MARKUP_TAG_ATTRIBUTE_VALUE); addToken(temp, zzMarkedPos-1, Token.SEPARATOR); jspInState = zzLexicalState; yybegin(JAVA_EXPRESSION); }
	[^\"<]*					{}
	"<"						{ /* Allowing JSP expressions, etc. */ }
	[\"]					{ addToken(start,zzStartRead, Token.MARKUP_TAG_ATTRIBUTE_VALUE); yybegin(INTAG); }
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.MARKUP_TAG_ATTRIBUTE_VALUE); addEndToken(INTERNAL_ATTR_DOUBLE); return firstToken; }
}

<INATTR_SINGLE> {
	{JspStart}				{ int temp=zzStartRead; if (zzStartRead>start) addToken(start,zzStartRead-1, Token.MARKUP_TAG_ATTRIBUTE_VALUE); addToken(temp, zzMarkedPos-1, Token.SEPARATOR); jspInState = zzLexicalState; yybegin(JAVA_EXPRESSION); }
	[^\'<]*					{}
	"<"						{ /* Allowing JSP expressions, etc. */ }
	[\']					{ addToken(start,zzStartRead, Token.MARKUP_TAG_ATTRIBUTE_VALUE); yybegin(INTAG); }
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.MARKUP_TAG_ATTRIBUTE_VALUE); addEndToken(INTERNAL_ATTR_SINGLE); return firstToken; }
}

<INTAG_SCRIPT> {
	{JspStart}				{ addToken(Token.SEPARATOR); jspInState = zzLexicalState; yybegin(JAVA_EXPRESSION); }
	{InTagIdentifier}			{ addToken(Token.MARKUP_TAG_ATTRIBUTE); }
	"/>"					{	addToken(Token.MARKUP_TAG_DELIMITER); yybegin(YYINITIAL); }
	"/"						{ addToken(Token.MARKUP_TAG_DELIMITER); } // Won't appear in valid HTML.
	{Whitespace}+				{ addToken(Token.WHITESPACE); }
	"="						{ addToken(Token.OPERATOR); }
	">"						{ yybegin(JAVASCRIPT); addToken(Token.MARKUP_TAG_DELIMITER); }
	[\"]						{ start = zzMarkedPos-1; yybegin(INATTR_DOUBLE_SCRIPT); }
	[\']						{ start = zzMarkedPos-1; yybegin(INATTR_SINGLE_SCRIPT); }
	<<EOF>>					{ addToken(zzMarkedPos,zzMarkedPos, INTERNAL_INTAG_SCRIPT); return firstToken; }
}

<INATTR_DOUBLE_SCRIPT> {
	{JspStart}				{ int temp=zzStartRead; if (zzStartRead>start) addToken(start,zzStartRead-1, Token.MARKUP_TAG_ATTRIBUTE_VALUE); addToken(temp, zzMarkedPos-1, Token.SEPARATOR); jspInState = zzLexicalState; yybegin(JAVA_EXPRESSION); }
	[^\"<]*					{}
	"<"						{ /* Allowing JSP expressions, etc. */ }
	[\"]					{ yybegin(INTAG_SCRIPT); addToken(start,zzStartRead, Token.MARKUP_TAG_ATTRIBUTE_VALUE); }
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.MARKUP_TAG_ATTRIBUTE_VALUE); addEndToken(INTERNAL_ATTR_DOUBLE_QUOTE_SCRIPT); return firstToken; }
}

<INATTR_SINGLE_SCRIPT> {
	{JspStart}				{ int temp=zzStartRead; if (zzStartRead>start) addToken(start,zzStartRead-1, Token.MARKUP_TAG_ATTRIBUTE_VALUE); addToken(temp, zzMarkedPos-1, Token.SEPARATOR); jspInState = zzLexicalState; yybegin(JAVA_EXPRESSION); }
	[^\'<]*					{}
	"<"						{ /* Allowing JSP expressions, etc. */ }
	[\']					{ yybegin(INTAG_SCRIPT); addToken(start,zzStartRead, Token.MARKUP_TAG_ATTRIBUTE_VALUE); }
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.MARKUP_TAG_ATTRIBUTE_VALUE); addEndToken(INTERNAL_ATTR_SINGLE_QUOTE_SCRIPT); return firstToken; }
}

<JAVASCRIPT> {

	{EndScriptTag}					{
								  yybegin(YYINITIAL);
								  addToken(zzStartRead,zzStartRead+1, Token.MARKUP_TAG_DELIMITER);
								  addToken(zzMarkedPos-7,zzMarkedPos-2, Token.MARKUP_TAG_NAME);
								  addToken(zzMarkedPos-1,zzMarkedPos-1, Token.MARKUP_TAG_DELIMITER);
								}

	// ECMA keywords.
	"break" |
	"continue" |
	"delete" |
	"else" |
	"for" |
	"function" |
	"if" |
	"in" |
	"new" |
	"return" |
	"this" |
	"typeof" |
	"var" |
	"void" |
	"while" |
	"with"						{ addToken(Token.RESERVED_WORD); }

	// Reserved (but not yet used) ECMA keywords.
	"abstract" |
	"case" |
	"catch" |
	"class" |
	"const" |
	"debugger" |
	"default" |
	"do" |
	"enum" |
	"export" |
	"extends" |
	"final" |
	"finally" |
	"goto" |
	"implements" |
	"import" |
	"instanceof" |
	"interface" |
	"native" |
	"package" |
	"private" |
	"protected" |
	"public" |
	"static" |
	"super" |
	"switch" |
	"synchronized" |
	"throw" |
	"throws" |
	"transient" |
	"try" |
	"volatile" |
	"null"						{ addToken(Token.RESERVED_WORD); }
	{PrimitiveTypes}				{ addToken(Token.DATA_TYPE); }

	// Literals.
	{BooleanLiteral}				{ addToken(Token.LITERAL_BOOLEAN); }
	"NaN"						{ addToken(Token.RESERVED_WORD); }
	"Infinity"					{ addToken(Token.RESERVED_WORD); }

	// Functions.
	"eval" |
	"parseInt" |
	"parseFloat" |
	"escape" |
	"unescape" |
	"isNaN" |
	"isFinite"					{ addToken(Token.FUNCTION); }

	{LineTerminator}				{ addEndToken(INTERNAL_IN_JS); return firstToken; }
	{JS_Identifier}				{ addToken(Token.IDENTIFIER); }
	{Whitespace}+					{ addToken(Token.WHITESPACE); }

	/* String/Character literals. */
	{JS_CharLiteral}				{ addToken(Token.LITERAL_CHAR); }
	{JS_UnclosedCharLiteral}			{ addToken(Token.ERROR_CHAR); }
	{JS_UnclosedErrorCharLiteral}		{ addToken(Token.ERROR_CHAR); addEndToken(INTERNAL_IN_JS); return firstToken; }
	{JS_ErrorCharLiteral}			{ addToken(Token.ERROR_CHAR); }
	{JS_StringLiteral}				{ addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }
	{JS_UnclosedStringLiteral}		{ addToken(Token.ERROR_STRING_DOUBLE); addEndToken(INTERNAL_IN_JS); return firstToken; }
	{JS_UnclosedErrorStringLiteral}	{ addToken(Token.ERROR_STRING_DOUBLE); addEndToken(INTERNAL_IN_JS); return firstToken; }
	{JS_ErrorStringLiteral}			{ addToken(Token.ERROR_STRING_DOUBLE); }

	/* Comment literals. */
	"/**/"						{ addToken(Token.COMMENT_MULTILINE); }
	{JS_MLCBegin}					{ start = zzMarkedPos-2; yybegin(JS_MLC); }
	{JS_LineCommentBegin}.*			{ addToken(Token.COMMENT_EOL); addEndToken(INTERNAL_IN_JS); return firstToken; }

	/* Separators. */
	{JS_Separator}					{ addToken(Token.SEPARATOR); }
	{JS_Separator2}				{ addToken(Token.IDENTIFIER); }

	{JspStart}				{ addToken(Token.SEPARATOR); jspInState = zzLexicalState; yybegin(JAVA_EXPRESSION); }

	/* Operators. */
	{JS_Operator}					{ addToken(Token.OPERATOR); }

	/* Numbers */
	{JS_IntegerLiteral}				{ addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
	{JS_HexLiteral}				{ addToken(Token.LITERAL_NUMBER_HEXADECIMAL); }
	{JS_FloatLiteral}				{ addToken(Token.LITERAL_NUMBER_FLOAT); }
	{JS_ErrorNumberFormat}			{ addToken(Token.ERROR_NUMBER_FORMAT); }

	{JS_ErrorIdentifier}			{ addToken(Token.ERROR_IDENTIFIER); }

	/* Ended with a line not in a string or comment. */
	<<EOF>>						{ addEndToken(INTERNAL_IN_JS); return firstToken; }

	/* Catch any other (unhandled) characters and flag them as bad. */
	.							{ addToken(Token.ERROR_IDENTIFIER); }

}


<JS_MLC> {
	// JavaScript MLC's.  This state is essentially Java's MLC state.
	[^\n\*]+						{}
	\n							{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addEndToken(INTERNAL_IN_JS_MLC); return firstToken; }
	{JS_MLCEnd}					{ yybegin(JAVASCRIPT); addToken(start,zzStartRead+1, Token.COMMENT_MULTILINE); }
	\*							{}
	<<EOF>>						{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addEndToken(INTERNAL_IN_JS_MLC); return firstToken; }
}


<JAVA_EXPRESSION> {

	"%>"							{ addToken(Token.SEPARATOR); start = zzMarkedPos; yybegin(jspInState); }

	/* Keywords */
	"abstract"|
	"assert" |
	"break"	 |
	"case"	 |
	"catch"	 |
	"class"	 |
	"const"	 |
	"continue" |
	"default" |
	"do"	 |
	"else"	 |
	"enum"	 |
	"extends" |
	"final"	 |
	"finally" |
	"for"	 |
	"goto"	 |
	"if"	 |
	"implements" |
	"import" |
	"instanceof" |
	"interface" |
	"native" |
	"new"	 |
	"null"	 |
	"package" |
	"private" |
	"protected" |
	"public" |
	"static" |
	"strictfp" |
	"super"	 |
	"switch" |
	"synchronized" |
	"this"	 |
	"throw"	 |
	"throws" |
	"transient" |
	"try"	 |
	"void"	 |
	"volatile" |
	"while"					{ addToken(Token.RESERVED_WORD); }
	"return"				{ addToken(Token.RESERVED_WORD_2); }

	/* Data types. */
	{PrimitiveTypes}			{ addToken(Token.DATA_TYPE); }

	/* Booleans. */
	{BooleanLiteral}			{ addToken(Token.LITERAL_BOOLEAN); }

	/* java.lang classes */
	"Appendable" |
	"AutoCloseable" |
	"CharSequence" |
	"Cloneable" |
	"Comparable" |
	"Iterable" |
	"Readable" |
	"Runnable" |
	"Thread.UncaughtExceptionHandler" |
	"Boolean" |
	"Byte" |
	"Character" |
	"Character.Subset" | 
	"Character.UnicodeBlock" | 
	"Class" |
	"ClassLoader" |
	"Compiler" |
	"Double" |
	"Enum" |
	"Float" |
	"InheritableThreadLocal" |
	"Integer" |
	"Long" |
	"Math" |
	"Number" |
	"Object" |
	"Package" |
	"Process" |
	"ProcessBuilder" |
	"ProcessBuilder.Redirect" |
	"Runtime" |
	"RuntimePermission" |
	"SecurityManager" |
	"Short" |
	"StackTraceElement" |
	"StrictMath" |
	"String" |
	"StringBuffer" |
	"StringBuilder" |
	"System" |
	"Thread" |
	"ThreadGroup" |
	"ThreadLocal" |
	"Throwable" |
	"Void" |
	"Character.UnicodeScript" |
	"ProcessBuilder.Redirect.Type" |
	"Thread.State" |
	"ArithmeticException" |
	"ArrayIndexOutOfBoundsException" |
	"ArrayStoreException" |
	"ClassCastException" |
	"ClassNotFoundException" |
	"CloneNotSupportedException" |
	"EnumConstantNotPresentException" |
	"Exception" |
	"IllegalAccessException" |
	"IllegalArgumentException" |
	"IllegalMonitorStateException" |
	"IllegalStateException" |
	"IllegalThreadStateException" |
	"IndexOutOfBoundsException" |
	"InstantiationException" |
	"InterruptedException" |
	"NegativeArraySizeException" |
	"NoSuchFieldException" |
	"NoSuchMethodException" |
	"NullPointerException" |
	"NumberFormatException" |
	"RuntimeException" |
	"SecurityException" |
	"StringIndexOutOfBoundsException" |
	"TypeNotPresentException" |
	"UnsupportedOperationException" |
	"AbstractMethodError" |
	"AssertionError" |
	"ClassCircularityError" |
	"ClassFormatError" |
	"Error" |
	"ExceptionInInitializerError" |
	"IllegalAccessError" |
	"IncompatibleClassChangeError" |
	"InstantiationError" |
	"InternalError" |
	"LinkageError" |
	"NoClassDefFoundError" |
	"NoSuchFieldError" |
	"NoSuchMethodError" |
	"OutOfMemoryError" |
	"StackOverflowError" |
	"ThreadDeath" |
	"UnknownError" |
	"UnsatisfiedLinkError" |
	"UnsupportedClassVersionError" |
	"VerifyError" |
	"VirtualMachineError" 			{ addToken(Token.FUNCTION); }

	{LineTerminator}				{ addEndToken(INTERNAL_IN_JAVA_EXPRESSION - jspInState); return firstToken; }

	{JIdentifier}					{ addToken(Token.IDENTIFIER); }

	{WhiteSpace}+					{ addToken(Token.WHITESPACE); }

	/* String/Character literals. */
	{JCharLiteral}					{ addToken(Token.LITERAL_CHAR); }
	{JUnclosedCharLiteral}			{ addToken(Token.ERROR_CHAR); addEndToken(INTERNAL_IN_JAVA_EXPRESSION - jspInState); return firstToken; }
	{JErrorCharLiteral}				{ addToken(Token.ERROR_CHAR); }
	{JStringLiteral}				{ addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }
	{JUnclosedStringLiteral}			{ addToken(Token.ERROR_STRING_DOUBLE); addEndToken(INTERNAL_IN_JAVA_EXPRESSION - jspInState); return firstToken; }
	{JErrorStringLiteral}			{ addToken(Token.ERROR_STRING_DOUBLE); }

	/* Comment literals. */
	"/**/"						{ addToken(Token.COMMENT_MULTILINE); }
	{MLCBegin}					{ start = zzMarkedPos-2; yybegin(JAVA_MLC); }
	{DocCommentBegin}				{ start = zzMarkedPos-3; yybegin(JAVA_DOCCOMMENT); }
	{LineCommentBegin}.*			{ addToken(Token.COMMENT_EOL); addEndToken(INTERNAL_IN_JAVA_EXPRESSION - jspInState); return firstToken; }

	/* Annotations. */
	{Annotation}					{ addToken(Token.ANNOTATION); }

	/* Separators. */
	{Separator}					{ addToken(Token.SEPARATOR); }
	{Separator2}					{ addToken(Token.IDENTIFIER); }

	/* Operators. */
	{Operator}					{ addToken(Token.OPERATOR); }

	/* Numbers */
	{IntegerLiteral}				{ addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
	{HexLiteral}					{ addToken(Token.LITERAL_NUMBER_HEXADECIMAL); }
	{FloatLiteral}					{ addToken(Token.LITERAL_NUMBER_FLOAT); }
	{ErrorNumberFormat}				{ addToken(Token.ERROR_NUMBER_FORMAT); }

	{ErrorIdentifier}				{ addToken(Token.ERROR_IDENTIFIER); }

	/* Ended with a line not in a string or comment. */
	<<EOF>>						{ addEndToken(INTERNAL_IN_JAVA_EXPRESSION - jspInState); return firstToken; }

	/* Catch any other (unhandled) characters and flag them as bad. */
	.							{ addToken(Token.ERROR_IDENTIFIER); }

}


<JAVA_MLC> {
	[^hwf\n\*]+				{}
	{URL}					{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addHyperlinkToken(temp,zzMarkedPos-1, Token.COMMENT_MULTILINE); start = zzMarkedPos; }
	[hwf]					{}
	\n						{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addEndToken(INTERNAL_IN_JAVA_MLC - jspInState); return firstToken; }
	{MLCEnd}					{ yybegin(JAVA_EXPRESSION); addToken(start,zzStartRead+1, Token.COMMENT_MULTILINE); }
	\*						{}
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addEndToken(INTERNAL_IN_JAVA_MLC - jspInState); return firstToken; }
}


<JAVA_DOCCOMMENT> {

	[^hwf\@\{\n\<\*]+			{}
	{URL}						{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); addHyperlinkToken(temp,zzMarkedPos-1, Token.COMMENT_DOCUMENTATION); start = zzMarkedPos; }
	[hwf]						{}

	"@"{BlockTag}				{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); addToken(temp,zzMarkedPos-1, Token.COMMENT_KEYWORD); start = zzMarkedPos; }
	"@"							{}
	"{@"{InlineTag}[^\}]*"}"	{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); addToken(temp,zzMarkedPos-1, Token.COMMENT_KEYWORD); start = zzMarkedPos; }
	"{"							{}
	\n							{ addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); addEndToken(INTERNAL_IN_JAVA_DOCCOMMENT - jspInState); return firstToken; }
	"<"[/]?({Letter}[^\>]*)?">"	{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); addToken(temp,zzMarkedPos-1, Token.COMMENT_MARKUP); start = zzMarkedPos; }
	\<							{}
	{MLCEnd}					{ yybegin(JAVA_EXPRESSION); addToken(start,zzStartRead+1, Token.COMMENT_DOCUMENTATION); }
	\*							{}
	<<EOF>>						{ yybegin(JAVA_EXPRESSION); addToken(start,zzEndRead, Token.COMMENT_DOCUMENTATION); addEndToken(INTERNAL_IN_JAVA_DOCCOMMENT - jspInState); return firstToken; }

}


<JSP_DIRECTIVE> {
	"include" |
	"page" |
	"taglib"					{ addToken(Token.RESERVED_WORD); }
	"/"						{ addToken(Token.RESERVED_WORD); }
	{InTagIdentifier}			{ addToken(Token.IDENTIFIER); }
	{Whitespace}+				{ addToken(Token.WHITESPACE); }
	"="						{ addToken(Token.OPERATOR); }
	"%>"						{ yybegin(YYINITIAL); addToken(Token.SEPARATOR); }
	"%"						{ addToken(Token.IDENTIFIER); }
	">"						{ addToken(Token.IDENTIFIER); /* Needed as InTagIdentifier ignores it. */ }
	{UnclosedStringLiteral}		{ addToken(Token.ERROR_STRING_DOUBLE); }
	{StringLiteral}			{ addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }
	{UnclosedCharLiteral}		{ addToken(Token.ERROR_CHAR); }
	{CharLiteral}				{ addToken(Token.LITERAL_CHAR); }
	<<EOF>>					{ addToken(zzMarkedPos,zzMarkedPos, INTERNAL_IN_JSP_DIRECTIVE); return firstToken; }
}

