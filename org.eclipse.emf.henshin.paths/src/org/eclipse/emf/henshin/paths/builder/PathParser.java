package org.eclipse.emf.henshin.paths.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.henshin.paths.builder.Builder.PathErrorHandler;
import org.xml.sax.SAXException;




public class PathParser{

	private int line;
	private List<String> fullPath = new ArrayList<String>();
	private IFile file;
	private PathErrorHandler reporter;
	private InputStream contents;
	private boolean plus = false;
	private Path paths;
	
	
	public void parse(IFile file, PathErrorHandler reporter) throws IOException, SAXException, CoreException {
		paths = new Path(file.getProject());
		this.file = file;
		this.reporter = reporter;
		contents = file.getContents();
		String row="";
		int prevb = contents.read();
		int b = contents.read();
		boolean ignoreComment = false;
		boolean ignoreDoc = false;
		line = 1;
		while(b!=-1){
			if((char)b==';' || (char)b=='{' || (char)b=='}'){
				findPath(row);
				row = "";
			}
			else if((char)b == '\n'){
				if(ignoreComment){
					ignoreComment = false;
					row = "";
				}
				line++;	
			}
			else if((char)b == '/' && prevb == '/') 
				ignoreComment = true;
			else if(((char)b == '*' && (char)prevb == '/') || ((char)prevb == '*' && (char)b == '/')){
				if(!ignoreDoc && row.length()!=0)
					row = row.substring(0, row.length()-1);
				ignoreDoc = !ignoreDoc;
			}
			else if(!ignoreComment && !ignoreDoc)
				row+=(char)b;
			prevb=b;
			b = contents.read();
		}
		
	}

	private void findPath(String row) {
		String path = getMatchedStrings(row, "(\"[A-Za-z\\-\\_/\\.]+\"|\\((\\w|\\w+ *, *\\w+)*\\))").replaceAll("[\\(\\)]+", "");
		String identifier = getMatchedStrings(row, "([A-Za-z]+)(.*[\\(=])+","f =", "l ", "f\\.", "f\\(");
		if(!path.equals("") && !identifier.equals("")){
			if(paths.exists(path))
				paths.addPath(path, identifier);
			else{
				String prevIdentifier = getMatchedStrings(row, "([ =]\\w+\\.|\\(\\w+\\.)").replaceAll("[ .=\\(]", "");
				String prevPath = paths.getPath(prevIdentifier);
				if(!path.contains("\"")){
					path = getPath(path);
					if(path.length()==0) return;
				}
				else
					path = path.replaceAll("\"", "");
				path = prevPath + "/" + path;

				if(paths.exists(path))
					paths.addPath(path, identifier);
				else
					raiseError(true, "This Path ist failary: " + path);
			}
		}
		else if(!identifier.equals("")){
			path = getMatchedStrings(row, "\\(([A-Za-z]+)\\)");
			path = paths.getPath(path.replaceAll("[\\(\\)]", ""));
			if(path!=null && !path.equals(""))
				raiseError(!paths.addPath(path, identifier), "Dont exsist: " + identifier + " = " + path);
		}
	}

	private String getPath(String path) {
		String result = "";
		String[] identifiers = path.split(" *, *");
		for(String s : identifiers){
			String temp = paths.getPath(s);
			if(temp!=null)
				result += result.length()==0?temp:":" + temp;
		}
		return result.contains(":")?null:result;
	}

	private String getMatchedStrings(String text, String regex, String... split){
		Pattern patterns = Pattern.compile(regex);
		Matcher m = patterns.matcher(text);
		String result = "";
		String[] ssdsd = "run(".split("\\(");
		for(int i=0; m.find(); i++){
			String r = m.group(i);
			if(split!=null)
				for(String s : split){
					r = r.split(s.substring(1))[s.charAt(0)=='f'?0:r.split(s.substring(1)).length-1];
				}
			result += r;
		}
		return result;
	}

	private boolean raiseError(boolean found){
		return raiseError(found, "");
	}
	private boolean raiseError(boolean error, String row){
		if(error)
			try {
				reporter.error(new PathException(row, line));
			} catch (SAXException e) {
				e.printStackTrace();
			}
		return error;
	}
}
