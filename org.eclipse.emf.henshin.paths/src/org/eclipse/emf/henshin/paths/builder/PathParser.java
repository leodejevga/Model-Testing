package org.eclipse.emf.henshin.paths.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.paths.builder.Builder.PathErrorHandler;
import org.eclipse.core.resources.IMarker;

public class PathParser {

	private ErrorPosition position;
	private List<String> fullPath = new ArrayList<String>();
	private IFile file;
	private PathErrorHandler reporter;
	private InputStream contents;
	private boolean plus = false;
	private PathHandler paths;
	private IMarker marker;

	public void parse(IFile file, PathErrorHandler reporter) throws IOException, CoreException {
		paths = new PathHandler(file.getProject());
		this.file = file;
		this.reporter = reporter;
		contents = file.getContents();
		String row = "";
		int prevb = contents.read();
		int b = contents.read();
		boolean ignoreComment = false;
		boolean ignoreDoc = false;
		position = new ErrorPosition();
		position.line = 1;
		position.end = 1;
		
		marker = this.file.createMarker("Path Error");
		
		while (b != -1) {
			if ((char) b == ';' || (char) b == '{' || (char) b == '}') {
				findPath(row);
				row = "";
			} else if ((char) b == '\n') {
				if (ignoreComment) {
					ignoreComment = false;
					row = "";
				}
				position.line++;
			} else if ((char) b == '/' && prevb == '/')
				ignoreComment = true;
			else if (((char) b == '*' && (char) prevb == '/') || ((char) prevb == '*' && (char) b == '/')) {
				if (!ignoreDoc && row.length() != 0)
					row = row.substring(0, row.length() - 1);
				ignoreDoc = !ignoreDoc;
			} else if (!ignoreComment && !ignoreDoc)
				row += (char) b;
			prevb = b;
			b = contents.read();
			position.start++;
			position.end++;
		}

	}

	private void findPath(String row) {
		String path = getMatchedStrings(row, "(\"[A-Za-z0-9\\-\\_/\\.]+\"|\\((\\w|\\w+ *, *\\w+)*\\))")
				.replaceAll("[\\(\\)]+", "");
		String identifier = getMatchedStrings(row, "([A-Za-z]+)(.*[\\(=])+", "f *=", "l +", "f\\.", "f\\(");
		
		if (!path.equals("") && !identifier.equals("")) {
			boolean henshinPath = checkHenshin(row);
			if (!row.contains(" final ") && !henshinPath && path.contains("\"")){ // && (path.contains("/") || path.contains("."))){
				HenshinPath p = new HenshinPath(path.replaceAll("\"", ""), file.getProject());
				paths.addPath(p, identifier);
				if(!p.exists() && !p.isFile()){
					position.set(row, path);
					raiseError(true, "Path not Found");
					position.reset();
				}
			}
			else if(!henshinPath)
				paths.addPath(path, identifier);
		}
	}

	private boolean checkHenshin(String row) {
		
		boolean result = checkResource(row);
		result = result?true:checkModule(row);
		result = result?true:getResource(row);
		result = result?true:setGraph(row);
		result = result?true:setUnit(row);
		result = result?true:setParameter(row);
		result = result?true:resultParameter(row);
		return result;
	}

