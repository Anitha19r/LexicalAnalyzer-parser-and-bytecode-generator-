package cop5556sp17;

import static cop5556sp17.Scanner.Kind.AND;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.ASSIGN;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.COMMA;
import static cop5556sp17.Scanner.Kind.DIV;
import static cop5556sp17.Scanner.Kind.EOF;
import static cop5556sp17.Scanner.Kind.EQUAL;
import static cop5556sp17.Scanner.Kind.GE;
import static cop5556sp17.Scanner.Kind.GT;
import static cop5556sp17.Scanner.Kind.IDENT;
import static cop5556sp17.Scanner.Kind.KW_BOOLEAN;
import static cop5556sp17.Scanner.Kind.KW_FILE;
import static cop5556sp17.Scanner.Kind.KW_FRAME;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_IF;
import static cop5556sp17.Scanner.Kind.KW_IMAGE;
import static cop5556sp17.Scanner.Kind.KW_INTEGER;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SCALE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_URL;
import static cop5556sp17.Scanner.Kind.KW_WHILE;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.LE;
import static cop5556sp17.Scanner.Kind.LPAREN;
import static cop5556sp17.Scanner.Kind.LT;
import static cop5556sp17.Scanner.Kind.MINUS;
import static cop5556sp17.Scanner.Kind.MOD;
import static cop5556sp17.Scanner.Kind.NOTEQUAL;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_SLEEP;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.OR;
import static cop5556sp17.Scanner.Kind.PLUS;
import static cop5556sp17.Scanner.Kind.RPAREN;
import static cop5556sp17.Scanner.Kind.SEMI;
import static cop5556sp17.Scanner.Kind.TIMES;

