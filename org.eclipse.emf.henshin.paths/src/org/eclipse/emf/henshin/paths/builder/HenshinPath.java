package org.eclipse.emf.henshin.paths.builder;

import java.io.File;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.emf.henshin.interpreter.Assignment;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class HenshinPath extends Path {
	private Engine engine;
	private Module module;
	private HenshinResourceSet resourceSet;
	private EGraph graph;
	private UnitApplication app;
	private int initReady = 0;

	public HenshinPath(String path, IProject project) {
		super(path, project);
	}
	public HenshinPath(Path path) {
		super(path.path, path.project);
	}
	
	public boolean initResourceSet(boolean force) {
		if (force || resourceSet == null)
			try{
				String p = project.getRawLocationURI().getPath() + "/" + path;
				resourceSet = new HenshinResourceSet(p);
			} catch(Exception e){
				return false;
			}
		else
			return false;
		return true;
	}

	public boolean initModule(String henshin) {
		if(exists(henshin) && resourceSet != null && henshin.endsWith(".henshin"))
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
		if(exists(xmi) && resourceSet != null && xmi.endsWith(".xmi"))
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
		if (graph != null && module != null) {
			try {
				engine = new EngineImpl();
				app = new UnitApplicationImpl(engine);
				app.setEGraph(graph);
				initReady = 1;
				return true;
			} catch (Exception e) {}
		}
		initReady = 0;
		return false;
	}

	public boolean initRule(String rule) {
		if (initReady > 0) {
			try {
				app.setUnit(module.getUnit(rule));
				initReady = 2;
				return true;
			} catch (Exception e) {
				initReady = 1;
			}
		}
		return false;
	}

	public boolean isParameter(String parameter, Object value) {
		if (initReady > 1) {
			try {
				app.setParameterValue(parameter, value);
				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}
	public boolean isResultParameter(String parameter) {
		if (initReady > 1) {
			try {
				//TODO: Finde heraus wie man den Kind of Parameter feststellen kann
//				Object o = app.getResultAssignment();
//				app.getAssignment().getParameterValues()
//				Object a = ((Assignment) o).getParameterValue(app.getUnit().getParameter("accountId"));
				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}
	@Override
	public String toString() {
		return super.toString() + (resourceSet!=null?":rSet":"") + (module!=null?":module":"") + (graph!=null?":graph":"") + (engine!=null?":engine":"") + (app!=null?":app":"");
	}
}
