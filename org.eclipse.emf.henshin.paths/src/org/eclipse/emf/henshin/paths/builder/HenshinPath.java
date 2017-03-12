package org.eclipse.emf.henshin.paths.builder;

//import java.io.File;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EClassifier;
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

public class HenshinPath extends Path {
	private Engine engine;
	private Module module;
	private HenshinResourceSet resourceSet;
	private EGraph graph;
	private UnitApplication app;
	private int initReady = 0;
	private Unit rule;
	private String xmi;
	private String henshin;

	public HenshinPath(String path, IProject project) {
		super(path, project);
	}

	public HenshinPath(Path path) {
		super(path.getPath(), path.project);
	}

	public boolean initResourceSet(boolean force) {
		if (force || resourceSet == null)
			try {
				if (path.getType() != IResource.FOLDER)
					return false;
				String p = path.getLocation().toOSString();
				resourceSet = new HenshinResourceSet(p);
			} catch (Exception e) {
				return false;
			}

		return true;
	}

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

	public Unit getUnit(String unit) {
		if (module != null) {
			return module.getUnit(unit);
		}
		return null;
	}

	public boolean isParameter(String parameter, Object value) {
		return isParameter(parameter, value, null, null);
	}

	public boolean isParameter(String parameter, Object value, String type, ParameterKind kind) {
		if (initReady > 1) {
			try {
				Parameter p = app.getUnit().getParameter(parameter);
				if (p == null)
					return false;
				ParameterKind pk = p.getKind();
				if (kind != null && pk != kind)
					return false;
				String ec = p.getType().getName();
				if (type != null && !ec.equals(type))
					return false;
				app.setParameterValue(parameter, value);
				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

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

	public boolean resource() {
		return resourceSet != null;
	}

	public boolean app() {
		return app != null;
	}

	public boolean graph() {
		return graph != null;
	}

	public boolean module() {
		return module != null;
	}

	public boolean engine() {
		return engine != null;
	}

	public boolean rule() {
		return initReady > 1;
	}

	@Override
	protected HenshinPath clone() {
		HenshinPath result = new HenshinPath(getPath(), project);
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

	@Override
	public String toString() {
		return super.toString() + (resource() ? ":rSet" : "") + (module() ? ":module" : "") + (graph() ? ":graph" : "")
				+ (engine() ? ":engine" : "") + (app() ? ":app" : "") + "\n";
	}
}
