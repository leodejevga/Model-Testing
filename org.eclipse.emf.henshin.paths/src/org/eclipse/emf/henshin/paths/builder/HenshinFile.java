package org.eclipse.emf.henshin.paths.builder;

//import java.io.File;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Parameter;
import org.eclipse.emf.henshin.model.ParameterKind;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

/**
 * The Class HenshinFile.
 */
public class HenshinFile extends ProjectFile {
	
	/** The engine. */
	private Engine engine;
	
	/** The module. */
	private Module module;
	
	/** The resource set. */
	private HenshinResourceSet resourceSet;
	
	/** The graph. */
	private EGraph graph;
	
	/** The unit application. */
	private UnitApplication app;
	
	/** The initialization status. */
	private int initReady = 0;
	
	/** The rule that was defined for the application. */
	private Unit rule;
	
	/** The xmi file path. */
	private String xmi;
	
	/** The henshin file path. */
	private String henshin;

	/**
	 * Instantiates a new henshin path.
	 *
	 * @param path the path of the file
	 * @param project the project of the file
	 */
	public HenshinFile(String path, IProject project) {
		super(path, project);
	}
	
	/**
	 * Instantiates a new henshin path.
	 *
	 * @param path the path of the file
	 * @param project the project of the file
	 */
	public HenshinFile(Unit rule) {
		super();
		this.rule = rule;
	}

	/**
	 * Instantiates a new henshin path with path.
	 *
	 * @param path the path
	 */
	public HenshinFile(ProjectFile path) {
		super(path.getPath(), path.file.getProject());
	}

	/**
	 * Inits the resource set.
	 *
	 * @param force true, if resource should be reinitialized
	 * @return true, if successful
	 */
	public boolean initResourceSet(boolean force) {
		if (force || resourceSet == null)
			try {
				if (file.getType() != IResource.FOLDER)
					return false;
				String p = file.getLocation().toOSString();
				resourceSet = new HenshinResourceSet(p);
			} catch (Exception e) {
				return false;
			}

		return true;
	}

	/**
	 * Inits the module.
	 *
	 * @param henshin the henshin path
	 * @return true, if successful
	 */
	public boolean initModule(String henshin) {
		this.henshin = henshin;
		if (resourceSet != null && henshin.endsWith(".henshin"))
			try {
				module = resourceSet.getModule(henshin, false);
			} catch (Exception e) {
				return false;
			}
		else
			return false;
		return true;
	}

	/**
	 * Inits the graph.
	 *
	 * @param xmi the xmi path
	 * @return true, if successful
	 */
	public boolean initGraph(String xmi) {
		this.xmi = xmi;
		if (resourceSet != null && xmi.endsWith(".xmi"))
			try {
				graph = new EGraphImpl(resourceSet.getResource(xmi));
			} catch (Exception e) {
				return false;
			}
		else
			return false;
		return true;
	}

	/**
	 * Inits the app.
	 *
	 * @return true, if successful
	 */
	public boolean initApp() {
		if (graph != null) {
			try {
				engine = new EngineImpl();
				app = new UnitApplicationImpl(engine);
				app.setEGraph(graph);
				initReady = 1;
				return true;
			} catch (Exception e) {
			}
		}
		initReady = 0;
		return false;
	}

	/**
	 * Inits the rule.
	 *
	 * @param unit the rule to initialize
	 * @return true, if successful
	 */
	public boolean initRule(Unit unit) {
		this.rule = unit;
		if (initReady > 0) {
			try {
				app.setUnit(unit);
				initReady = 2;
				return true;
			} catch (Exception e) {
				initReady = 1;
			}
		}
		return false;
	}
	
	/**
	 * Inits the rule.
	 *
	 * @param unit the rule to initialize
	 * @return true, if successful
	 */
	public boolean saveRule(Unit unit) {
		this.rule = unit;
		return false;
	}

	/**
	 * Gets the unit.
	 *
	 * @param unit the name of the unit
	 * @return the <b>unit</b> or <b>null</b> if module is not initialized or unit is not found
	 * 
	 * @see org.eclipse.emf.henshin.model#getUnit(String name)
	 */
	public Unit getRule() {
		return rule;
	}

	/**
	 * Gets the unit.
	 *
	 * @param unit the name of the unit
	 * @return the <b>unit</b> or <b>null</b> if module is not initialized or unit is not found
	 * 
	 * @see org.eclipse.emf.henshin.model#getUnit(String name)
	 */
	public Unit getUnit(String unit) {
		if (module != null) {
			return module.getUnit(unit);
		}
		return null;
	}

