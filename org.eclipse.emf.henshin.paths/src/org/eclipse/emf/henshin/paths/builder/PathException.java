package org.eclipse.emf.henshin.paths.builder;

public class PathException extends Exception{

	private static final long serialVersionUID = 1546845184351L;
	ErrorPosition ep;

	public PathException(String text, ErrorPosition ep){
		super(text);
		this.ep = ep;
	}
	
	public ErrorPosition getPosition(){
		return ep;
	}

}
