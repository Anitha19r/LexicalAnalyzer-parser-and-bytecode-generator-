package cop5556sp17;

import java.util.ArrayList;
import java.util.Arrays;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	//Enum for storing states
	public static enum State {	
						IN_DIGIT,
						IN_IDENT,
						AFTER_EQ,
						AFTER_NOT,
						AFTER_LESS,
						AFTER_GRT,
						AFTER_SLASH,
						AFTER_MINUS, 
						START,
						AFTER_PIPE;
						}
	
	
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		//Considered EOF length =0 and text -eof
		public String getText() {
			//TODO IMPLEMENT THIS
			String text = "";
			if(length==0)
				return "eof";
			else if((pos+length) <= chars.length()){
				text = chars.substring(pos, (pos+length)); 
				return text;
			}
			return "eof";
			
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			int [] positionArray = new int[LinePosMap.size()];
			for (int i=0; i<LinePosMap.size();i++){
				positionArray[i]=LinePosMap.get(i);
			}
			int position = Arrays.binarySearch(positionArray, this.pos);
			if(position <0) position = (position*(-1))-2;
			LinePos linePosval = new LinePos(position, pos- positionArray[position]);
			return linePosval;

		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
			int retVal = 0;
			if(kind.equals(Kind.INT_LIT)) {
			String text = getText();
				try{
					retVal = Integer.parseInt(text);
				} catch(Exception e) {
					throw new NumberFormatException();
				}
			}
			return retVal;
			
		}//Anitha changes phase 2
		public boolean isKind(Kind kindIn) {
			// TODO Auto-generated method stub
			if(this.kind==kindIn)
				return true;
			else return false;
			
		}
		//Anitha changes phase 3 begin
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }

		//Anitha changes phase 3 end
	}

	 


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();


	}
	
	
	//Added by Anitha to store the new line entries. This arrayList will store the entry position of new line
	ArrayList<Integer> LinePosMap = new ArrayList<Integer>(); 

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	/*Every time a character is encountered which can have another character to follow it we change the state to AFTER_(CHAR) 
	 * LATER WE VALIDATE FOR THE FOLLOWING CHAR. THEN ADD THE TOKEN AND CHANGE 3 THINGS 
	 * 1) STATE TO START 
	 * 2) POSITION++ (CHECK NOTE)
	 * 3) START POS OF NEXT CHAR++
	 * FOR EVERY \N NEWLINE CHAR WE ADD AN ENTRY IN THE LINEPOSMAP
	 * NOTE: IN SOME AFTER_(CHAR) CASES THE POS IS NOT INCREMENTED---> REASON:  WE HAVE ALREADY PARSED TO NEXT POS BUT 
	 * WE REALIZE THAT THE CHAR DOESNT FOLLOW THE PREV CHAR AS AN OPERATOR SO WE ADD THE PREV CHAR TO TOKEN N CHANGE 
	 * STATE TO START AND REITERATE FOR CURR CHAR
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		//TODO IMPLEMENT THIS!!!!
		int str_len = chars.length();
		State state = State.START;
		int ch,startPos = 0,len=0;
		int line = 0;
		LinePosMap.add(0);
		while (pos <= str_len){
			ch=pos<str_len?chars.charAt(pos) : -1;
			switch (state){
			case START:
				if(!(ch==-1)){
					pos=skipSpaces(pos);
					startPos=pos;
					ch=pos<str_len?chars.charAt(pos) : -1;
				}
				switch (ch) 
		        {
		            case -1: 
		            {
		            	tokens.add(new Token(Kind.EOF , pos, 0)); 
		            	pos++;
		            }
		            break;
		            case (int)'&':
		            	tokens.add(new Token(Kind.AND,startPos,1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		            break;
		            case (int)'%':
		            	tokens.add(new Token(Kind.MOD,startPos,1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		            break;
		            case (int)'+': 
		            {
		            	tokens.add(new Token(Kind.PLUS, startPos, 1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		        	}
		            break;
		            case (int)'*': 
		            {
		            	tokens.add(new Token(Kind.TIMES, startPos, 1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		        	}
		            break;
		            case (int)';': 
		            {
		            	tokens.add(new Token(Kind.SEMI, startPos, 1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		        	}
		            break;
		            case (int)',': 
		            {
		            	tokens.add(new Token(Kind.COMMA, startPos, 1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		        	}
		            break;
		            case (int)'(': 
		            {
		            	tokens.add(new Token(Kind.LPAREN, startPos, 1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		        	}
		            break;
		            case (int)')': 
		            {
		            	tokens.add(new Token(Kind.RPAREN, startPos, 1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		        	}
		            break;
		            case (int)'{': 
		            {
		            	tokens.add(new Token(Kind.LBRACE, startPos, 1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		        	}
		            break;
		            case (int)'}': 
		            {
		            	tokens.add(new Token(Kind.RBRACE, startPos, 1));
		            	pos++;
		            	startPos =pos;
		            	state = State.START;
		        	}
		            break;
		            case (int)'\n':
		 //           case (int)'\r': 
		            {
		            	pos++;
		            	line++;
		            	LinePosMap.add(pos);
		            	startPos =pos;
		        	}
		            
		            break;
		            
		            case (int)'=': 
		            {
		            	state = State.AFTER_EQ;
		            	pos++;
		            	
		            }
		            break;
		            case (int)'!': 
		            {
		            	state = State.AFTER_NOT;
		            	pos++;
		            }     
		            break;
		            case (int)'>': 
		            {
		            	state = State.AFTER_GRT;
		            	pos++;
		            }
		            break;
		            case (int)'<': 
		            {
		            	state = State.AFTER_LESS;
		            	pos++;
		            }
		            break;
		            case (int)'/': 
		            {
		            	state = State.AFTER_SLASH;
		            	pos++;
		            }
		            
		            break;
		            case (int)'-': 
		            {
		            	state = State.AFTER_MINUS;
		            	pos++;
		            }
		            break;
		            case (int)'|':
		            	state = State.AFTER_PIPE;
	            		pos++;
		            break;
		            
		            
		            
		            case (int)'0': 
		            {
		            	tokens.add(new Token(Kind.INT_LIT,startPos, 1));
		            	pos++;
		            	startPos =pos;
		            }
		            break;
		            default: 
		            {
		                if (Character.isDigit(ch)) 
		            	{
		            		state = State.IN_DIGIT;	                        		 
		            	}
		                else if (Character.isJavaIdentifierStart(ch)) 
		                {
		                     state = State.IN_IDENT;
		                } 
		                else 
		                {
		                	throw new IllegalCharException("illegal char " +(char)ch+" at pos "+pos);
		                }
		            }
		        }
				
				break;
			case IN_DIGIT: 
				int chkDig=getInt(pos);
				if(chkDig!=-1){
					tokens.add(new Token(Kind.INT_LIT,startPos, chkDig-pos));
					state = State.START;
					startPos =chkDig;
					pos=chkDig;
				}
				break;
			case IN_IDENT: 
				int chkIdent=getIdent(pos,startPos);
				if(chkIdent!=-1){
					state = State.START;
					pos=chkIdent;
					startPos =pos;
				}
				break;
			case AFTER_EQ: 
				if(pos<str_len){
					if(chars.charAt(pos) == '='){
						tokens.add(new Token(Kind.EQUAL,startPos, 2));
						pos++;
						startPos=pos;
						state = State.START;
					}
					else
						throw new IllegalCharException("Illegal \'=\' found, at pos : " + pos);
				}
				else
					throw new IllegalCharException("Illegal \'=\' found, at pos : " + pos);
				
				break;
			case AFTER_NOT: 
				if(pos<str_len){
					if(chars.charAt(pos) == '='){
						tokens.add(new Token(Kind.NOTEQUAL,startPos, 2));
						pos++;
					}
					else
						tokens.add(new Token(Kind.NOT,startPos, 1));
					
					startPos=pos;
					state = State.START;
				}
				else
					tokens.add(new Token(Kind.NOT,startPos, 1));
					startPos=pos;
					state = State.START;
				break;
			case AFTER_LESS: 
				if(pos<str_len){
					if(chars.charAt(pos) == '='){
						tokens.add(new Token(Kind.LE,startPos, 2));
						pos++;
						
					}
					else
						if(chars.charAt(pos) == '-'){
							tokens.add(new Token(Kind.ASSIGN,startPos, 2));
							pos++;
						}
					else	
						tokens.add(new Token(Kind.LT,startPos, 1));
					startPos=pos;
					state = State.START;
				}
				else
					tokens.add(new Token(Kind.LT,startPos, 1));
					startPos=pos;
					state = State.START;
				break;
			case AFTER_GRT: 
				if(pos<str_len){
					if(chars.charAt(pos) == '='){
						tokens.add(new Token(Kind.GE,startPos, 2));
						pos++;
					}
					else
						tokens.add(new Token(Kind.GT,startPos, 1));
					startPos=pos;
				}
				else
					tokens.add(new Token(Kind.GT,startPos, 1));
					startPos=pos;
					state = State.START;
					break;
			case AFTER_PIPE: 
				ch=pos<str_len?chars.charAt(pos) : -1;
				switch (ch) 
		        {
				case (int)'-':
					int curr_char = pos;
					if(curr_char < str_len-1){
						if((chars.charAt(curr_char)=='-' && chars.charAt(curr_char+1)=='>')){
							pos=pos+2;
							tokens.add(new Token(Kind.BARARROW,startPos,3));
							state = State.START;
							startPos =pos;
						}
						else {
							tokens.add(new Token(Kind.OR,startPos, 1));
							state = State.START;
							startPos =pos;
						}
					}
					else {
						tokens.add(new Token(Kind.OR,startPos, 1));
						state = State.START;
						startPos =pos;
					}
					break;
				default:
						tokens.add(new Token(Kind.OR,startPos, 1));
						state = State.START;
						startPos =pos;
					break;
			    }
				break;
			case AFTER_SLASH: 
				ch=pos<str_len?chars.charAt(pos) : -1;
				boolean comm_found=false;
				switch (ch) 
		        {
				case (int)'*':
					int curr_char = pos+1;
					while(curr_char < str_len -1){
						if(!(chars.charAt(curr_char)=='*' && chars.charAt(curr_char+1)=='/'))
							if(chars.charAt(curr_char)=='\n')// || chars.charAt(curr_char)=='\r')
								{
								line++;
								LinePosMap.add(curr_char+1);
								curr_char++;
								}
							else
								curr_char++;	
						else 
							{
							comm_found=true;
							state = State.START;
							pos= curr_char+2;
							startPos=pos;
							break;
							}
					}
					if((curr_char == str_len-1 || curr_char == str_len )&& !comm_found)
						throw new IllegalCharException("Comment tag not closed");
					break;
				default:  
					tokens.add(new Token(Kind.DIV,startPos, 1));
					state = State.START;
            		startPos =pos;
            		break;
		        }
				break;
			case AFTER_MINUS: 
				if(pos<str_len){
					if(chars.charAt(pos) == '>'){
						tokens.add(new Token(Kind.ARROW,startPos, 2));
						pos++;
					}
					else
						tokens.add(new Token(Kind.MINUS,startPos, 1));
					startPos=pos;
				}
				else
					tokens.add(new Token(Kind.MINUS,startPos, 1));
				startPos=pos;
				state = State.START;
				break;
			default: assert false;
			}
		}
		//tokens.add(new Token(Kind.EOF,pos,0));
		return this;  
	}

	public int skipSpaces(int pos){
		boolean whiteSpace =true;
		while(whiteSpace && pos<chars.length()){
			if(Character.isWhitespace(chars.charAt(pos))){
				if(chars.charAt(pos)=='\n'){
					LinePosMap.add(pos+1);
				}
				pos++;
				
			}
			else {
				whiteSpace=false;
				break;
			}
		}
		return pos;
	}
	public int getInt(int pos) throws IllegalNumberException{
		StringBuilder int_lit = new StringBuilder();
		int value;
		while(pos<chars.length()){
		if ((chars.charAt(pos))>='0' && (chars.charAt(pos))<='9'){
			int_lit.append(chars.charAt(pos));
			pos++;
		}
		else break;
		}
		if(int_lit.length()>0){
			try {
				value = Integer.parseInt(int_lit.toString());
				return pos;
			} catch (NumberFormatException e) {
				value = -1; 
				throw new IllegalNumberException("Number Out of Range Error");
			}
		}
		else 
			return -1;
		
	}
	/*
	 * THIS METHOD WILL CHECK IF THE STRING IS IDENT ELSE SET THE CORRES KIND  AND RETURN THE NEXT POSITION
	 */
	public int getIdent(int pos,int startPos){
		int curr_pos = pos;
		StringBuilder ident = new StringBuilder();
		char curr_char = chars.charAt(curr_pos);
		while((curr_char>='a' && curr_char<='z') || (curr_char>='A' && curr_char<='Z') || (curr_char=='$') || (curr_char=='_') || (curr_char>='0' && curr_char<='9')){
			ident.append(String.valueOf(curr_char));
			curr_pos++;
			if(curr_pos<(chars.length())){
			curr_char = chars.charAt(curr_pos);
			} else {
				break;
			}
		}
		Kind kind = Kind.IDENT; 
		switch(ident.toString()) {
			case "integer":
				kind = Kind.KW_INTEGER;
				break;
			case "boolean":
				kind = Kind.KW_BOOLEAN;
				break;
			case "image":
				kind = Kind.KW_IMAGE;
				break;
			case "url":
				kind = Kind.KW_URL;
				break;
			case "file":
				kind = Kind.KW_FILE;
				break;
			case "frame":
				kind = Kind.KW_FRAME;
				break;
			case "while":
				kind = Kind.KW_WHILE;
				break;
			case "if":
				kind = Kind.KW_IF;
				break;
			case "sleep":
				kind = Kind.OP_SLEEP;
				break;
			case "screenheight":
				kind = Kind.KW_SCREENHEIGHT;
				break;
			case "screenwidth":
				kind = Kind.KW_SCREENWIDTH;
				break;
			case "gray":
				kind = Kind.OP_GRAY;
				break;
			case "convolve":
				kind = Kind.OP_CONVOLVE;
				break;
			case "blur":
				kind = Kind.OP_BLUR;
				break;
			case "scale":
				kind = Kind.KW_SCALE;
				break;
			case "width":
				kind = Kind.OP_WIDTH;
				break;
			case "height":
				kind = Kind.OP_HEIGHT;
				break;
			case "xloc":
				kind = Kind.KW_XLOC;
				break;
			case "yloc":
				kind = Kind.KW_YLOC;
				break;
			case "hide":
				kind = Kind.KW_HIDE;
				break;
			case "show":
				kind = Kind.KW_SHOW;
				break;
			case "move":
				kind = Kind.KW_MOVE;
				break;
			case "true":
				kind = Kind.KW_TRUE;
				break;
			case "false":
				kind = Kind.KW_FALSE;
				break;
			default:  	
				kind = Kind.IDENT;
		}
		tokens.add(new Token(kind,startPos, curr_pos-pos));
		return curr_pos;
		
	}
	
	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
			
		if (tokenNum >= tokens.size())
	        return null;
	    return tokens.get(tokenNum);
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		LinePos linepos = t.getLinePos(); 
		if(linepos!=null) {
			return linepos;
		}
		return null;

	}


}
