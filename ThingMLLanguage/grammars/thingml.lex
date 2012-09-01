package thingmlparser;
import java_cup.runtime.*;
%%

%class Lexer
%unicode
%cup
%line
%column
%public
%{
  StringBuffer string = new StringBuffer();
  
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
  
%}
LineTerminator          = \r|\n|\r\n
WhiteSpace		= {LineTerminator} | [ \t\f]
Identifier              = [:jletter:] [:jletterdigit:]*
EndOfLineComment        = "//" [^\r\n]* {LineTerminator}
DecIntegerLiteral       = 0 | [1-9][0-9]*

%state STRING

%%
    
<YYINITIAL>{
  {WhiteSpace}                    	{}
  {EndOfLineComment}              	{}
  "StateMachine"                  	{ return symbol(sym.STATEMACHINE); }
  "init"                          	{ return symbol(sym.INIT); }
  "final"                         	{ return symbol(sym.FINAL); }
  "State"                         	{ return symbol(sym.STATE); }
  
  /* operators */
  "->"                          	{ return symbol(sym.TRANS); }

  "{"			         	{ return symbol(sym.LBRACK); }
  "}"					{ return symbol(sym.RBRACK); }
  "("                             	{ return symbol(sym.LPAR); }
  ")"                            	{ return symbol(sym.RPAR); }

  {Identifier}                    	{ return symbol(sym.ID, yytext()); }
  {DecIntegerLiteral}             	{ return symbol(sym.INT_LITERAL, yytext()); }
}

  /* error */
  .     		                { throw new Error("Illegal character '" + yytext() + "' at line " + yyline + ", column " + yycolumn + "."); }
  
