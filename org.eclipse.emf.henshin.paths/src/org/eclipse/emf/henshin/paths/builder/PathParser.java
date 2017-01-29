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
import org.xml.sax.SAXException;

public class PathParser {

	private int line;
	private List<String> fullPath = new ArrayList<String>();
	private IFile file;
	private PathErrorHandler reporter;
	private InputStream contents;
	private boolean plus = false;
	private PathHandler paths;

	public void parse(IFile file, PathErrorHandler reporter) throws IOException, SAXException, CoreException {
		paths = new PathHandler(file.getProject());
		this.file = file;
		this.reporter = reporter;
		contents = file.getContents();
		String row = "";
		int prevb = contents.read();
		int b = contents.read();
		boolean ignoreComment = false;
		boolean ignoreDoc = false;
		line = 1;
		while (b != -1) {
			if ((char) b == ';' || (char) b == '{' || (char) b == '}') {
				findPath(row);
				row = "";
			} else if ((char) b == '\n') {
				if (ignoreComment) {
					ignoreComment = false;
					row = "";
				}
				line++;
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
		}

	}

	private void findPath(String row) {
		String path = getMatchedStrings(row, "(\"[A-Za-z\\-\\_/\\.]+\"|\\((\\w|\\w+ *, *\\w+)*\\))")
				.replaceAll("[\\(\\)]+", "");
		String identifier = getMatchedStrings(row, "([A-Za-z]+)(.*[\\(=])+", "f =", "l ", "f\\.", "f\\(");
		
		if (!path.equals("") && !identifier.equals("")) {
			boolean henshinPath = checkHenshin(row);
			if (!row.contains(" final ") && !henshinPath && path.contains("\"") && (path.contains("/") || path.contains("."))){
				HenshinPath p = new HenshinPath(path.replaceAll("\"", ""), file.getProject());
				paths.addPath(p, identifier);
				raiseError(!paths.getPath(identifier).exists, "Path not Found");
			}
			else if(!henshinPath)
				paths.addPath(path, identifier);
		}
	}

	private boolean checkHenshin(String row) {
		String identifier="";
		String content = getMatchedStrings(row, "HenshinResourceSet\\(\\w+", "lHenshinResourceSet\\(");
		content = content.length()==0?":" + getMatchedStrings(row, "HenshinResourceSet\\(\".+\"", "lHenshinResourceSet\\(\"").replace("\"", ""):content;
		if(content.length()!=0 && !content.equals(":")){
			identifier = getMatchedStrings(row, "HenshinResourceSet \\w+", "lHenshinResourceSet ");
			HenshinPath p;
			if(content.startsWith(":"))
				p = new HenshinPath(content.substring(1), file.getProject());
			else 
				p = paths.getPath(content);
			if(p.exists)
				raiseError(!p.initResourceSet(true), "Resourceset Not initialized");
			paths.addPath(p, identifier);
			return true;
		}
		content = getMatchedStrings(row, "getModule\\(\".+\"", "lgetModule\\(\"").replace("\"", "");
		content = content.length()==0?":" + getMatchedStrings(row, "getModule\\(\\w+", "lgetModule\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			
			identifier = getMatchedStrings(row, "Module \\w+", "lModule ");
			HenshinPath resource = paths.getPath(getMatchedStrings(row, "\\w+.getModule\\(", "f.getModule\\("));
			if(content.startsWith(":"))
				content = paths.getPathAsString(content);

			if(resource.resource())
				raiseError(!resource.initModule(content), "Init Module dont worked");
			paths.addPath(resource, identifier);

			return true;
		}
		content = getMatchedStrings(row, "\\.getResource\\(\".+\"", "l\\.getResource\\(\"").replace("\"", "");
		content = content.length()==0?":" + getMatchedStrings(row, "\\.getResource\\(\\w+", "l\\.getResource\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			identifier = getMatchedStrings(row, "EGraph \\w+", "lEGraph ");
			String resource = getMatchedStrings(row, "\\w+\\.getResource\\(", "f\\.getResource\\(");
			HenshinPath resourceHP = paths.getPath(resource);
			
			if(content.startsWith(":"))
				content = paths.getPathAsString(content);

			if(resourceHP.module())
				raiseError(!resourceHP.initGraph(content), "Init Graph dont worked");
			paths.addPath(resourceHP, identifier);

			return true;
		}

		content = getMatchedStrings(row, "\\.setEGraph\\(\\w+", "l\\.setEGraph\\(");
		if(content.length()!=0){
			identifier = getMatchedStrings(row, "\\w+\\.setEGraph\\(", "f\\.setEGraph\\(");
			HenshinPath app = paths.getPath(content);
			if(app.graph())
				raiseError(!app.initApp(), "Init app dont worked");
			paths.addPath(app, identifier);

			return true;
		}
		content = getMatchedStrings(row, "\\.setUnit\\(\\w+\\.getUnit\\(\"\\w+\"", "l\\.setUnit\\(\\w+\\.getUnit\\(\"").replace("\"", "");
		content = content.length()==0? ":" + getMatchedStrings(row, "\\.setUnit\\(\\w+\\.getUnit\\(\\w+", "l\\.setUnit\\(\\w+\\.getUnit\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			identifier = getMatchedStrings(row, "\\w+\\.setUnit\\(", "f\\.setUnit\\(");
			//TODO: module erkennen und übergeben
			if(content.startsWith(":"))
				content = paths.getPathAsString(content.substring(1));
			HenshinPath app = paths.getPath(identifier);
			Unit unit = paths.getPath(getMatchedStrings(row, "\\w+\\.getUnit", "f\\.getUnit")).getUnit(content);
			if(unit == null)
				raiseError(true, "The Module dont has Unit " + content);
			if(app.app())
				raiseError(!app.initRule(content, unit), "Init rule dont worked");
			paths.addPath(app, identifier);

			return true;
		}
		content = getMatchedStrings(row, "\\.setParameterValue\\(\"\\w+\"", "l\\.setParameterValue\\(\"").replace("\"", "");
		content = content.length()==0? ":" + getMatchedStrings(row, "\\.setParameterValue\\(\\w+", "l\\.setParameterValue\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			identifier = getMatchedStrings(row, "\\w+\\.setParameterValue\\(", "f\\.setParameterValue\\(");
			String stringValue = getMatchedStrings(row, "\\.setParameterValue\\(.+\\)", "l\\.setParameterValue\\(.+, ");
			stringValue = stringValue.substring(0, stringValue.length()-1);
			Object value;
			if(stringValue.contains("\""))
				value = stringValue.replaceAll("\"", "");
			else if(stringValue.contains("d"))
				value = Double.parseDouble(stringValue);
			else
				value = Integer.parseInt(stringValue);
			if(content.startsWith(":"))
				content = paths.getPathAsString(content.substring(1));
			HenshinPath app = paths.getPath(identifier);
			if(app.rule())
				raiseError(!app.isParameter(content, value), "Parameter " + content + " is not a parameter");
			paths.addPath(app, identifier);

			return true;
		}
		content = getMatchedStrings(row, "\\.getResultParameterValue\\(\"\\w+\"", "l\\.getResultParameterValue\\(\"").replace("\"", "");
		content = content.length()==0? ":" + getMatchedStrings(row, "\\.getResultParameterValue\\(\\w+", "l\\.getResultParameterValue\\("):content;
		if(content.length()!=0 && !content.equals(":")){
			identifier = getMatchedStrings(row, "\\w+\\.getResultParameterValue\\(", "f\\.getResultParameterValue\\(");

			if(content.startsWith(":"))
				content = paths.getPathAsString(content.substring(1));
			HenshinPath app = paths.getPath(identifier);
			if(app.rule())
				raiseError(!app.isResultParameter(content), content + " is not a result rule");
			paths.addPath(app, identifier);

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
					r = r.split(s.substring(1))[s.charAt(0) == 'f' ? 0 : r.split(s.substring(1)).length - 1];
				}
			result += r;
		}
		return result;
	}

	private boolean raiseError(boolean found) {
		return raiseError(found, "");
	}

	private boolean raiseError(boolean error, String row) {
		if (error)
			try {
				System.out.println("Error raised: " + row);
				reporter.error(new PathException(row, line));
			} catch (SAXException e) {
				e.printStackTrace();
			}
		return error;
	}
}
