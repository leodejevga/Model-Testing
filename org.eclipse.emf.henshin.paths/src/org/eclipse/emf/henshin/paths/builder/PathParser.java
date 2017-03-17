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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

// TODO: Auto-generated Javadoc
/**
 * The Class PathParser.
 */
public class PathParser {

	/** The position. */
	private PathException.ErrorPosition position;

	/** The full path. */
	private List<String> fullPath = new ArrayList<String>();

	/** The file. */
	private IFile file;

	/** The reporter. */
	private PathErrorHandler reporter;

	/** The contents. */
	private InputStream contents;

	/** The plus. */
	private boolean plus = false;

	/** The paths. */
	private PathHandler paths;

	/** The marker. */
	private IMarker marker;

	/**
	 * Parses the.
	 *
	 * @param file the file
	 * @param reporter the reporter
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws CoreException the core exception
	 */
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
		position = new PathException.ErrorPosition();
		position.line = 1;
		position.end = 1;

		marker = this.file.createMarker("Path Error");
		while (b != -1) {
			if ((char) b == ';' || (char) b == '{' || (char) b == '}') {
				findPath(row);
				row = "";
			} else if ((char) b == '\n' || (char) b == '\r') {
				if (ignoreComment) {
					ignoreComment = false;
					row = "";
				}
				position.line++;
			} else if ((char) b == '/' && prevb == '/' || (char) b == '@')
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
		contents.close();

	}

	/**
	 * Find path.
	 *
	 * @param row the row
	 */
	private void findPath(String row) {
		String path = getMatchedStrings(row, "(\".+\"|\\((\\w|\\w+ *[,+] *\\w+)*\\))").replaceAll("[\\(\\)]+", "");
		String identifier = getMatchedStrings(row, "([A-Za-z]+)(.*[\\(=])+", "f *=", "l +", "f\\.", "f\\(");
		if (!path.equals("") && !identifier.equals("")) {
			boolean henshinPath = checkHenshin(row);
			if (!henshinPath && path.contains("\"")) { // && (path.contains("/") ||
														// path.contains("."))){
				HenshinFile p = new HenshinFile(path.replaceAll("\"", ""), file.getProject());
				paths.addPath(p, identifier);
				if (!p.exists() && path.contains("/")) {
					raiseWarning(true, "Path " + path + " not Found", row, path);
				}
			} else if (!henshinPath)
				paths.addPath(path, identifier);
		}
	}

	/**
	 * Check henshin.
	 *
	 * @param row the row
	 * @return true, if successful
	 */
	private boolean checkHenshin(String row) {
		if (!row.contains("(") && !row.contains(")"))
			return false;
		boolean result = checkResource(row);
		result = result ? true : checkModule(row);
		result = result ? true : getResource(row);
		result = result ? true : setGraph(row);
		result = result ? true : getSetUnit(row);
		result = result ? true : getUnit(row);
		result = result ? true : setUnit(row);
		result = result ? true : setParameter(row);
		result = result ? true : resultParameter(row);
		return result;
	}

	private String[] getContent(String precondition, String remove, String row) {
		String[] result = { "", "" };
		result[0] = getMatchedStrings(row, precondition + "\\(.+,|" + precondition + "\\(.+\\)", remove + "\\(")
				.replace(")", "").replace(",", "");

		String[] parts = result[0].split("\\+");
		if (parts.length == 1 && parts[0].length() == 0)
			return result;
		for (String part : parts) {
			if (part.contains("\""))
				result[1] += part.replace("\"", "").replace(" ", "");
			else {
				result[1] += paths.getPathAsString(part.replace(" ", ""));
			}
		}
		return result;
	}

	/**
	 * Result parameter.
	 *
	 * @param row the row
	 * @return true, if successful
	 */
	private boolean resultParameter(String row) {
		String[] contents = getContent("\\.getResultParameterValue", "l\\.getResultParameterValue", row);

		if (contents[1].length() != 0) {
			String identifier = getMatchedStrings(row, "\\w+\\.getResultParameterValue\\(",
					"f\\.getResultParameterValue\\(");

			HenshinFile app = paths.getPath(identifier);
			if (app == null) {
				raiseError(true, identifier + " is not a UnitApplication", row, identifier);
				return true;
			}
			HenshinFile temp = app.clone();
			if (temp.rule()) {
				raiseError(!temp.isOutParameter(contents[1]), "\"" + contents[1] + "\" is not a result parameter", row,
						contents[0]);
			}
			paths.addPath(temp, identifier);

			return true;
		}
		return false;
	}

