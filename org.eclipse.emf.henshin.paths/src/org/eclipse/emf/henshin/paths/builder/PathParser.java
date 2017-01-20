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
		while(b!=-1){
			if((char)b==';' || (char)b=='{' || (char)b=='}'){
				String temp = row;
				row = "";
				if(temp.contains("\""))
					findPath(temp);
			}
			else if((char)b == '\n'){
				ignoreComment = false;
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
		String[] path = getMatchedStrings(row, "bank");//"\"[A-Za-z/\\.]+\"");
		String[] identifier = getMatchedStrings(row, "([A-Za-z]+)(.*[\\(=])+","f =", "l ", "f.");
		boolean found = paths.addPath(path, identifier[0]);
		if(!found)
			try {
				reporter.error(new PathException("Error found!!!!", line));
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private String[] getMatchedStrings(String text, String regex, String... split){
		Pattern patterns = Pattern.compile(regex);
		Matcher m = patterns.matcher(text);
		String[] result = new String[m.groupCount()];
		for(int i=0; i<result.length; i++){
			System.out.println("group=" + m.group());
			result[i] = m.group(i);
			if(split!=null)
				for(String s : split)
					result[i] = result[i].split(s.substring(1))[s.charAt(0)=='f'?0:result[i].split(s.substring(1)).length];
		}
		return result;
	}
//	private void findPath2() {
//
//		line = 1;
//		int b = 0;
//		try {
//			String text = "";
//			boolean found = false;
//			boolean check = false;
//			char last = ' ';
//			boolean ignore = false;
//			while(b != -1){
//				char c = (char) b;
//				if(c == '\n')
//					line++;
//				else if(c == '+' && fullPath.isEmpty())
//					plus = true;
//				else if((c == '=' || c == '!' || c == ',' || c == '+' || c == '(' || c == ')' || c == '%') && found)
//					ignore = true;
//				else if(c == '"' && last != '\\')
//					found = !found;
//				else if(found && !ignore)
//					text += c;
//				else if((c == '}' || c == ';' || (c == ',' || (!found && c == ')'))) && !fullPath.isEmpty())
//					check = true;
//				else if(plus && c == ';')
//					plus = false;
//				
//				
//				if(text.length()!=0 && !found){
//					text = text.replace("\\", "").replace("\\'", "").replace("\\n", "").replace("\\r", "");
//					fullPath.add(text);
//					text = "";
//				}
//				
//				if(check){
//					if(ignore)
//						ignore = false;
//					else
//						if(!fullPath.get(fullPath.size()-1).endsWith("."))
//							testPath();
//					plus = false;
//					fullPath = new ArrayList<String>();
//					check = false;
//				}
//				last = c;
//				b = contents.read();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return;
//		}
//	}
//
//	private void testPath() throws SAXException, CoreException {
//		String path = "";
//		for (String s : fullPath)
//			path+=s;
//		if(path.contains("http://www.") || path.contains("http://") || path.contains("www.") || path.startsWith(".."))
//			return;
//		if(path.contains("/") || path.contains(".") || path.contains("\\")){
//			
//			if(!path.contains("/") && !path.contains("\\"))
//				reporter.warning(new PathException("It could be a wrong Path, depending on how you using this String", line));
//			else if(fullPath.size()>1 || plus)
//				reporter.warning(new PathException("It could be a wrong Path, depending on the Parameters in the Path", line));
//			else{
//				if(found == null)
//					if(path.contains("."))
//						reporter.error(new PathException("File  \"" + path + "\"  in  \"" + pr.getName() + "\\\"  not exists", line));
//					else
//						reporter.error(new PathException("Directory  \"" + path + "\"  in  \"" + pr.getName() + "\\\"  not exists", line));
//			}
//		}
//	}
}
