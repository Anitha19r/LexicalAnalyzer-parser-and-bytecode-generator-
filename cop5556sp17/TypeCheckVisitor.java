package cop5556sp17;

import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.INTEGER;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SCALE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Kind k;
		binaryChain.getE0().visit(this, arg);
		binaryChain.getE1().visit(this, arg);
		switch( binaryChain.getArrow().kind )
        {  
		case ARROW:
			TypeName t = binaryChain.getE0().getTypeN();
			switch(binaryChain.getE0().getTypeN()){
			case URL: 
				if(binaryChain.getE1().getTypeN().equals(TypeName.IMAGE)){
					binaryChain.setTypeN(TypeName.IMAGE);
				}
				else{
					throw new TypeCheckException("Invalid image in URL chain operation" );
				}
				break;
			case FILE:
				if(binaryChain.getE1().getTypeN().equals(TypeName.IMAGE)){
					binaryChain.setTypeN(TypeName.IMAGE);
				}
				else{
					throw new TypeCheckException("Invalid image in File chain operation" );
				}
				break;
			case FRAME:
				k=binaryChain.getE1().getFirstToken().kind;
				if(binaryChain.getE1() instanceof FrameOpChain && (k.equals(KW_XLOC)||k.equals(KW_YLOC)))
					binaryChain.setTypeN(TypeName.INTEGER);
				else 
					if(binaryChain.getE1() instanceof FrameOpChain && (k.equals(KW_SHOW) || k.equals(KW_HIDE) || k.equals(KW_MOVE)))
						binaryChain.setTypeN(TypeName.FRAME);
			
				else {
					throw new TypeCheckException("Invalid Frame chain operation" );
				}
				break;
			case IMAGE:
				k=binaryChain.getE1().getFirstToken().kind;
				if(binaryChain.getE1() instanceof ImageOpChain && (k.equals(OP_WIDTH)||k.equals(OP_HEIGHT)))
					binaryChain.setTypeN(TypeName.INTEGER);
				else if(binaryChain.getE1().getTypeN().equals(TypeName.FRAME))
					binaryChain.setTypeN(TypeName.FRAME);
				else if(binaryChain.getE1().getTypeN().equals(TypeName.FILE))
					binaryChain.setTypeN(TypeName.NONE);
				else if(binaryChain.getE1() instanceof FilterOpChain && (k.equals(OP_BLUR)||k.equals(OP_CONVOLVE)||k.equals(OP_GRAY)))
					binaryChain.setTypeN(TypeName.IMAGE);
				else if(binaryChain.getE1() instanceof ImageOpChain  && k.equals(KW_SCALE))
					binaryChain.setTypeN(TypeName.IMAGE);
				else if(binaryChain.getE1() instanceof IdentChain && binaryChain.getE1().getTypeN().equals(IMAGE))
					binaryChain.setTypeN(TypeName.IMAGE);
				else
					throw new TypeCheckException("Invalid Image chain operation" );
				break;
			case INTEGER:
				if(binaryChain.getE1() instanceof IdentChain && binaryChain.getE1().getTypeN().equals(INTEGER)){
					binaryChain.setTypeN(INTEGER);
				}
				else{
					throw new TypeCheckException("Invalid Bindary chain operation for integer");
				}
				break;
				
			default:
				throw new TypeCheckException("Invalid Image chain operation" );
			}
        	break;
        case BARARROW:
        	if(binaryChain.getE0().getTypeN().equals(TypeName.IMAGE)){
        		Kind k1=binaryChain.getE1().getFirstToken().kind;
        		if(k1.equals(OP_BLUR)||k1.equals(OP_CONVOLVE)||k1.equals(OP_GRAY))
					binaryChain.setTypeN(TypeName.IMAGE);
        		else 
        			throw new TypeCheckException("Invalid Image chain barrow operation" );
        	}
        	else
        		throw new TypeCheckException("Invalid Image chain barrow operation " );
        	break;
        
        default:
        	throw new TypeCheckException("Invalid  chain operation " );

        }
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
        switch( binaryExpression.getOp().kind )
        {  case PLUS:
           case MINUS:
    			if(!((binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER) && binaryExpression.getE1().getTypeN().equals(TypeName.INTEGER)) 
    				|| (binaryExpression.getE0().getTypeN().equals(TypeName.IMAGE) && binaryExpression.getE1().getTypeN().equals(TypeName.IMAGE))))
    				throw new TypeCheckException("Invalid minus operation" );
    			if((binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER)))
    				binaryExpression.setTypeN(TypeName.INTEGER);
    			else
    				binaryExpression.setTypeN(TypeName.IMAGE);	
    			break;
           case TIMES: 
        	   if(!((binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER) && binaryExpression.getE1().getTypeN().equals(TypeName.INTEGER)) 
       				|| (binaryExpression.getE0().getTypeN().equals(TypeName.IMAGE) && binaryExpression.getE1().getTypeN().equals(TypeName.INTEGER))
       				|| (binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER) && binaryExpression.getE1().getTypeN().equals(TypeName.IMAGE))))
        		   throw new TypeCheckException("Invalid multiplication operation" );
        	   if((binaryExpression.getE0().getTypeN().equals(TypeName.IMAGE)) || (binaryExpression.getE0().getTypeN().equals(TypeName.IMAGE)) )
        		   binaryExpression.setTypeN(TypeName.IMAGE);
        	   else
        		   binaryExpression.setTypeN(TypeName.INTEGER);
        	   break;
           case DIV: //Anitha changes assignment 6
        	   if(!((binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER) && binaryExpression.getE1().getTypeN().equals(TypeName.INTEGER)) 
          				|| (binaryExpression.getE0().getTypeN().equals(TypeName.IMAGE) && binaryExpression.getE1().getTypeN().equals(TypeName.INTEGER))
          				|| (binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER) && binaryExpression.getE1().getTypeN().equals(TypeName.IMAGE))))
           		   throw new TypeCheckException("Invalid division operation" );
           	   if((binaryExpression.getE0().getTypeN().equals(TypeName.IMAGE)) || (binaryExpression.getE0().getTypeN().equals(TypeName.IMAGE)) )
           		   binaryExpression.setTypeN(TypeName.IMAGE);
           	   else
           		   binaryExpression.setTypeN(TypeName.INTEGER);
        	    /* if (!(binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER) && binaryExpression.getE1().getTypeN().equals(TypeName.INTEGER)))
        	    	throw new TypeCheckException("Invalid division operation" );
        	    binaryExpression.setTypeN(TypeName.INTEGER);*/
        	    break;
           case LT:
           case GT:
           case LE: 
           case GE: 
        	   if(!((binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER) && binaryExpression.getE1().getTypeN().equals(TypeName.INTEGER)) 
          				|| (binaryExpression.getE0().getTypeN().equals(TypeName.BOOLEAN) && binaryExpression.getE1().getTypeN().equals(TypeName.BOOLEAN)))) 
           	   		throw new TypeCheckException("Invalid comparison operation" );
        	   binaryExpression.setTypeN(TypeName.BOOLEAN);	
        	   break;
           case EQUAL:
           case NOTEQUAL: 
        	   if(!(binaryExpression.getE0().getTypeN().equals(binaryExpression.getE1().getTypeN())))
        		   throw new TypeCheckException("Invalid equals operation" );
        	   binaryExpression.setTypeN(TypeName.BOOLEAN);
        	   break;
           case AND:
        	   if(binaryExpression.getE0().getTypeN().equals(TypeName.BOOLEAN) && binaryExpression.getE1().getTypeN().equals(TypeName.BOOLEAN))
        		   binaryExpression.setTypeN(TypeName.BOOLEAN); 
        	   else {
	   				throw new TypeCheckException("AND operated on other than BOOLEAN.");
	   			}
	   			break;
	   		case OR:
	   			if(binaryExpression.getE0().getTypeN().equals(TypeName.BOOLEAN) && binaryExpression.getE1().getTypeN().equals(TypeName.BOOLEAN))
	   				binaryExpression.setTypeN(TypeName.BOOLEAN);
	   			else {
	   				throw new TypeCheckException("OR operated on other than BOOLEAN.");
	   			}
	   			break;
	   		case MOD:
	   			if(binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER) && binaryExpression.getE1().getTypeN().equals(TypeName.INTEGER))
		   				binaryExpression.setTypeN(INTEGER);
	   			else if(binaryExpression.getE0().getTypeN().equals(TypeName.INTEGER) && binaryExpression.getE1().getTypeN().equals(TypeName.IMAGE))
	   				binaryExpression.setTypeN(IMAGE); //Assignment 6
	   			else if(binaryExpression.getE0().getTypeN().equals(TypeName.IMAGE) && binaryExpression.getE1().getTypeN().equals(TypeName.INTEGER))
	   				binaryExpression.setTypeN(IMAGE);//Assignment 6
	   			else {
	   				throw new TypeCheckException("MOD operated on other than INTEGER.");
	   			}
	   			break;
           default: throw new TypeCheckException("illegal char " );
        }
        return binaryExpression;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		
		symtab.enterScope();
		for( Dec decs : block.getDecs()){
			decs.visit(this, arg);
		}
		for( Statement stats : block.getStatements()){
			stats.visit(this, arg);
		}
		symtab.leaveScope();
		return block;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		
		booleanLitExpression.setTypeN(TypeName.BOOLEAN);
		return booleanLitExpression;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		

		if(!(filterOpChain.getArg().getExprList().size() == 0)){
			throw new TypeCheckException("FilterOp expression list size != 0");
		}
		else{
			filterOpChain.setTypeN(TypeName.IMAGE);
		}
		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		
		Token FrameOP = frameOpChain.firstToken;
		if (FrameOP.isKind(KW_SHOW)||FrameOP.isKind(KW_HIDE)) {
		    if(!(frameOpChain.getArg().getExprList().size() == 0))
		    	 throw new TypeCheckException("FrameOp Show/Hide expression list size != 0");
		    frameOpChain.setTypeN(TypeName.NONE);
		}
	    else if (FrameOP.isKind(KW_XLOC)||FrameOP.isKind(KW_YLOC)){
	    	 if(!(frameOpChain.getArg().getExprList().size() == 0))
	    		 throw new TypeCheckException("FrameOp Xloc/Yloc expression list size != 0");
	    	 frameOpChain.setTypeN(TypeName.INTEGER);
	    	}
	    else if(FrameOP.isKind(KW_MOVE)){
    			if(!(frameOpChain.getArg().getExprList().size() == 2))
   	    		 throw new TypeCheckException("FrameOp move expression list size != 2 ");
   	    	 frameOpChain.setTypeN(TypeName.NONE);
    		}
		else
		{
			throw new TypeCheckException("Bug in parser!!!!! :X");
		}
		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		
		Dec dec = symtab.lookup(identChain.getFirstToken().getText());
		if (dec == null){
			throw new TypeCheckException("Ident Chain not present");
		}
		identChain.setTypeN(dec.getTypeN());
		identChain.setDec(dec);
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		
		Dec dec = symtab.lookup(identExpression.getFirstToken().getText());
		if (dec == null){
			throw new TypeCheckException("Ident Expression not present");
		}
		identExpression.setTypeN(dec.getTypeN());
		identExpression.setDec(dec);
		return identExpression;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		
		ifStatement.getE().visit(this,arg);
		if(!(ifStatement.getE().getTypeN().equals(TypeName.BOOLEAN))){
			throw new TypeCheckException("If statement expression type mismatch : Boolean expected");
		}
		ifStatement.getB().visit(this, arg);
		return ifStatement;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		
		intLitExpression.setTypeN(TypeName.INTEGER);
		return intLitExpression;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		
		sleepStatement.getE().visit(this,arg);
		if(!(sleepStatement.getE().getTypeN().equals(TypeName.INTEGER))){
			throw new TypeCheckException("Sleep statement expression type mismatch : Boolean expected");
		}
		return sleepStatement;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		
		whileStatement.getE().visit(this, arg);
		if(!(whileStatement.getE().getTypeN().equals(TypeName.BOOLEAN))){
			throw new TypeCheckException("While statement expression type mismatch : Boolean expected");
		}
		whileStatement.getB().visit(this, arg);
		return whileStatement;
	}

	@Override 
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		
		boolean result = symtab.insert(declaration.getIdent().getText(), declaration);
		//System.out.println("dec " +result);
		if(!result){
			throw new TypeCheckException("Definition already exists for: " +declaration.getIdent().getText());
		}
		return declaration;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		symtab.enterScope();
		for(ParamDec p : program.getParams()){
			p.visit(this,arg);
		}
		Block b = program.getB();
		b.visit(this, arg);
		symtab.leaveScope();
		return program;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getVar().visit(this, arg);
		assignStatement.getE().visit(this, arg);
		
		if(!(assignStatement.getVar().getDec().getTypeN().equals(assignStatement.getE().getTypeN()))){
			throw new TypeCheckException("Type mismatch between expression ident and expression in Assignment");
		}
		return assignStatement;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec d = symtab.lookup(identX.getText());
		if(d == null) 
			throw new TypeCheckException("Ident value not found");
		else
			identX.setDec(d);
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		boolean result = symtab.insert(paramDec.getIdent().getText(), paramDec);
		//System.out.println("paramdec " +result);
		if(!result){
			throw new TypeCheckException("Definition already exists for: " +paramDec.getIdent().getText());
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setTypeN(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Token imageOp = imageOpChain.firstToken;
		Tuple t= imageOpChain.getArg();
		imageOpChain.getArg().visit(this, arg);
		if (imageOp.isKind(OP_WIDTH)|| imageOp.isKind(OP_HEIGHT)){
		     if(! (t.getExprList().size() == 0))
		    	 throw new TypeCheckException("Integer expression expected");
		     imageOpChain.setTypeN(TypeName.INTEGER);
		}
	    else if (imageOp.isKind(KW_SCALE)){
	    	 if(! (t.getExprList().size() == 1))
	    		 throw new TypeCheckException("Integer expression expected");
	    	 imageOpChain.setTypeN(TypeName.IMAGE);
	    }
	    return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		Object i=null;
		for (Expression e : tuple.getExprList()){
			i = e.visit(this, arg);
			if(!e.getTypeN().equals(TypeName.INTEGER)){
				throw new TypeCheckException("Integer expression expected");
			}
		
		}
		return i;
		//return null;
	}
}