	private boolean resultParameter(String row) {
		String content = getMatchedStrings(row, "\\.getResultParameterValue\\(\"\\w+\"", "l\\.getResultParameterValue\\(\"").replace("\"", "");
		content = content.length()==0? ":" + getMatchedStrings(row, "\\.getResultParameterValue\\(\\w+", "l\\.getResultParameterValue\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			String identifier = getMatchedStrings(row, "\\w+\\.getResultParameterValue\\(", "f\\.getResultParameterValue\\(");

			String linkedContent = content.startsWith(":")?paths.getPathAsString(content.substring(1)):content;
			content = content.startsWith(":")?content.substring(1):content;
			
			HenshinPath app = paths.getPath(identifier);
			HenshinPath temp = app.clone();
			if(temp.rule()){
				position.set(row, content);
				raiseError(!temp.isOutParameter(linkedContent), "\"" + linkedContent + "\" is not a result parameter");
				position.reset();
			}
			paths.addPath(temp, identifier);

			return true;
		}
		return false;
	}

	private boolean setParameter(String row) {
		String content = getMatchedStrings(row, "\\.setParameterValue\\(\"\\w+\"", "l\\.setParameterValue\\(\"").replace("\"", "");
		content = content.length()==0? ":" + getMatchedStrings(row, "\\.setParameterValue\\(\\w+", "l\\.setParameterValue\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			String identifier = getMatchedStrings(row, "\\w+\\.setParameterValue\\(", "f\\.setParameterValue\\(");
			String value = getMatchedStrings(row, "\\.setParameterValue\\(.+\\)", "l\\.setParameterValue\\(.+, ");
			value = value.substring(0, value.length()-1);
			String type=null;
			if(value.contains("\""))
				type = "EString";
			else if(value.contains("d"))
				type = "EDouble";
			else if(value.split("[a-zA-Z]").length==1)
				type = "EInt";
			
			String linkedContent = content.startsWith(":")?paths.getPathAsString(content.substring(1)):content;
			content = content.startsWith(":")?content.substring(1):content;
			
			HenshinPath app = paths.getPath(identifier);
			if(linkedContent == null)
				return true;
			if(app.rule()){
				position.set(row, content);
				raiseError(!app.isParameter(linkedContent, value, type, null), "Parameter \"" + linkedContent + "\" is not a parameter of " + identifier);
			}
			else if(app.exists() && app.module()){
				position.set(row, identifier);
				raiseError(true, "A rule for " + identifier + " is not set yet");
			}
			position.reset();
			paths.addPath(app, identifier);

			return true;
		}
		return false;
	}

	private boolean setUnit(String row) {
		String content = getMatchedStrings(row, "\\.setUnit\\(\\w+\\.getUnit\\(\"\\w+\"", "l\\.setUnit\\(\\w+\\.getUnit\\(\"").replace("\"", "");
		content = content.length()==0? ":" + getMatchedStrings(row, "\\.setUnit\\(\\w+\\.getUnit\\(\\w+", "l\\.setUnit\\(\\w+\\.getUnit\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			String identifier = getMatchedStrings(row, "\\w+\\.setUnit\\(", "f\\.setUnit\\(");

			String linkedContent = content.startsWith(":")?paths.getPathAsString(content.substring(1)):content;
			content = content.startsWith(":")?content.substring(1):content;
			
			HenshinPath app = paths.getPath(identifier);
			HenshinPath module = paths.getPath(getMatchedStrings(row, "\\w+\\.getUnit", "f\\.getUnit"));
			Unit unit = module.getUnit(linkedContent);
			if(module.module() && unit == null){
				position.set(row, content);
				raiseError(true, "The Module dont has Unit " + linkedContent);
				position.reset();
				return true;
			}
			position.set(row, identifier);
			raiseError(app==null, identifier + " is not initialized");
			position.reset();
			if(app.app() && unit!=null){
				position.set(row, content);
				raiseError(!app.initRule(unit), "The initialization of Rule " + unit.getName() + " was not successfull");
				position.reset();
			}
			paths.addPath(app, identifier);

			return true;
		}
		return false;
	}

	private boolean setGraph(String row) {
		String content = getMatchedStrings(row, "\\.setEGraph\\(\\w+", "l\\.setEGraph\\(");
		if(content.length()!=0){
			String identifier = getMatchedStrings(row, "\\w+\\.setEGraph\\(", "f\\.setEGraph\\(");
			HenshinPath app = paths.getPath(content);
			HenshinPath temp = app.clone();
			if(temp.graph()){
				position.set(row, content);
				raiseError(!temp.initApp(), "Init app dont worked");
				position.reset();
			}
			paths.addPath(temp, identifier);

			return true;
		}
		return false;
	}

	private boolean getResource(String row) {
		String content = getMatchedStrings(row, "\\.getResource\\(\".+\"", "l\\.getResource\\(\"").replace("\"", "");
		content = content.length()==0?":" + getMatchedStrings(row, "\\.getResource\\(\\w+", "l\\.getResource\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			String identifier = getMatchedStrings(row, "EGraph \\w+", "lEGraph ");
			String resource = getMatchedStrings(row, "\\w+\\.getResource\\(", "f\\.getResource\\(");
			HenshinPath resourceHP = paths.getPath(resource);

			String linkedContent = content.startsWith(":")?paths.getPathAsString(content.substring(1)):content;
			content = content.startsWith(":")?content.substring(1):content;

			HenshinPath temp = resourceHP.clone();
			if(temp.exists() && temp.resource()){
				position.set(row, content);
				raiseError(!temp.initGraph(linkedContent), "Init Graph dont worked");
				position.reset();
			}
			paths.addPath(temp, identifier);

			return true;
		}
		return false;
	}

	private boolean checkModule(String row) {
		String content="";
		content = getMatchedStrings(row, "getModule\\(\".+\"", "lgetModule\\(\"").replace("\"", "");
		content = content.length()==0?":" + getMatchedStrings(row, "getModule\\(\\w+", "lgetModule\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			
			String identifier = getMatchedStrings(row, "Module \\w+", "lModule ");
			HenshinPath resource = paths.getPath(getMatchedStrings(row, "\\w+.getModule\\(", "f.getModule\\("));
			
			String linkedContent = content.startsWith(":")?paths.getPathAsString(content.substring(1)):content;
			content = content.startsWith(":")?content.substring(1):content;
			
			HenshinPath temp = resource.clone();
			if(temp.exists() && temp.resource()){
				position.set(row, content);
				raiseError(!temp.initModule(linkedContent), "Init Module dont worked");
				position.reset();
			}
			paths.addPath(temp, identifier);

			return true;
		}
		return false;
	}

	private boolean checkResource(String row) {
		String content = getMatchedStrings(row, "HenshinResourceSet\\(\".+\"", "lHenshinResourceSet\\(\"").replace("\"", "");
		content = content.length()==0?":" + getMatchedStrings(row, "HenshinResourceSet\\(\\w+", "lHenshinResourceSet\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			String identifier = getMatchedStrings(row, "HenshinResourceSet \\w+", "lHenshinResourceSet ");

			String linkedContent = content.startsWith(":")?paths.getPathAsString(content.substring(1)):content;
			
			HenshinPath p;
			if(content.startsWith(":")){
				content = content.substring(1);
				p = paths.getPath(content);
			}
			else{
				p = new HenshinPath(content, file.getProject());
			}
			if(p==null){
				return false;
			}
			if(!p.exists() && linkedContent.length()==0){
				raiseError(true, "Path not found");
				return true;
			}
			HenshinPath n = p.clone();

			position.set(row, content);
			raiseError(!n.initResourceSet(true), "Resourceset Not initialized");
			position.reset();
			
			paths.addPath(n, identifier);
			return true;
		}
		return false;
	}

	
	private String getPath(String path) {
		String result = "";
		String[] identifiers = path.split(" *, *");
		for (String s : identifiers) {
			String temp = paths.getPathAsString(s);
			if (temp != null)
				result += result.length() == 0 ? temp : ":" + temp;
		}
		return result.contains(":") ? null : result;
	}

	public static String getMatchedStrings(String text, String regex, String... split) {
		Pattern patterns = Pattern.compile(regex);
		Matcher m = patterns.matcher(text);
		String result = "";

		for (int i = 0; m.find(); i++) {
			String r = m.group(i);
			if (split != null)
				for (String s : split) {
					try{
						String[] temp = r.split(s.substring(1));
						r = temp.length==0?"":temp[s.charAt(0) == 'f' ? 0 : temp.length - 1];
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			result += r;
		}
		return result;
	}

	private boolean raiseError(boolean error, String row) {
		if (error){
			reporter.error(new PathException(row, position));
//			marker.
		}
		return error;
	}
	private boolean raiseWarning(boolean warning, String row) {
		if (warning)
			reporter.warning(new PathException(row, position));
		return warning;
	}
}