	/**
	 * Sets the parameter.
	 *
	 * @param row the row
	 * @return true, if successful
	 */
	private boolean setParameter(String row) {
		String[] contents = getContent("\\.setParameterValue", "l\\.setParameterValue", row);

		if (contents[1].length() != 0) {
			String identifier = getMatchedStrings(row, "\\w+\\.setParameterValue\\(", "f\\.setParameterValue\\(");
			String value = getMatchedStrings(row, "\\.setParameterValue\\(.+\\)", "l\\.setParameterValue\\(.+, ")
					.replace(")", "");
			String valueName=value;
			String type = null;

			if (value.contains("\""))
				type = "EString";
			else if (value.split("[0-9\\.]").length > 1)
				type = "EDouble";
			else if (value.split("[0-9]").length > 1)
				type = "EInt";
			else {
				value = paths.getPathAsString(value);
				type = value.length() != 0 ? "EString" : null;
			}

			HenshinFile app = paths.getPath(identifier);
			if (raiseError(app == null, identifier + " is not a RuleApplication.", row, identifier))
				return true;

			if (app.rule()) {
				int result = app.isParameter(contents[1], value, type, null);

				if(result == 1)
					raiseError(result != 0, "Parameter \"" + contents[1] + "\" is not a parameter of " + identifier, row, contents[0]);
				else if (result == 2)
					raiseError(result != 0, "type of " + (value.contains("\"")?value:"\""+value+"\"") + " is not correct. Found: " + type, row, valueName);
				else if (result == -1)
					raiseError(result != 0, identifier + " RuleApplication is not initialized", row, identifier);
			} else if (app.exists() && app.module()) {
				raiseError(true, "A rule for " + identifier + " is not set yet", row, identifier);
			}
			paths.addPath(app, identifier);

			return true;
		}
		return false;
	}

	private boolean setUnit(String row) {
		String[] contents = getContent("\\.setUnit", "l\\.setUnit", row);
		if (contents[0].length() != 0) {
			String identifier = getMatchedStrings(row, "\\w+\\.setUnit\\(", "f\\.setUnit\\(");
			HenshinFile app = paths.getPath(identifier);

			if (raiseError(app == null, identifier + " is not a UnitApplication", row, identifier))
				return true;

			HenshinFile newApp = app.clone();
			if (app.app()) {
				HenshinFile rule = paths.getPath(contents[0]);
				if (raiseError(rule == null || rule.getRule() == null, contents[0] + " is not a Unit", row,
						contents[0]))
					return true;

				raiseError(!newApp.initRule(rule.getRule()),
						"Initialising the Unit " + contents[0] + " is not successfull", row, contents[0]);
				paths.addPath(newApp, identifier);
				return true;
			}
		}
		return false;
	}

