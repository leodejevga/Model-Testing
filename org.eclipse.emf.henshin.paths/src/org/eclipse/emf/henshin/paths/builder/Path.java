package org.eclipse.emf.henshin.paths.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

public class Path {
	private IProject project;
	private Map<String, String> entries;


	public Path(IProject project){
		this.entries = new HashMap<String, String>();
		this.project = project;
	}
	public Path(String path, String identifier, IProject project){
		this.entries = new HashMap<String, String>();
		entries.put(identifier, path);
		this.project = project;
	}
	
	public boolean exists(String identifier){
		if(identifier == null || identifier == "")
			return false;
		return project.findMember(entries.get(identifier))!=null;
	}

	public boolean addPath(String path){
		return addPath(path, null);
	}

	public boolean addPath(String path, String identifier){
		if(identifier!=null)
			this.entries.put(identifier, path);
		return exists(entries.get(identifier));
	}
	public boolean addPath(String[] path, String identifier){
		String p = "";
		if(identifier!=null){
			for(String part : path)
				p += part;
			this.entries.put(identifier, p);
		}
		return exists(identifier);
	}
	
	
}
