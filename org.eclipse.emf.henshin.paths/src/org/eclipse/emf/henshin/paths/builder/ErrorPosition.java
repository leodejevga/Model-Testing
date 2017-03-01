package org.eclipse.emf.henshin.paths.builder;

public class ErrorPosition {
	public int line;
	public int start;
	public int end;
	
	private int startTemp=0;
	private int endTemp=0;
	
	public void set(int start, int end){
		if(startTemp == 0)
			startTemp = this.start;
		if(endTemp == 0)
			endTemp = this.end;
		this.start = start;
		this.end = end;
	}
	public void reset(){
		if(startTemp != 0)
			start = startTemp;
		if(endTemp != 0)
			end = endTemp;
		startTemp = 0;
		endTemp = 0;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Line = " + line + " from: " + start + " to: " + end;
	}
	public void set(String row, String target) {
		int s = this.start - (row.length()-row.indexOf(target))+1;
		int e = this.end - (row.length()-row.indexOf(target)-target.length());
		set(s, e);
	}
}