	private boolean getUnit(String row) {
		String[] contents = getContent("\\.getUnit", "l\\.getUnit", row);
		if (contents[1].length() != 0) {
			String idModule = getMatchedStrings(row, "\\w+\\.getUnit\\(", "f\\.getUnit\\(");
			String identifier = getMatchedStrings(row, "Unit +\\w+ +=", "f *=", "lUnit ").replace(" ", "");
			HenshinFile module = paths.getPath(idModule);

			if (raiseError(module == null, idModule + " is not a Module", row, idModule))
				return true;

			if (module.module()) {
				HenshinFile rule = new HenshinFile(module.getUnit(contents[1]));
				if (raiseError(rule == null, idModule + " is not a Module", row, contents[0])) {
					return true;
				}
				paths.addPath(rule, identifier);
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the unit.
	 *
	 * @param row the row
	 * @return true, if successful
	 */
	private boolean getSetUnit(String row) {
		String[] contents = getContent("\\.setUnit\\(\\w+\\.getUnit", "l\\.setUnit\\(\\w+\\.getUnit", row);

		if (contents[1].length() != 0) {
			String identifier = getMatchedStrings(row, "\\w+\\.setUnit\\(", "f\\.setUnit\\(");

			HenshinFile app = paths.getPath(identifier);
			HenshinFile module = paths.getPath(getMatchedStrings(row, "\\w+\\.getUnit", "f\\.getUnit"));
			if (module == null)
				return false;
			Unit unit = module.getUnit(contents[1]);
			if (module.module() && unit == null) {
				raiseError(true, "The Module dont has Unit " + contents[1], row, contents[0]);
				return true;
			}
			raiseError(app == null, identifier + " is not initialized", row, identifier);
			if (app.app() && unit != null) {
				raiseError(!app.initRule(unit), "The initialization of Rule " + unit.getName() + " was not successfull",
						row, contents[0]);
			}
			paths.addPath(app, identifier);

			return true;
		}
		return false;
	}

	/**
	 * Sets the graph.
	 *
	 * @param row the row
	 * @return true, if successful
	 */
	private boolean setGraph(String row) {
		String[] contents = getContent("\\.setEGraph", "l\\.setEGraph", row);

		if (contents[1].length() != 0) {
			String identifier = getMatchedStrings(row, "\\w+\\.setEGraph\\(", "f\\.setEGraph\\(");
			HenshinFile graph = paths.getPath(contents[0]);
			HenshinFile temp = graph.clone();
			if (temp.graph()) {
				raiseError(!temp.initApp(), "Init app dont worked", row, contents[0]);
			}
			paths.addPath(temp, identifier);

			return true;
		}
		return false;
	}

	/**
	 * Gets the resource.
	 *
	 * @param row the row
	 * @return the resource
	 */
	private boolean getResource(String row) {
		String[] contents = getContent("\\.getResource", "l\\.getResource", row);

		if (contents[1].length() != 0) {
			String identifier = getMatchedStrings(row, "EGraph \\w+", "lEGraph ");
			String resource = getMatchedStrings(row, "\\w+\\.getResource\\(", "f\\.getResource\\(");
			HenshinFile resourceHP = paths.getPath(resource);
			if (resourceHP == null)
				return false;
			HenshinFile temp = resourceHP.clone();
			if (temp.exists() && temp.resource()) {
				raiseError(!temp.initGraph(contents[1]), "Init Graph dont worked", row, contents[0]);
			}
			paths.addPath(temp, identifier);

			return true;
		}
		return false;
	}

	/**
	 * Check module.
	 *
	 * @param row the row
	 * @return true, if successful
	 */
	private boolean checkModule(String row) {
		String[] contents = getContent("getModule", "lgetModule", row);

		if (contents[1].length() != 0) {

			String identifier = getMatchedStrings(row, "Module \\w+", "lModule ");
			if (identifier.length() == 0)
				return false;
			HenshinFile resource = paths.getPath(getMatchedStrings(row, "\\w+.getModule\\(", "f.getModule\\("));

			if (resource == null)
				return false;
			HenshinFile temp = resource.clone();
			if (temp.exists() && temp.resource()) {
				raiseError(!temp.initModule(contents[1]), "Init Module dont worked", row, contents[0]);
			}
			paths.addPath(temp, identifier);

			return true;
		}
		return false;
	}

	/**
	 * Check resource.
	 *
	 * @param row the row
	 * @return true, if successful
	 */
	private boolean checkResource(String row) {
		String[] contents = getContent("HenshinResourceSet", "lHenshinResourceSet", row);

		if (contents[1].length() != 0) {
			String identifier = getMatchedStrings(row, "HenshinResourceSet +\\w+", "lHenshinResourceSet ");

			HenshinFile p = new HenshinFile(contents[1], file.getProject());

			if (!p.exists()) {
				raiseError(true, "Path \"" + contents[1] + "\" not found", row, contents[0]);
				return true;
			}
			HenshinFile n = p.clone();

			raiseError(!n.initResourceSet(true), "Resourceset not initialized", row, contents[0]);

			paths.addPath(n, identifier);
			return true;
		}
		return false;
	}

	/**
	 * Gets the path.
	 *
	 * @param path the path
	 * @return the path
	 */
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

	/**
	 * Gets the matched strings.
	 *
	 * @param text the text
	 * @param regex the regex
	 * @param split the split
	 * @return the matched strings
	 */
	public static String getMatchedStrings(String text, String regex, String... split) {
		Pattern patterns = Pattern.compile(regex);
		Matcher m = patterns.matcher(text);
		String result = "";

		for (int i = 0; m.find(); i++) {
			String r = m.group();
			if (split != null)
				for (String s : split) {
					try {
						String[] temp = r.split(s.substring(1));
						r = temp.length == 0 ? "" : temp[s.charAt(0) == 'f' ? 0 : temp.length - 1];
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			result += r;
		}
		return result;
	}

	/**
	 * Raise error.
	 *
	 * @param error the error
	 * @param row the row
	 * @return true, if successful
	 */
	private boolean raiseError(boolean error, String message, String row, String toMark) {
		if (error) {
			position.set(row, toMark);
			reporter.error(new PathException(message, position));
			position.reset();
			// marker.
		}
		return error;
	}

	/**
	 * Raise warning.
	 *
	 * @param warning the warning
	 * @param row the row
	 * @return true, if successful
	 */
	private boolean raiseWarning(boolean warning, String message, String row, String toMark) {
		if (warning) {
			position.set(row, toMark);
			reporter.warning(new PathException(message, position));
			position.reset();
		}
		return warning;
	}
}
