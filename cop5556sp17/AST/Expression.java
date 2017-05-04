package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	/*Anitha phase 4*/
	Dec dec;
	TypeName t;
	
	public TypeName getTypeN() {
		return this.t;
	}
	public void setTypeN(TypeName t1) {
		t = t1;
	}
	
	public Dec getDec() {
		return dec;
	}

	public void setDec(Dec dec) {
		this.dec = dec;
	}
	/*Anitha phase 4*/
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}
	
	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
