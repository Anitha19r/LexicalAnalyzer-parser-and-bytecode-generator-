package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	public Chain(Token firstToken) {
		super(firstToken);
	}
	TypeName t;
	/*Anitha phase 4*/
	public TypeName getTypeN() throws SyntaxException {
		return t;
	}
	public void setTypeN(TypeName t1) {
		t = t1;
	}
	/*Anitha phase 4*/

}
