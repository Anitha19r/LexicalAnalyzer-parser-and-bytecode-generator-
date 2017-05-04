package cop5556sp17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.slotNo = 1;
		this.iterator = 0;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slotNo ;
	int iterator ;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES); //Anitha
		//cw = new ClassWriter(0);
		//cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params){
			dec.visit(this, mv);
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, startRun, endRun, 1);//Anitha changes
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeN());
		if(assignStatement.getE().getTypeN().equals(TypeName.IMAGE))
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage",  PLPRuntimeImageOps.copyImageSig, false);
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	
	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().visit(this,0);// 0 indicates left
		if (binaryChain.getArrow().isKind(Kind.ARROW)){
			if(binaryChain.getE0().getTypeN().equals(TypeName.URL)){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL",  PLPRuntimeImageIO.readFromURLSig, false);
				//mv.visitFieldInsn(PUTFIELD, className,binaryChain.getFirstToken().getText(), PLPRuntimeImageIO.BufferedImageDesc);
			}
			if(binaryChain.getE0().getTypeN().equals(TypeName.FILE)){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile",   PLPRuntimeImageIO.readFromFileDesc, false);
				//mv.visitFieldInsn(PUTFIELD, className,binaryChain.getFirstToken().getText(), PLPRuntimeImageIO.BufferedImageDesc);//Anitha check what text needs to be left
			}
			binaryChain.getE1().visit(this, 1);// 1 indicates right for arrow
		}
		else{
			mv.visitInsn(DUP);
			binaryChain.getE1().visit(this, 2);//indicates dup for barrow
		}
		if (binaryChain.getE1() instanceof IdentChain) {//Anitha check
			if (((IdentChain)binaryChain.getE1()).getDec() instanceof ParamDec) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, ((IdentChain)binaryChain.getE1()).getDec().getIdent().getText(),
				((IdentChain)binaryChain.getE1()).getDec().getTypeN().getJVMTypeDesc());

			} else {
				if (((IdentChain)binaryChain.getE1()).getDec().getTypeN().equals(TypeName.INTEGER)) {
				mv.visitVarInsn(ILOAD, ((IdentChain)binaryChain.getE1()).getDec().getSlotNo());
				} else {
				mv.visitVarInsn(ALOAD, ((IdentChain)binaryChain.getE1()).getDec().getSlotNo());
				}			
			}
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //Anitha changes
		CodeGenUtils.genPrint(DEVEL, mv, "\n Binary Expression first token: " + binaryExpression.getFirstToken().getText() );//Anitha is this needed
		switch (binaryExpression.getOp().kind){
		case PLUS:
		case TIMES:
		case DIV:
		case MINUS:
		case MOD:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);// commented to handle image
			break;
		default: break;
		}
		TypeName type1= binaryExpression.getE0().getTypeN();
		TypeName type2= binaryExpression.getE1().getTypeN();
		/*if(type2.equals(TypeName.IMAGE)){
			binaryExpression.getE1().visit(this, arg);
			binaryExpression.getE0().visit(this, arg);
		}
		else{
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
		}
		*/
		Label setTrue = new Label();
		Label setTrue2 = new Label();
		switch (binaryExpression.getOp().kind){
		case PLUS:
			if(type1.equals(TypeName.INTEGER) && type2.equals(TypeName.INTEGER))
				mv.visitInsn(IADD);
			if(type1.equals(TypeName.IMAGE) && type2.equals(TypeName.IMAGE))
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add", PLPRuntimeImageOps.addSig, false);
			break;
        case MINUS:
        	if(type1.equals(TypeName.INTEGER) && type2.equals(TypeName.INTEGER))
        		mv.visitInsn(ISUB);
        	if(type1.equals(TypeName.IMAGE) && type2.equals(TypeName.IMAGE))
        		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub", PLPRuntimeImageOps.subSig, false);
        	break;
        case TIMES: 
        	if(type1.equals(TypeName.INTEGER) && type2.equals(TypeName.INTEGER))
        		mv.visitInsn(IMUL);
        	else if((type1.equals(TypeName.IMAGE) && type2.equals(TypeName.INTEGER)))
        		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul",PLPRuntimeImageOps.mulSig, false);
        	else if (type1.equals(TypeName.INTEGER) && type2.equals(TypeName.IMAGE)){
        		mv.visitInsn(SWAP);
        		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul",PLPRuntimeImageOps.mulSig, false);
        	}
        	break;
        case DIV:
        	if(type1.equals(TypeName.INTEGER) && type2.equals(TypeName.INTEGER))
        		mv.visitInsn(IDIV);
        	if(type1.equals(TypeName.IMAGE) && type2.equals(TypeName.INTEGER))
        		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div", PLPRuntimeImageOps.divSig, false);
        	break;
        case MOD:
        	if(type1.equals(TypeName.IMAGE) )
        		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod",PLPRuntimeImageOps.modSig, false);
        	if(type1.equals(TypeName.INTEGER) && type2.equals(TypeName.INTEGER))
        		mv.visitInsn(IREM);
        	break;
        case LT:
        	binaryExpression.getE0().visit(this, arg);
        	binaryExpression.getE1().visit(this, arg);
        	mv.visitJumpInsn(IF_ICMPGE, setTrue);
        	mv.visitInsn(ICONST_1);
        	mv.visitJumpInsn(GOTO, setTrue2);
        	mv.visitLabel(setTrue);
        	mv.visitInsn(ICONST_0);
        	mv.visitLabel(setTrue2);
        	break;


        	case LE:
        	binaryExpression.getE0().visit(this, arg);
        	binaryExpression.getE1().visit(this, arg);
        	mv.visitJumpInsn(IF_ICMPGT, setTrue);
        	mv.visitInsn(ICONST_1);
        	mv.visitJumpInsn(GOTO, setTrue2);
        	mv.visitLabel(setTrue);
        	mv.visitInsn(ICONST_0);
        	mv.visitLabel(setTrue2);
        	break;

        case OR:
        	binaryExpression.getE0().visit(this, arg);
        	mv.visitJumpInsn(IFNE, setTrue);
        	binaryExpression.getE1().visit(this, arg);
        	mv.visitJumpInsn(IFNE, setTrue);
        	mv.visitInsn(ICONST_0);
        	mv.visitJumpInsn(GOTO, setTrue2);
        	mv.visitLabel(setTrue);
        	mv.visitInsn(ICONST_1);
        	mv.visitLabel(setTrue2);
        	break;


        case AND:
        	binaryExpression.getE0().visit(this, arg);
        	mv.visitJumpInsn(IFEQ, setTrue);
        	binaryExpression.getE1().visit(this, arg);
        	mv.visitJumpInsn(IFEQ, setTrue);
        	mv.visitInsn(ICONST_1);
        	mv.visitJumpInsn(GOTO, setTrue2);
        	mv.visitLabel(setTrue);
        	mv.visitInsn(ICONST_0);
        	mv.visitLabel(setTrue2);
        	break;


        	

        case GT:
        	binaryExpression.getE0().visit(this, arg);
        	binaryExpression.getE1().visit(this, arg);
        	mv.visitJumpInsn(IF_ICMPLE, setTrue);
        	mv.visitInsn(ICONST_1);
        	mv.visitJumpInsn(GOTO, setTrue2);
        	mv.visitLabel(setTrue);
        	mv.visitInsn(ICONST_0);
        	mv.visitLabel(setTrue2);
        	break;


        case GE:
        	binaryExpression.getE0().visit(this, arg);
        	binaryExpression.getE1().visit(this, arg);
        	mv.visitJumpInsn(IF_ICMPLT, setTrue);
        	mv.visitInsn(ICONST_1);
        	mv.visitJumpInsn(GOTO, setTrue2);
        	mv.visitLabel(setTrue);
        	mv.visitInsn(ICONST_0);
        	mv.visitLabel(setTrue2);
        	break;


        case EQUAL:
        	binaryExpression.getE0().visit(this, arg);
        	binaryExpression.getE1().visit(this, arg);
        	switch (type1){
        	case INTEGER:
        	case BOOLEAN:
        		mv.visitJumpInsn(IF_ICMPNE, setTrue);
	        	mv.visitInsn(ICONST_1);
	        	mv.visitJumpInsn(GOTO, setTrue2);
	        	mv.visitLabel(setTrue);
	        	mv.visitInsn(ICONST_0);
	        	mv.visitLabel(setTrue2);
	        	break;
        	default: 
        		mv.visitJumpInsn(IF_ACMPNE, setTrue);
	        	mv.visitInsn(ICONST_1);
	        	mv.visitJumpInsn(GOTO, setTrue2);
	        	mv.visitLabel(setTrue);
	        	mv.visitInsn(ICONST_0);
	        	mv.visitLabel(setTrue2);
	        	break;
        	}
        	break;


        case NOTEQUAL:
        	binaryExpression.getE0().visit(this, arg);
        	binaryExpression.getE1().visit(this, arg);
        	switch (type1){
        	case INTEGER:
        	case BOOLEAN:
        		mv.visitJumpInsn(IF_ICMPEQ, setTrue);
	        	mv.visitInsn(ICONST_1);
	        	mv.visitJumpInsn(GOTO, setTrue2);
	        	mv.visitLabel(setTrue);
	        	mv.visitInsn(ICONST_0);
	        	mv.visitLabel(setTrue2);
	        	break;
	        default:
	        	mv.visitJumpInsn(IF_ACMPEQ, setTrue);
	        	mv.visitInsn(ICONST_1);
	        	mv.visitJumpInsn(GOTO, setTrue2);
	        	mv.visitLabel(setTrue);
	        	mv.visitInsn(ICONST_0);
	        	mv.visitLabel(setTrue2);
	        	break;
        	}
        	break;


        	default:
        	break;
        	}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//Anitha changes
		CodeGenUtils.genPrint(DEVEL, mv, "\n Block ");
		ArrayList<Dec> declarations = block.getDecs();
		ArrayList<Statement> statements = block.getStatements();
 		Label startBlock = new Label();
		Label endBlock = new Label();
		mv.visitLineNumber(block.getFirstToken().getLinePos().line, startBlock);
		mv.visitLabel(startBlock);
		for(Dec d: declarations)
			d.visit(this, mv);
		for(Statement s : statements){
			s.visit(this, mv);
			/*if (s instanceof AssignmentStatement) {
				if (((AssignmentStatement) s).getVar().getDec() instanceof ParamDec) {
					mv.visitVarInsn(ALOAD, 0);
				}
			}*/
			//s.visit(this, mv);
			if (s instanceof BinaryChain)
				mv.visitInsn(POP);
			
		}
		mv.visitLineNumber(0, endBlock);
		mv.visitLabel(endBlock);
		//doing this here as end of Block not determined/visited until now.Therefore can't store it before.	
		for(Dec d: declarations){
			mv.visitLocalVariable(d.getIdent().getText(), d.getTypeN().getJVMTypeDesc(), null, startBlock, endBlock,d.getSlotNo());//local var as its notdone in dec
			slotNo--;
			
		}
		
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//Anitha changes
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	} 

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		if(constantExpression.firstToken.isKind(Kind.KW_SCREENWIDTH)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
			//mv.visitFieldInsn(PUTFIELD, className, constantExpression.firstToken.getText(), "I");
		}
		else if(constantExpression.firstToken.isKind(Kind.KW_SCREENHEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
			//mv.visitFieldInsn(PUTFIELD, className, constantExpression.firstToken.getText(), "I");
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//Anitha
		CodeGenUtils.genPrint(DEVEL, mv, "\n Declaration: " + declaration.getFirstToken().getText() );
		declaration.setSlotNo(slotNo++);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		filterOpChain.getArg().visit(this, arg);
		Token t1 =filterOpChain.getFirstToken();
		if(t1.isKind(Kind.OP_CONVOLVE)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp", PLPRuntimeFilterOps.opSig, false);
		}
		if(t1.isKind(Kind.OP_BLUR)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp", PLPRuntimeFilterOps.opSig, false);
		}
		if(t1.isKind(Kind.OP_GRAY)){
			if (! arg.equals(2)) 
				mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp",PLPRuntimeFilterOps.opSig , false);
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		if(frameOpChain.getFirstToken().isKind(Kind.KW_SHOW)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "showImage", PLPRuntimeFrame.showImageDesc, false);
		}
		else if(frameOpChain.getFirstToken().isKind(Kind.KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		}
		else if(frameOpChain.getFirstToken().isKind(Kind.KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		}
		else if(frameOpChain.getFirstToken().isKind(Kind.KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getX", PLPRuntimeFrame.getXValDesc, false);
		}
		else if(frameOpChain.getFirstToken().isKind(Kind.KW_YLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getY", PLPRuntimeFrame.getYValDesc, false);
		}
		return null;
	}
	
	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {//Anitha change
		
		if(!arg.equals(1)){
			if(identChain.getDec() instanceof ParamDec){
				if (identChain.getTypeN().equals(TypeName.INTEGER) || identChain.getTypeN().equals(TypeName.BOOLEAN)) {
					mv.visitVarInsn(ILOAD, ((Dec) identChain.getDec()).getSlotNo());
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),identChain.getDec().getTypeN().getJVMTypeDesc());
				}
				else if(identChain.getTypeN().equals(TypeName.FILE) || identChain.getTypeN().equals(TypeName.IMAGE)){
					mv.visitVarInsn(ALOAD, ((Dec) identChain.getDec()).getSlotNo());
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),identChain.getDec().getTypeN().getJVMTypeDesc());
				}
			}
			else {
				if (identChain.getDec().getTypeN().equals(TypeName.FRAME)) {
					if (identChain.getDec().getinit())
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNo());
					else 
						mv.visitInsn(ACONST_NULL);
				} 
				else 
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNo());
				}
			}
		else{
			if(identChain.getDec() instanceof ParamDec){
				switch (identChain.getTypeN()){
				case INTEGER:
				case BOOLEAN://Anitha check
					//mv.visitVarInsn(ALOAD, 0);
					//mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),identChain.getDec().getTypeN().getJVMTypeDesc());//Anitha check end
					mv.visitVarInsn(ILOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(),identChain.getTypeN().getJVMTypeDesc());
					identChain.getDec().setinit(true);//Anitha check
					break;
				case FILE:
					/*mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
					identChain.getDec().getTypeN().getJVMTypeDesc());*/
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),identChain.getDec().getTypeN().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",PLPRuntimeImageIO.writeImageDesc, false);
					//mv.visitInsn(POP);
					identChain.getDec().setinit(true);//Anitha check
					break;
				case URL:
					break;
				default:
					break;
				
				}
			}
			else{
				switch (identChain.getTypeN()){
				case IMAGE:
					//mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNo());//Anitha check
					mv.visitVarInsn(ASTORE, ((Dec) identChain.getDec()).getSlotNo());
					identChain.getDec().setinit(true);//Anitha check
					break;
				case FRAME:
					//mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNo());
					if (identChain.getDec().getinit()) {
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNo());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",PLPRuntimeFrame.createOrSetFrameSig, false);
						} 
					else {
						mv.visitInsn(ACONST_NULL);
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNo());
						identChain.getDec().setinit(true);
					}
					break;
				case FILE:
					//mv.visitVarInsn(ALOAD, (identChain.getDec()).getSlotNo());
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNo());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getDec().setinit(true);//Anitha check
					break;
				case INTEGER:
				case BOOLEAN:
					//mv.visitVarInsn(ILOAD, identChain.getDec().getSlotNo());//Anitha check
					mv.visitVarInsn(ISTORE, identChain.getDec().getSlotNo());
					identChain.getDec().setinit(true);//Anitha check
					break;
				default:
					break;
				}
			}
		}
		return null;
		
	}
	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//Anitha changes //Anitha check
		CodeGenUtils.genPrint(DEVEL, mv, "\nIdent Expression first token: " + identExpression.getFirstToken().getText() );//Anitha is this needed
		if (identExpression.getDec() instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);//this pointer load
			if (identExpression.getDec().getTypeN().equals( TypeName.INTEGER))
				mv.visitFieldInsn(GETFIELD, className, identExpression.firstToken.getText(), "I");
			else if (identExpression.getDec().getTypeN().equals( TypeName.BOOLEAN))
				mv.visitFieldInsn(GETFIELD, className, identExpression.firstToken.getText(), "Z");
			else
				mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(),identExpression.getDec().getTypeN().getJVMTypeDesc());
		} 
		else{
			if (identExpression.getTypeN().equals(TypeName.INTEGER) || identExpression.getTypeN().equals(TypeName.BOOLEAN)) {
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlotNo());
			} 
			else {
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlotNo());
			}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//Anitha changes 
		if (identX.getDec() instanceof ParamDec) {
			/*if (identX.getDec() instanceof ParamDec) {
			if (identX.getDec().getTypeN().equals( TypeName.INTEGER))
				mv.visitFieldInsn(PUTFIELD, className, identX.getText(), "I");
			else if (identX.getDec().getTypeN().equals( TypeName.BOOLEAN))
				mv.visitFieldInsn(PUTFIELD, className, identX.getText(), "Z");
		} else
			mv.visitVarInsn(ISTORE, identX.getDec().getSlotNo());//Anitha */
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, identX.getText(),identX.getDec().getTypeN().getJVMTypeDesc());
		} 
		else{
			switch (identX.getDec().getTypeN()){
				case IMAGE:
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",PLPRuntimeImageOps.copyImageSig, false);
					mv.visitVarInsn(ASTORE, identX.getDec().getSlotNo());
					identX.getDec().setinit(true);
					break;
			
				case INTEGER:
				case BOOLEAN:
					mv.visitVarInsn(ISTORE, identX.getDec().getSlotNo());
					identX.getDec().setinit(true);
					break;
				default:  
					mv.visitVarInsn(ASTORE, identX.getDec().getSlotNo());
					identX.getDec().setinit(true);
					break;
			}
			}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//Anitha changes
		ifStatement.getE().visit(this, arg);
		Label after = new Label();
		mv.visitJumpInsn(IFEQ, after);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(after);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		if(imageOpChain.getFirstToken().isKind(Kind.OP_WIDTH)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
		}
		else if(imageOpChain.getFirstToken().isKind(Kind.OP_HEIGHT)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
		}
		else if(imageOpChain.getFirstToken().isKind(Kind.KW_SCALE)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "scale",PLPRuntimeImageOps.scaleSig , false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//Anitha changes
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//For assignment 5, only needs to handle integers and booleans
		//Anitha changes
		//instance variable in class, init with values from arg array
		FieldVisitor fv = null;
		if (paramDec.getTypeN().equals(TypeName.INTEGER)) {
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "I", null, null);
			//fv.visitEnd();
		} else if (paramDec.getTypeN().equals( TypeName.BOOLEAN)) {
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Z", null, null);
			//fv.visitEnd();
		}
		else if (paramDec.getTypeN().equals(TypeName.URL)) {
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/io/URL;", null, null);
			//fv.visitEnd();
		} 
		else if (paramDec.getTypeN().equals( TypeName.FILE)) {
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/io/File;", null, null);
			//fv.visitEnd();
		}
		fv.visitEnd();
		mv.visitVarInsn(ALOAD, 0);//this pointer
		if(paramDec.getTypeN().equals(TypeName.FILE)){
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
		}
		mv.visitVarInsn(ALOAD, 1);//arg 
		//mv.visitInsn(ICONST_0);//Check
		mv.visitLdcInsn(iterator++); 
		//mv.visitInsn(AALOAD); 
		if (paramDec.getTypeN().equals( TypeName.INTEGER)) {
			mv.visitInsn(AALOAD); 
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		} 
		else if (paramDec.getTypeN().equals( TypeName.BOOLEAN)) {
			mv.visitInsn(AALOAD); 
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}
		else if (paramDec.getTypeN().equals( TypeName.URL)) {
			//mv.visitInsn(AALOAD); 
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		} 
		else if (paramDec.getTypeN().equals( TypeName.FILE)) {
			mv.visitInsn(AALOAD); 
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
		}
		return null;

	}



	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		
		for( Expression expr : tuple.getExprList()){
			expr.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//Anitha Changes
		
		Label body = new Label();
		Label guard = new Label();
		mv.visitJumpInsn(GOTO, guard);

		mv.visitLabel(body);
		whileStatement.getB().visit(this, null); 

		mv.visitLabel(guard);
		whileStatement.getE().visit(this, arg);

		mv.visitJumpInsn(IFNE, body);
		return null;
	}

}
