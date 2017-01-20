package org.eclipse.emf.henshin.paths.builder;

public class PathException extends Exception{

	private static final long serialVersionUID = 1546845184351L;
	int line = 0;

	public PathException(String text, int line){
		super(text);
		this.line = line;
	}
	
	public int getLineNumber(){
		return line;
	}

}