import java.util.ArrayList;
import java.util.HashMap;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;
	ArrayList<Kind> statementKind;
	HashMap<Kind,String> operations;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		statementKind = new ArrayList<Kind> ();
		statementKind.add(OP_SLEEP);
		statementKind.add(OP_BLUR);
		statementKind.add(OP_GRAY);
		statementKind.add(OP_CONVOLVE);
		statementKind.add(KW_SHOW);
		statementKind.add(KW_HIDE);
		statementKind.add(KW_MOVE);
		statementKind.add(KW_XLOC);
		statementKind.add(KW_YLOC);
		statementKind.add(OP_WIDTH);
		statementKind.add(OP_HEIGHT);
		statementKind.add(KW_SCALE);
		statementKind.add(IDENT);
		statementKind.add(KW_WHILE);
		statementKind.add(KW_IF);
		operations = new HashMap<Kind,String> ();
		operations.put(IDENT,"chain");
		operations.put(LT, "relOp");
		operations.put(GT, "relOp");
		operations.put(GE, "relOp");
		operations.put(LE, "relOp");
		operations.put(EQUAL, "relOp");
		operations.put(NOTEQUAL, "relOp");
		operations.put(PLUS, "weakOp");
		operations.put(MINUS, "weakOp");
		operations.put(OR, "weakOp");
		operations.put(TIMES, "strongOp");
		operations.put(DIV, "strongOp");
		operations.put(AND, "strongOp");
		operations.put(MOD, "strongOp");
		operations.put(OP_BLUR,"filter");
		operations.put(OP_GRAY,"filter");
		operations.put(OP_CONVOLVE,"filter");
		operations.put(KW_SHOW,"frame");
		operations.put(KW_HIDE,"frame");
		operations.put(KW_MOVE,"frame");
		operations.put(KW_XLOC,"frame");
		operations.put(KW_YLOC,"frame");
		operations.put(OP_WIDTH,"image");
		operations.put(OP_HEIGHT,"image");
		operations.put(KW_SCALE,"image");
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		ASTNode p=program();
		matchEOF();	
		return p;
	}

	

	Expression factor() throws SyntaxException {
		Token t1= t;
		Expression e=null;
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			e= new IdentExpression(t1);
			consume();
		}
			break;
		case INT_LIT: {
			e= new IntLitExpression(t1);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e= new BooleanLitExpression(t1);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e= new ConstantExpression(t1);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e=expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		return e;
	}

	Block block() throws SyntaxException {
		//TODO
		try{
			Block b=null;
			Token first=null;
			first =t;
			ArrayList<Dec> decs = new ArrayList<Dec>();
			ArrayList<Statement> statements =new ArrayList<Statement>();
			match(Kind.LBRACE);
			while(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME)||statementKind.contains(t.kind)){
				if(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME)){
					decs.add(dec());
				}
				if(statementKind.contains(t.kind)){
					statements.add(statement());
				}
		}
		match(Kind.RBRACE);
		b= new Block(first,decs, statements);
		return b;
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		//throw new UnimplementedFeatureException(); Anitha
	}

	Program program() throws SyntaxException {
		//TODO
		try{
			Program p = null; 
			Block b=null;
			ArrayList<ParamDec> paramList = new ArrayList<ParamDec>();
			Token first =t;
			match(Kind.IDENT);
			if(t.isKind(Kind.LBRACE))
				b=block();
			else{
				paramList.add(paramDec());
				while(t.isKind(Kind.COMMA)){
					consume();
					paramList.add(paramDec());
					}
				b=block();
			}
			p= new Program(first,paramList,b);
			return p;
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		
		//throw new UnimplementedFeatureException();Anitha comment
	}

	ParamDec paramDec() throws SyntaxException {
		//TODO
		try{
			ParamDec p=null;
			Token first=t; 
			if(t.isKind(KW_URL)||t.isKind(KW_INTEGER)||t.isKind(KW_FILE)||t.isKind(KW_BOOLEAN)){
				//while(t.isKind(KW_URL)||t.isKind(KW_INTEGER)||t.isKind(KW_FILE)||t.isKind(KW_BOOLEAN)){
					consume();
					Token ident = t;
					match(IDENT);
					p= new ParamDec(first, ident);
				//}
			}
			else
				throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
			return p;
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		
		//throw new UnimplementedFeatureException(); Anitha commented
	}

	Dec dec() throws SyntaxException {
		//TODO
		try{
			Dec d=null;
			Token first=t;
			if(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME)){
				consume();
				Token next=t;
				match(Kind.IDENT);
				d= new Dec(first, next);
			}
			else
				throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
			return d;
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		
		//throw new UnimplementedFeatureException();Anitha
	}

	Statement statement() throws SyntaxException {
		//TODO
		try{
			Statement s=null;
			switch(t.kind){
			case OP_SLEEP:
				Token first=t;
				Expression e0=null;
				match(Kind.OP_SLEEP);
				e0=expression();
				match(SEMI);
				s= new SleepStatement(first, e0);
				break;
			case KW_WHILE:
				s=whileStatement();
				break;
			case KW_IF:
				s=ifStatement();
				break;
			case IDENT:
				Token nextPeek = scanner.peek();
				if(nextPeek.kind==ASSIGN)
					s=assign();
				else 
					s=chain();
				match(Kind.SEMI);
				break;
			default:{
				if(operations.containsKey(t.kind)){
					s=chain();
					match(Kind.SEMI);
					break;
				}
				else
					throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);}
			}
			return s;
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		
		//throw new UnimplementedFeatureException();Anitha 
	}

	
	Statement assign() throws SyntaxException {
		try{
			IdentLValue  i=null;
			Expression e=null;
			Token first=t;
			match(IDENT);
			i= new IdentLValue(first);
			match(ASSIGN);
			e=expression();
			return new AssignmentStatement(first,i,e);
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		
	}
//change Anitha asap
	Statement chain() throws SyntaxException {
		//TODO
		try{
			Token first = t, arrow =null;
			Chain s1=null;
			ChainElem  s2=null;
			s1=chainElem();
			if(!t.isKind(ASSIGN)){
				arrow=arrowOp();
				s2=chainElem();
				s1 = new BinaryChain(first,s1, arrow,s2);
				if(t.isKind(ARROW) ||t.isKind(BARARROW)){
					while(t.isKind(ARROW)||t.isKind(BARARROW)){
						arrow=arrowOp();
						s2=chainElem();
						s1=new BinaryChain(first,s1,arrow,s2);
					}
				}
			}
			else
				assign();
			return s1;
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		//throw new UnimplementedFeatureException(); ANitha 
	}
	
	ChainElem chainElem() throws SyntaxException {
		//TODO
		try{
			Token first=t;
			ChainElem cE=null;
			Tuple tupleArg;
			if(operations.containsKey(t.kind)){
				if(operations.get(t.kind)=="chain"||operations.get(t.kind)=="filter"||operations.get(t.kind)=="image"||operations.get(t.kind)=="frame"){
					if(t.isKind(IDENT)){
						consume();
						cE=new IdentChain(first);
					}
					else{
						consume();
						tupleArg=arg();
						switch (operations.get(first.kind)){
						case "filter":
							cE=new FilterOpChain(first,tupleArg);
							break;
						case "image":
							cE=new ImageOpChain(first,tupleArg);
							break;
						case "frame":
							cE=new FrameOpChain(first,tupleArg);
							break;
						}
					}
				}
			}
			else 
				throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine); //New changes Anitha
			return cE;
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		//throw new UnimplementedFeatureException(); Anitha
	}
	Token arrowOp() throws SyntaxException{
		try{
			Token first=t;
			if(t.isKind(ARROW))
				match(ARROW);
			else if  (t.isKind(BARARROW))
				match(BARARROW);
			else 
				throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
			return first;
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
	}
	Tuple arg() throws SyntaxException {
		try{
			ArrayList<Expression> exprList=new ArrayList<Expression>();
			Token t1=t;
			if(t.isKind(LPAREN)){
				match(LPAREN);
				exprList.add(expression());
				while(t.isKind(COMMA)){
					consume();
					exprList.add(expression());
				}
				match(RPAREN);			
			}
			return new Tuple(t1,exprList);
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		//throw new UnimplementedFeatureException(); Anitha
	}
	Statement whileStatement() throws SyntaxException{
		try{
			Token first=t;
			Expression e0=null;
			Block b0=null;
			match(KW_WHILE);
			match(LPAREN);
			e0=expression();
			match(RPAREN);
			b0=block();
			return new WhileStatement(first, e0, b0);
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
	}
	Statement ifStatement() throws SyntaxException{
		try{
			Token first=t;
			Expression e0=null;
			Block b0=null;
			match(KW_IF);
			match(LPAREN);
			e0=expression();
			match(RPAREN);
			b0=block();
			return new IfStatement(first, e0, b0);
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		
	}
	Expression expression() throws SyntaxException {
		//TODO
		try{
			Expression e0=null, e1=null;
			Token t1=t, op=null;
			e0=term();
			while(operations.containsKey(t.kind)){
				if(operations.get(t.kind).equals("relOp")){
					op=t;
					consume();
					e1=term();
					e0=new BinaryExpression(t1,e0,op,e1);
				}
				else
					//throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
					break;
			}
			return e0;
			
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		//throw new UnimplementedFeatureException();Anitha
	}

	Expression term() throws SyntaxException {
		//TODO
		try{
			Token t1=t, op=null;
			//System.out.println("intem"+t.getText());//Anitha phase 3
			Expression e0=null, e1=null;
			e0=elem();
			while(operations.containsKey(t.kind)){
				//System.out.println("Ani" +t.getText());//Anitha phase 3
				if(operations.get(t.kind).equals("weakOp")){
					op=t;
					consume();
					e1=elem();
					e0=new BinaryExpression(t1,e0,op,e1);
				}
				else
					//throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
					break;
			}
			return e0;
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		//throw new UnimplementedFeatureException();Anitha
	}

	Expression elem() throws SyntaxException {
		//TODO
		try{
			Token t1=t;
			Token op=null;
			Expression e0=null, e1=null;
			e0=factor();
			while(operations.containsKey(t.kind)){
				if(operations.get(t.kind).equals("strongOp")){
					op=t;
					consume();
					e1=factor();
					e0=new BinaryExpression(t1,e0,op,e1);
				}
				else
					break;
				//	throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
			}
			return e0;
			
		}catch(Exception e){
			throw new SyntaxException("Illegal token in statement: " + t.kind.text + " at line " +t.getLinePos().line+ " at position "+ t.getLinePos().posInLine);
		}
		//throw new UnimplementedFeatureException();Anitha
	}
	
	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
			if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
