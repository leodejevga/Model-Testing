package org.eclipse.emf.henshin.paths.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

public class PathHandler {
	private IProject project;
	private Map<String, HenshinPath> entries;
	private int thread;

	public PathHandler(IProject project) {
		this.entries = new HashMap<String, HenshinPath>();
		thread = 0;
		this.project = project;
	}

	public PathHandler(String path, String identifier, IProject project) {
		this.entries = new HashMap<String, HenshinPath>();
		entries.put(identifier, new HenshinPath(path, project));
		this.project = project;
	}

	
	public boolean exists(String path) {
		return project.findMember(path.replace("\"", "")) != null;
	}
	public boolean exists(Path path) {
		boolean ex = project.findMember(path.getPath()) != null;
		return ex;
		//TODO schauen ob rule oder parameter existieren
	}

	
	public String getPathAsString(String identifier) {
		HenshinPath result = entries.get(identifier);
		return result!=null?result.getPath():null;
	}
	public HenshinPath getPath(String identifier) {
		return entries.get(identifier);
	}

	
	public boolean addPath(String path) {
		return addPath(path, null);
	}
	public boolean addPath(HenshinPath path) {
		return addPath(path, null);
	}
	public boolean addPath(String path, String identifier) {
		if (identifier != null && identifier.length()!=0 && path.length()!=0)
			this.entries.put(identifier, this.entries.get(path));
		else 
			return false;
		return true;
	}
	public boolean addPath(HenshinPath path, String identifier) {
		if (identifier != null && identifier.length()!=0)
			this.entries.put(identifier, path);
		else 
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "PH: " + entries;
	}
}
