package cop5556sp17;



import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import cop5556sp17.AST.Dec;


public class SymbolTable {
	class SymbolValue{
		int scope;
		Dec decl;
		SymbolValue(int scope1, Dec decl1){
			this.scope =scope1;
			this.decl=decl1;
		}
		@Override
	    public boolean equals(Object other) {
	      return other != null && other.getClass() == getClass()
	        && ((SymbolValue) other).scope == (this.scope)
	        && SymbolValue.this.decl.getIdent().getText().equals(((SymbolValue) other).decl.getIdent().getText())
	        && SymbolValue.this.decl.getType().getText().equals(((SymbolValue) other).decl.getType().getText()); 
	    }
	}
	int  current_scope, next_scope;
	Stack<Integer> scope_stack;
	HashMap<String, LinkedList<SymbolValue>> map1;
	

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		current_scope = next_scope++; 
		scope_stack.push(current_scope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		scope_stack.pop();
		if(!scope_stack.isEmpty())
	    current_scope = scope_stack.peek();
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		LinkedList<SymbolValue> link;
		SymbolValue sv=new SymbolValue(current_scope,dec);
		
		if(map1.containsKey(ident)){
			link=map1.get(ident);
			if(link.contains(sv)){
				return false;
			}
			else{
				link.add(sv);
				map1.put(ident,link);	
				return true;
			}
		}
		else{
			link = new LinkedList<SymbolValue>();
			link.add(sv);
			map1.put(ident,link);
			return true;
		}
		
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		LinkedList<SymbolValue> link;
		SymbolValue sv;
		link=map1.get(ident);
		if(link==null) return null;
		else{
			for (ListIterator<SymbolValue> it = link.listIterator(link.size()); it.hasPrevious();){
				sv=it.previous();
				for(int i=scope_stack.size()-1; i>=0;i--){
					if(sv.scope == scope_stack.get(i))
						return sv.decl;
				}
			}
		}
		return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		this.current_scope=0;
		this.next_scope=0;
		this.scope_stack = new Stack<>();
		this.map1 = new HashMap<String, LinkedList<SymbolValue>>();
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return this.map1.toString();
	}
	
	
	
	


}
