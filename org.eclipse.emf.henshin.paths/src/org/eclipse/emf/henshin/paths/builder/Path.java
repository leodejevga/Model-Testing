package org.eclipse.emf.henshin.paths.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

public class Path {
	private IProject project;
	private List<Map<String, String>> entries;
	private int thread;


	public Path(IProject project){
		this.entries = new ArrayList<Map<String,String>>();
		this.entries.add(new HashMap<String, String>());
		thread = 0;
		this.project = project;
	}
	public Path(String path, String identifier, IProject project){
		this.entries = new ArrayList<Map<String,String>>();
		this.entries.add(new HashMap<String, String>());
		entries.get(thread).put(identifier, path);
		this.project = project;
	}

	public boolean exists(String path){
		return project.findMember(path.replaceAll("\"",	""))!=null;
	}

	public String getPath(String identifier){
		String result;
		for(Map<String,String> m : entries){
			result = m.get(identifier);
			if(result!=null)
				return result;
		}
		
		return "";
	}
	
	public boolean addPath(String path){
		return addPath(path, null);
	}
	
	public boolean addPath(String path, String identifier){
		if(identifier!=null)
			if(path.contains("\""))
				this.entries.get(thread).put(identifier, path.replaceAll("\"", ""));
			else
				this.entries.get(thread).put(identifier, path);
		return exists(entries.get(thread).get(identifier));
	}
	
	public void newThread(){
		entries.add(new HashMap<String, String>());
		thread = entries.size()-1;
	}
	
	public boolean prevThread(){
		if(thread!=0){
			thread--;
			return true;
		}
		return false;
	}
	
}