	/**
	 * Sets parameter in the intern UnitApplication and returns <b>true</b> if no problem was occurred.<br><br>
	 * Call:<br>
	 * <code>isParameter(parameter, value, null, null);</code>
	 *
	 * @param parameter the parameter to prove
	 * @param value the value of parameter
	 * @return true, if parameter exists
	 * @see #isParameter(String, Object, String, ParameterKind)
	 */
	public int isParameter(String parameter, Object value) {
		return isParameter(parameter, value, null, null);
	}

	/**
	 * Sets parameter in the intern UnitApplication and returns <b>true</b> if no problem was occurred.<br>
	 * It also checks the type of the value and parameter if given and return <b>false</b> if the type is incorrect.<br>
	 *
	 * @param parameter the parameter to prove
	 * @param value the value of parameter
	 * @param type the type of the value (String, Integer, Float...)
	 * @param kind the kind of parameter (IN, OUT, INOUT, VAR, UNKNOWN)
	 * @return true, if parameter exists and the type of the value and parameter is correct
	 */
	public int isParameter(String parameter, Object value, String type, ParameterKind kind) {
		if (initReady > 1) {
			try {
				Parameter p = app.getUnit().getParameter(parameter);
				if (p == null)
					return 1;
				ParameterKind pk = p.getKind();
				if (kind != null && pk != kind)
					return 3;
				String ec = p.getType().getName();
				if (type != null && !ec.equals(type))
					return 2;
				app.setParameterValue(parameter, value);
				return 0;
			} catch (Exception e) {
			}
		}
		return -1;
	}

	/**
	 * Checks if parameter is an out parameter
	 *
	 * @param parameter the parameter to prove
	 * @return true, if the parameter kind is <code>OUT</code> or <code>INOUT</code>
	 */
	public boolean isOutParameter(String parameter) {
		if (initReady > 1) {
			try {
				Parameter p = app.getUnit().getParameter(parameter);
				if (p == null)
					return false;
				ParameterKind pk = p.getKind();
				if (pk == ParameterKind.INOUT || pk == ParameterKind.OUT)
					return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * Return true if ResourceSet is initialized.
	 *
	 * @return true, if ResourceSet is not null
	 */
	public boolean resource() {
		return resourceSet != null;
	}

	/**
	 * Return true if UnitApplication is initialized.
	 *
	 * @return true, if UnitApplication is not null
	 */
	public boolean app() {
		return app != null;
	}

	/**
	 * Return true if Graph is initialized.
	 *
	 * @return true, if Graph is not null
	 */
	public boolean graph() {
		return graph != null;
	}

	/**
	 * Return true if Module is initialized.
	 *
	 * @return true, if Module is not null
	 */
	public boolean module() {
		return module != null;
	}

	/**
	 * Return true if Engine is initialized.
	 *
	 * @return true, if Engine is not null
	 */
	public boolean engine() {
		return engine != null;
	}

	/**
	 * Return true if Rule is initialized.
	 *
	 * @return true, if Rule has been set
	 */
	public boolean rule() {
		return initReady > 1;
	}

	/**
	 * Clones the HenshinFile.
	 * @return cloned HenshinFile
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected HenshinFile clone() {
		HenshinFile result = new HenshinFile(getPath(), file==null?null:file.getProject());
		if (resource())
			result.initResourceSet(true);
		if (module())
			result.initModule(henshin);
		if (graph())
			result.initGraph(xmi);
		if (app())
			result.initApp();
		if (rule())
			result.initRule(rule);
		return result;
	}

	/**
	 * Returns a string like this: <br>
	 * <ul>
	 * <li>if HenshinFile not exists<br>
	 * <ul><code>false:path/to/file.f\n</code></ul></li>
	 * <li>if HenshinFile exists and all henshin elements are initialized</li>
	 * <ul><code>true:path/to/file.f:rSet:module:graph:engine:app\n</code></ul></li>
	 * 
	 * </ul>
	 * @see org.eclipse.emf.henshin.paths.builder.ProjectFile#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + (resource() ? ":rSet" : "") + (module() ? ":module" : "") + (graph() ? ":graph" : "")
				+ (engine() ? ":engine" : "") + (app() ? ":app" : "") + (rule() ? ":rule" : "") + "\n";
	}
}
